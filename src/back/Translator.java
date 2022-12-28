package back;

import back.store.Memory;
import back.store.RegisterFile;
import back.instruction.*;
import middle.MiddleCode;
import middle.middlecode.*;
import middle.symboltable.FuncInfo;
import middle.symboltable.Symbol;
import utility.MathUtil;

import java.util.*;

public class Translator {
    private MiddleCode ir;

    private RegisterMap registerMap = new RegisterMap();
    private Mips mips = new Mips();

    // 记录当前正在翻译的函数
    private FuncInfo currentFunc = null;
    private int currentStackSize = 0; // 当前正在翻译的函数已经用掉的栈的大小（局部变量+临时变量）

    // BFS 基本块
    private final HashSet<BasicBlock> visitedBlock = new HashSet<>();
    private final Queue<BasicBlock> queueBlock = new LinkedList<>();

    public Translator(MiddleCode ir) {
        this.ir = ir;
    }

    public Mips toMips() {
        loadStringConstant();
        loadGlobals();
        mips.append(new j(ir.getMainFunction().getLabelName()));
        for (FuncInfo meta : ir.getFunctions().values()) {
            translateFunction(meta);
        }
        return mips;
    }

    public void loadGlobals() {
        Map<String, Integer> globalAddress = ir.getGlobalAddress();
        Memory initMem = mips.getInitMem(); // Modifiable!
        // Global Variable
        for (Map.Entry<String, Integer> entry : ir.getGlobalVariables().entrySet()) {
            int address = globalAddress.get(entry.getKey());
            initMem.storeWord(address, entry.getValue());
        }
        // Global Array
        for (Map.Entry<String, List<Integer>> entry : ir.getGlobalArrays().entrySet()) {
            int baseAddress = globalAddress.get(entry.getKey());
            for (int i = 0; i < entry.getValue().size(); i++) {
                initMem.storeWord(baseAddress + 4 * i, entry.getValue().get(i));
            }
        }
    }

    public void loadStringConstant() {
        for (Map.Entry<String, String> entry : ir.getGlobalStrings().entrySet()) {
            mips.addStringConstant(entry.getKey(), entry.getValue());
        }
    }

    // 从函数头部开始, 将基本块中的中间代码翻译成 MIPS 目标代码
    public void translateFunction(FuncInfo funcInfo) {
        currentFunc = funcInfo;
        currentStackSize = funcInfo.getStackSize();
        BasicBlock head = funcInfo.getBody();
        queueBlock.offer(head);
        while (!queueBlock.isEmpty()) {
            BasicBlock block = queueBlock.poll();
            if (visitedBlock.contains(block)) {
                continue;
            }
            visitedBlock.add(block);
            translateBasicBlock(block);
        }
    }

    /* ------ 代码翻译 ------ */
    // 记录当前基本块中临时变量的 def-use 情况
    private final Map<Symbol, Integer> tempDefUse = new HashMap<>(); // key: 临时变量的 Symbol, value: 临时变量在当前基本块中还剩余几次被使用

    // 记录临时变量的使用: 如果某条中间指令的
    private void recordUseTempVariable(Operand operand) {
        if (operand instanceof Symbol && !((Symbol) operand).hasAddress()) {
            Symbol tempSymbol = (Symbol) operand;
            tempDefUse.merge(tempSymbol, 1, Integer::sum);
        }
    }

