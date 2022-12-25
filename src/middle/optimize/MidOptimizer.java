package middle.optimize;

import middle.MiddleCode;
import middle.middlecode.*;

import java.util.Queue;

/**
 * 中间代码优化的基本接口
 */
public interface MidOptimizer {
    void optimize(MiddleCode ir);

    default void detectBranch(Node node, Queue<BasicBlock> queue) {
        if (node instanceof Jump) {
            queue.offer(((Jump) node).getTarget());
        } else if (node instanceof BranchIfElse) {
            queue.offer(((BranchIfElse) node).getThenTarget());
            queue.offer(((BranchIfElse) node).getElseTarget());
        }
    }
}
