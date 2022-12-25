package back.instruction;

import back.store.RegisterFile;

/**
 * jr 指令，跳转到寄存器, 在写模拟器时需要特殊处理
 */
public class jr extends MipsInstruction {

    private int regSrc;

    public jr(int regSrc) {
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return "jr $" + RegisterFile.getRegisterName(regSrc);
    }
}