    // 处理在当前基本块中临时变量的使用次数
    private void processTempVariableUses(BasicBlock block) {
        tempDefUse.clear();
        Node code = block.getHead();
        while (code.hasNext()) {
            if (code instanceof AddressOffset) {
                recordUseTempVariable(((AddressOffset) code).getBase());
                recordUseTempVariable(((AddressOffset) code).getOffset());
            } else if (code instanceof PrintInt) {
                recordUseTempVariable(((PrintInt) code).getValue());
            } else if (code instanceof BinaryOp) {
                recordUseTempVariable(((BinaryOp) code).getSrc1());
                recordUseTempVariable(((BinaryOp) code).getSrc2());
            } else if (code instanceof Input) {
                // store input into pointer
                if (((Input) code).getDst().getType().equals(Symbol.Type.POINTER)) {
                    recordUseTempVariable(((Input) code).getDst());
                }
            } else if (code instanceof UnaryOp) {
                recordUseTempVariable(((UnaryOp) code).getSrc());
            } else if (code instanceof BranchIfElse) {
                recordUseTempVariable(((BranchIfElse) code).getCondition());
            } else if (code instanceof PointerOp) {
                if (((PointerOp) code).getOp().equals(PointerOp.Op.STORE)) {
                    recordUseTempVariable(((PointerOp) code).getSrc());
                }
                recordUseTempVariable(((PointerOp) code).getAddress());
            } else if (code instanceof Call) {
                List<Operand> params = ((Call) code).getParams();
                params.forEach(this::recordUseTempVariable);
            } else if (code instanceof Return) {
                if (((Return) code).hasValue()) {
                    recordUseTempVariable(((Return) code).getValue());
                }
            }
            code = code.getNext();
        }
    }

    public void translateBasicBlock(BasicBlock block) {
        processTempVariableUses(block);
        mips.setLabel(block.getLabel());
        Node code = block.getHead();
        while (code.hasNext()) {
            // mips.setDescription(code.toString());
            if (code instanceof BinaryOp) {
                translateBinaryOp((BinaryOp) code);
            } else if (code instanceof Call) {
                translateCall((Call) code);
            } else if (code instanceof Return) {
                translateReturn((Return) code);
            } else if (code instanceof UnaryOp) {
                translateUnaryOp((UnaryOp) code);
            } else if (code instanceof PointerOp) {
                translatePointerOp((PointerOp) code);
            } else if (code instanceof Input || code instanceof PrintInt || code instanceof PrintStr) {
                translateIO(code);
            } else if (code instanceof AddressOffset) {
                translateAddressOffset((AddressOffset) code);
            } else if (code instanceof Jump || code instanceof BranchIfElse) {
                translateBranchOrJump(code);
            } else {
                return;
            }
            code = code.getNext();
        }
    }

    private void consumeUseTempVariable(Symbol symbol) {
        if (symbol.hasAddress()) {
            return;
        }
        if (!tempDefUse.containsKey(symbol)) {
            System.err.println(symbol);
        }
        int count = tempDefUse.get(symbol);
        if (count != 1) {
            tempDefUse.put(symbol, count - 1);
        } else {
            tempDefUse.remove(symbol);
            // 如果不再使用的临时变量在寄存器中，应释放寄存器
            if (registerMap.isAllocated(symbol)) {
                registerMap.dealloc(registerMap.getRegisterOfSymbol(symbol));
            }
        }
    }

    // 为临时变量分配栈上地址
    private void assignAddressForTemp(Symbol symbol) {
        currentStackSize += 4;
        symbol.setAddress(currentStackSize);
        symbol.setLocal(true);
    }

    private void recycleRegisterOfVar(int register, Symbol var) {
        if (!var.hasAddress()) {
            // 对临时变量，为其分配地址
            assignAddressForTemp(var);
        }
        // 将变量存回内存
        String comment = String.format("recycle %s from $%s", var, RegisterFile.getRegisterName(register));
        if (!var.isLocal()) {
            mips.append(new sw(RegisterFile.Register.GP, var.getAddress(), register), comment);
        } else {
            mips.append(new sw(RegisterFile.Register.SP, -var.getAddress(), register), comment);
        }
    }

    // 为变量分配寄存器(如已经分配过寄存器则返回该变量所在的寄存器
    // load: 是否要从内存中读取变量的值(对于源操作数应为 true, 目标操作数应为 false)
    private int allocRegister(Symbol symbol, boolean load) {
        if (registerMap.isAllocated(symbol)) {
            registerMap.refresh(symbol);
            return registerMap.getRegisterOfSymbol(symbol);
        }
        if (!registerMap.hasFreeRegister()) {
            // 寄存器池已满，需要置换掉一个寄存器
            int register = registerMap.registerToReplace();
            Symbol var = registerMap.dealloc(register);
            // 将该变量存储进内存
            recycleRegisterOfVar(register, var);
        }
        int register = registerMap.allocRegister(symbol);
        if (load && symbol.hasAddress()) {
            String comment = String.format("assign $%s to %s", RegisterFile.getRegisterName(register), symbol);
            if (!symbol.isLocal()) {
                mips.append(new lw(RegisterFile.Register.GP, symbol.getAddress(), register), comment);
            } else {
                mips.append(new lw(RegisterFile.Register.SP, -symbol.getAddress(), register), comment);
            }
        }
        return register;
    }

