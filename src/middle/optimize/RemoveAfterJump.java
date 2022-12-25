package middle.optimize;

import middle.MiddleCode;
import middle.middlecode.*;
import middle.symboltable.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * 将跳转语句后面的指令（属于死代码，无法被执行到）删除
 */
public class RemoveAfterJump implements MidOptimizer {

    public RemoveAfterJump() {

    }

    @Override
    public void optimize(MiddleCode ir) {
        HashSet<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>(); // BFS
        for (FuncInfo func : ir.getFunctions().values()) {
            queue.offer(func.getBody());
            while (!queue.isEmpty()) {
                BasicBlock block = queue.poll();
                if (visited.contains(block)) {
                    continue;
                }
                visited.add(block);
                Node node = block.getHead();
                while (Objects.nonNull(node) && node.hasNext()) {
                    if (node instanceof Jump) {
                        queue.offer(((Jump) node).getTarget());
                        // 删掉跳转后的任何语句
                        while (node.getNext().hasNext()) {
                            Node follow = node.getNext();
                            follow.remove();
                        }
                    } else if (node instanceof BranchIfElse) {
                        queue.offer(((BranchIfElse) node).getThenTarget());
                        queue.offer(((BranchIfElse) node).getElseTarget());
                        // 删掉跳转后的任何语句
                        while (node.getNext().hasNext()) {
                            Node follow = node.getNext();
                            follow.remove();
                        }
                    } else if (node instanceof Return) {
                        while (node.getNext().hasNext()) {
                            Node follow = node.getNext();
                            follow.remove();
                        }
                    }
                    node = node.getNext();
                }
            }
        }
    }
}
