package edu.mit.compilers.codegen;

public class TrivialJumpRemove extends BasicBlockTraverser {

  BasicBlock last = null;

  @Override
  protected void visit(BasicBlock b) {
    if (last != null && last.taken == b && last.notTaken == null) {
      last.remove(last.size() - 1);
    }
    last = b;
  }

}