    private String registerCommentThree(int regDst, Symbol dst, int regSrc1, Symbol src1, int regSrc2, Symbol src2) {
        return String.format("$%s --> %s, $%s --> %s, $%s --> %s",
                RegisterFile.getRegisterName(regDst), dst,
                RegisterFile.getRegisterName(regSrc1), src1,
                RegisterFile.getRegisterName(regSrc2), src2);
    }

    private String registerCommentTwo(int regDst, Symbol dst, int regSrc, Symbol src) {
        return String.format("$%s --> %s, $%s --> %s", RegisterFile.getRegisterName(regDst), dst, RegisterFile.getRegisterName(regSrc), src);
    }

    private String registerCommentOne(int regDst, Symbol dst) {
        return String.format("$%s --> %s", RegisterFile.getRegisterName(regDst), dst);
    }

    // 跳转前，清除寄存器分配, 所有寄存器中的变量均写回栈
    private void clearRegister(boolean writeBack) {
        if (writeBack) {
            Map<Integer, Symbol> regState = registerMap.getState();
            for (Map.Entry<Integer, Symbol> entry : regState.entrySet()) {
                int register = entry.getKey();
                Symbol var = entry.getValue();
                recycleRegisterOfVar(register, var);
            }
        }
        registerMap.clear();
    }

    // R 型运算指令
    private void binaryOpHelper(int regSrc1, int regSrc2, int regDst, BinaryOp.Op op, Symbol src1, Symbol src2, Symbol dst) {
        switch (op) {
            case ADD: mips.append(new addu(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2)); break;
            case SUB: mips.append(new subu(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2)); break;
            case MUL:
                mips.append(new mul(regSrc1, regSrc2));
                mips.append(new mflo(regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case DIV:
                mips.append(new div(regSrc1, regSrc2));
                mips.append(new mflo(regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case MOD:
                mips.append(new div(regSrc1, regSrc2));
                mips.append(new mfhi(regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case ANDL: case AND: mips.append(new and(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2)); break;
            case ORL: case OR: mips.append(new or(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2)); break;
            case XOR: mips.append(new xor(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2)); break;
            case EQ:
                mips.append(new seq(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case NE:
                mips.append(new sne(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case LE:
                mips.append(new sle(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case LT:
                mips.append(new slt(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case GE:
                mips.append(new sge(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case GT:
                mips.append(new sgt(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case SLL:
                mips.append(new sllv(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case SRA:
                mips.append(new srav(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case MOVN:
                mips.append(new movn(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case MOVZ:
                mips.append(new movz(regSrc1, regSrc2, regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            case MULHI:
                mips.append(new mul(regSrc1, regSrc2));
                mips.append(new mfhi(regDst), registerCommentThree(regDst, dst, regSrc1, src1, regSrc2, src2));
                break;
            default: break;
        }
    }

    private void translateBinaryOp(BinaryOp code) {
        // 需要为操作数符号分配寄存器, 还需要考虑操作数是变量还是立即数来选取指令
        // 如果是双立即数就直接算出结果，存给临时变量
        // 操作数变量和目标变量必须都已经分配上寄存器再进行计算
        int regSrc1, regSrc2;
        if (code.getSrc1() instanceof Immediate) {
            if (code.getSrc2() instanceof Immediate) {
                // 双立即数, 直接算出结果
                int src1 = ((Immediate) code.getSrc1()).getValue();
                int src2 = ((Immediate) code.getSrc2()).getValue();
                int result;
                boolean moveTo = true;
                switch (code.getOp()) {
                    case ADD: result = src1 + src2; break;
                    case SUB: result = src1 - src2; break;
                    case MUL: result = src1 * src2; break;
                    case DIV: result = src1 / src2; break;
                    case MOD: result = src1 % src2; break;
                    case ANDL: result = ((src1 != 0) && (src2 != 0)) ? 1 : 0; break;
                    case ORL: result = ((src1 != 0) || (src2 != 0)) ? 1 : 0; break;
                    case AND: result = src1 & src2; break;
                    case OR: result = src1 | src2; break;
                    case XOR: result = src1 ^ src2; break;
                    case EQ: result = (src1 == src2) ? 1 : 0; break;
                    case NE: result = (src1 != src2) ? 1 : 0; break;
                    case LE: result = (src1 <= src2) ? 1 : 0; break;
                    case LT: result = (src1 < src2) ? 1 : 0; break;
                    case GE: result = (src1 >= src2) ? 1 : 0; break;
                    case GT: result = (src1 > src2) ? 1 : 0; break;
                    case SLL: result = (src1 << (src2 & 0x1f)); break;
                    case SRA: result = (src1 >> (src2 & 0x1f)); break;
                    case MOVN: result = src1; moveTo = src2 != 0; break;
                    case MOVZ: result = src1; moveTo = src2 == 0; break;
                    case MULHI: result = MathUtil.multiplyHigh(src1, src2); break;
                    default: return;
                }
                if (moveTo) {
                    int register = allocRegister(code.getDst(), false);
                    mips.append(new li(register, result), registerCommentOne(register, code.getDst()));
                }
            } else {
                // 立即数, 寄存器
                regSrc1 = RegisterFile.Register.V1;
                mips.append(new li(regSrc1, ((Immediate) code.getSrc1()).getValue()));
                regSrc2 = allocRegister((Symbol) code.getSrc2(), true);
                consumeUseTempVariable((Symbol) code.getSrc2());
                int regDst = allocRegister(code.getDst(), false);
                binaryOpHelper(regSrc1, regSrc2, regDst, code.getOp(), null, (Symbol) code.getSrc2(), code.getDst());
            }
        } else {
            if (code.getSrc2() instanceof Immediate) {
                // 寄存器, 立即数 (I 型指令)
                regSrc1 = allocRegister((Symbol) code.getSrc1(), true);
                consumeUseTempVariable((Symbol) code.getSrc1());
                int regDst = allocRegister(code.getDst(), false);
                int immediate = ((Immediate) code.getSrc2()).getValue();
                switch (code.getOp()) {
                    case ADD: mips.append(new addiu(regSrc1, immediate, regDst),
                            registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                    break;
                    case SUB: mips.append(new addiu(regSrc1, -immediate, regDst),
                            registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1())); break;
                    case MUL:
                        regSrc2 = RegisterFile.Register.V1;
                        mips.append(new li(regSrc2, ((Immediate) code.getSrc2()).getValue()));
                        mips.append(new mul(regSrc1, regSrc2));
                        mips.append(new mflo(regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case DIV:
                        regSrc2 = RegisterFile.Register.V1;
                        mips.append(new li(regSrc2, ((Immediate) code.getSrc2()).getValue()));
                        mips.append(new div(regSrc1, regSrc2));
                        mips.append(new mflo(regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case MOD:
                        regSrc2 = RegisterFile.Register.V1;
                        mips.append(new li(regSrc2, ((Immediate) code.getSrc2()).getValue()));
                        mips.append(new div(regSrc1, regSrc2));
                        mips.append(new mfhi(regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case ANDL: case AND: mips.append(new andi(regSrc1, immediate, regDst),
                            registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1())); break;
                    case ORL: case OR: mips.append(new ori(regSrc1, immediate, regDst),
                            registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1())); break;
                    case XOR: mips.append(new xori(regSrc1, immediate, regDst),
                            registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1())); break;
                    case EQ:
                        mips.append(new seqi(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case NE:
                        mips.append(new snei(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case LE:
                        mips.append(new slei(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case LT:
                        if (Short.MIN_VALUE <= immediate && immediate <= Short.MAX_VALUE) {
                            mips.append(new slti(regSrc1, immediate, regDst),
                                    registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        } else {
                            regSrc2 = RegisterFile.Register.V1;
                            mips.append(new li(regSrc2, immediate));
                            mips.append(new slt(regSrc1, regSrc2, regDst),
                                    registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        }
                        break;
                    case GE:
                        mips.append(new sgei(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case GT:
                        mips.append(new sgti(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case SLL:
                        mips.append(new sll(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case SRA:
                        mips.append(new sra(regSrc1, immediate, regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    case MOVN:
                        if (immediate != 0) {
                            mips.append(new move(regDst, regSrc1), registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        }
                        break;
                    case MOVZ:
                        if (immediate == 0) {
                            mips.append(new move(regDst, regSrc1), registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        }
                        break;
                    case MULHI:
                        regSrc2 = RegisterFile.Register.V1;
                        mips.append(new li(regSrc2, ((Immediate) code.getSrc2()).getValue()));
                        mips.append(new mul(regSrc1, regSrc2));
                        mips.append(new mfhi(regDst),
                                registerCommentTwo(regDst, code.getDst(), regSrc1, (Symbol) code.getSrc1()));
                        break;
                    default: break;
                }
            } else {
                // 寄存器, 寄存器 (R 型指令)
                // consumeUseTempVariable((Symbol) code.getSrc1());
                regSrc1 = allocRegister((Symbol) code.getSrc1(), true);
                regSrc2 = allocRegister((Symbol) code.getSrc2(), true);
                consumeUseTempVariable((Symbol) code.getSrc1());
                consumeUseTempVariable((Symbol) code.getSrc2());
                int regDst = allocRegister(code.getDst(), false);
                binaryOpHelper(regSrc1, regSrc2, regDst, code.getOp(), (Symbol) code.getSrc1(), (Symbol) code.getSrc2(), code.getDst());
            }
        }
    }

    private void translateUnaryOp(UnaryOp code) {
        if (code.getSrc() instanceof Immediate) {
            int immediate = ((Immediate) code.getSrc()).getValue();
            int regDst = allocRegister(code.getDst(), false);
            String comment = registerCommentOne(regDst, code.getDst());
            switch (code.getOp()) {
                case ABS: mips.append(new li(regDst, MathUtil.absoluteValue(immediate)), comment); break;
                case CLZ: mips.append(new li(regDst, MathUtil.countLeadingZeros(immediate)), comment); break;
                case NOT: mips.append(new li(regDst, immediate != 0 ? 0 : 1), comment); break;
                case CLO: mips.append(new li(regDst, MathUtil.countLeadingOnes(immediate)), comment); break;
                case MOV: mips.append(new li(regDst, immediate), comment); break;
                case NEG: mips.append(new li(regDst, -immediate), comment); break;
                default: break;
            }
        } else {
            int regSrc = allocRegister((Symbol) code.getSrc(), true);
            int regDst = allocRegister(code.getDst(), false);
            consumeUseTempVariable((Symbol) code.getSrc());
            String comment = registerCommentTwo(regDst, code.getDst(), regSrc, (Symbol) code.getSrc());
            switch (code.getOp()) {
                case ABS: mips.append(new abs(regDst, regSrc)); break;
                case CLZ: mips.append(new clz(regDst, regSrc)); break;
                case NOT: mips.append(new seq(regSrc, RegisterFile.Register.ZERO, regDst), comment); break;
                case CLO: mips.append(new clo(regDst, regSrc)); break;
                case MOV: mips.append(new move(regDst, regSrc), comment); break;
                case NEG: mips.append(new subu(RegisterFile.Register.ZERO, regSrc, regDst), comment); break;
                default: break;
            }
        }
    }

    private void translateIO(Node code) {
        if (code instanceof Input) {
            mips.append(new li(RegisterFile.Register.V0, syscall.READ_INTEGER));
            mips.append(new syscall());
            int regDst = allocRegister(((Input) code).getDst(), false);
            if (!((Input) code).getDst().getType().equals(Symbol.Type.POINTER)) {
                mips.append(new move(regDst, RegisterFile.Register.V0), registerCommentOne(regDst, ((Input) code).getDst()));
            } else {
                mips.append(new sw(regDst, 0, RegisterFile.Register.V0), registerCommentOne(regDst, ((Input) code).getDst()));
            }
        } else if (code instanceof PrintInt) {
            mips.append(new li(RegisterFile.Register.V0, syscall.PRINT_INTEGER));
            if (((PrintInt) code).getValue() instanceof Immediate) {
                mips.append(new li(RegisterFile.Register.A0, ((Immediate) ((PrintInt) code).getValue()).getValue()));
            } else {
                int regSrc = allocRegister((Symbol) ((PrintInt) code).getValue(), true);
                consumeUseTempVariable((Symbol) ((PrintInt) code).getValue());
                mips.append(new move(RegisterFile.Register.A0, regSrc), registerCommentOne(regSrc, (Symbol) ((PrintInt) code).getValue()));
            }
            mips.append(new syscall());
        } else {
            String label = ((PrintStr) code).getLabel();
            int address = mips.getStringAddress(label);
            mips.append(new li(RegisterFile.Register.A0, Mips.STRING_START_ADDRESS + address));
            mips.append(new li(RegisterFile.Register.V0, syscall.PRINT_STRING));
            mips.append(new syscall());
        }
    }

    private void translateCall(Call code) {
        // 保存现场 (寄存器强制写回, 下压 sp，保存返回地址 $ra), 传递参数, 生成跳转指令 (jal)
        // 恢复现场 (上弹 sp，恢复返回地址 $ra)，返回值 $v0 赋给相应寄存器
        clearRegister(true);
        // 保存 ra
        mips.append(new sw(RegisterFile.Register.SP, 0, RegisterFile.Register.RA));
        // 计算子函数栈基地址
        mips.append(new addiu(RegisterFile.Register.SP, -currentStackSize - 4, RegisterFile.Register.A0)); // A0 = 子函数的 sp
        // 传递参数
        List<Operand> params = code.getParams();
        int offset = 0;
        for (Operand param : params) {
            offset += 4;
            if (param instanceof Immediate) {
                mips.append(new li(RegisterFile.Register.V0, ((Immediate) param).getValue()));
                mips.append(new sw(RegisterFile.Register.A0, -offset, RegisterFile.Register.V0));
            } else {
                assert param instanceof Symbol;
                int reg = allocRegister((Symbol) param, true);
                consumeUseTempVariable((Symbol) param);
                mips.append(new sw(RegisterFile.Register.A0, -offset, reg), registerCommentOne(reg, (Symbol) param));
            }
        }
        // 移动 $sp
        mips.append(new move(RegisterFile.Register.SP, RegisterFile.Register.A0));
        clearRegister(false);
        // 生成跳转指令
        mips.append(new jal(code.getFunction().getLabelName()));
        // 恢复现场
        mips.append(new addiu(RegisterFile.Register.SP, currentStackSize + 4, RegisterFile.Register.SP));
        mips.append(new lw(RegisterFile.Register.SP, 0, RegisterFile.Register.RA)); // 恢复返回地址 $ra
        if (code.hasRet()) {
            int regRet = allocRegister(code.getRet(), false);
            mips.append(new move(regRet, RegisterFile.Register.V0), registerCommentOne(regRet, code.getRet()));
        }
    }

    private void translateReturn(Return code) {
        // 寄存器写回, 返回值 $v0 赋值, 生成 jr 指令
        if (currentFunc.isMain()) {
            clearRegister(false);
            mips.append(new li(RegisterFile.Register.V0, syscall.TERMINATE)); // Exit
            mips.append(new syscall());
            return;
        }
        if (code.hasValue()) {
            Operand value = code.getValue();
            if (value instanceof Immediate) {
                mips.append(new li(RegisterFile.Register.V0, ((Immediate) value).getValue()));
            } else {
                int regSrc = allocRegister((Symbol) value, true);
                consumeUseTempVariable((Symbol) value);
                mips.append(new move(RegisterFile.Register.V0, regSrc), registerCommentOne(regSrc, (Symbol) value));
            }
        }
        clearRegister(true);
        mips.append(new jr(RegisterFile.Register.RA));
    }

    private void translateAddressOffset(AddressOffset code) {
        Symbol base = code.getBase();
        Operand offset = code.getOffset();
        Symbol pointer = code.getTarget();
        int regPtr = allocRegister(pointer, true);
        String commentOne = registerCommentOne(regPtr, pointer);
        if (base.getType().equals(Symbol.Type.ARRAY)) {
            if (offset instanceof Immediate) {
                // 数组 + 立即数
                String commentArray = "global " + base;
                if (base.isLocal()) {
                    mips.append(new addiu(RegisterFile.Register.SP, -base.getAddress() + ((Immediate) offset).getValue(), regPtr), commentOne + "; " + commentArray);
                } else {
                    mips.append(new addiu(RegisterFile.Register.GP, base.getAddress() + ((Immediate) offset).getValue(), regPtr), commentOne + "; " + commentArray);
                }
            } else {
                // 数组 + 寄存器
                int regSrc = allocRegister((Symbol) offset, true);
                consumeUseTempVariable((Symbol) offset);
                String commentTwo = registerCommentTwo(regPtr, pointer, regSrc, (Symbol) offset);
                if (!base.isLocal()) {
                    mips.append(new addiu(RegisterFile.Register.GP, base.getAddress(), regPtr), commentOne);
                } else {
                    mips.append(new addiu(RegisterFile.Register.SP, -base.getAddress(), regPtr), commentOne);
                }
                mips.append(new addu(regPtr, regSrc, regPtr), commentTwo);
            }
        } else {
            if (offset instanceof Immediate) {
                // 指针 + 立即数
                int regSrc = allocRegister(base, true);
                consumeUseTempVariable(base);
                String commentTwo = registerCommentTwo(regPtr, pointer, regSrc, base);
                mips.append(new addiu(regSrc, ((Immediate) offset).getValue(), regPtr), commentTwo);
            } else {
                // 指针 + 寄存器
                int regSrc1 = allocRegister(base, true);
                int regSrc2 = allocRegister((Symbol) offset, true);
                consumeUseTempVariable(base);
                consumeUseTempVariable((Symbol) offset);
                String commentThree = registerCommentThree(regPtr, pointer, regSrc1, base, regSrc2, (Symbol) offset);
                mips.append(new addu(regSrc1, regSrc2, regPtr), commentThree);
            }
        }
    }

    private void translatePointerOp(PointerOp code) {
        int regBase = allocRegister(code.getAddress(), true);
        if (code.getOp().equals(PointerOp.Op.STORE)) {
            Operand src = code.getSrc();
            int regSrc;
            if (src instanceof Immediate) {
                consumeUseTempVariable(code.getAddress());
                regSrc = RegisterFile.Register.V0;
                mips.append(new li(regSrc, ((Immediate) src).getValue()));
                String comment = registerCommentOne(regBase, code.getAddress());
                mips.append(new sw(regBase, 0, regSrc), comment);
            } else {
                regSrc = allocRegister((Symbol) src, true);
                consumeUseTempVariable(code.getAddress());
                consumeUseTempVariable((Symbol) src);
                String comment = registerCommentTwo(regBase, code.getAddress(), regSrc, (Symbol) src);
                mips.append(new sw(regBase, 0, regSrc), comment);
            }
        } else {
            consumeUseTempVariable(code.getAddress());
            int regDst = allocRegister(code.getDst(), false);
            String comment = registerCommentTwo(regBase, code.getAddress(), regDst, code.getDst());
            mips.append(new lw(regBase, 0, regDst), comment);
        }
    }

    private void translateBranchOrJump(Node code) {
        clearRegister(true);
        // Use $v0 to load condition
        if (code instanceof Jump) {
            mips.append(new j(((Jump) code).getTarget().getLabel()));
            queueBlock.offer(((Jump) code).getTarget());
        } else {
            Operand cond = ((BranchIfElse) code).getCondition();
            int regSrc = RegisterFile.Register.V0;
            if (cond instanceof Immediate) {
                mips.append(new li(regSrc, ((Immediate) cond).getValue()));
            } else {
                Symbol symbol = (Symbol) cond;
                if (symbol.isLocal()) {
                    mips.append(new lw(RegisterFile.Register.SP, -symbol.getAddress(), regSrc));
                } else {
                    mips.append(new lw(RegisterFile.Register.GP, symbol.getAddress(), regSrc));
                }
            }
            mips.append(new bne(regSrc, RegisterFile.Register.ZERO, ((BranchIfElse) code).getThenTarget().getLabel()));
            mips.append(new j(((BranchIfElse) code).getElseTarget().getLabel()));
            queueBlock.offer(((BranchIfElse) code).getElseTarget());
            queueBlock.offer(((BranchIfElse) code).getThenTarget());
        }
    }

}
