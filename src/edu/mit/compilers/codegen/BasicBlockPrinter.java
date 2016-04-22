package edu.mit.compilers.codegen;

public class BasicBlockPrinter extends BasicBlockTraverser {

  StringBuilder builder = new StringBuilder();

  @Override
  public String toString() {
    return builder.toString();
  }

  @Override
  public void visit(BasicBlock b) {
    builder.append(b.label).append(":");
    for (Instruction i : b) {
      builder.append("\n\t").append(i.toString());
    }

    builder.append('\n');
  }

  @Override
  public void clear() {
    super.clear();
    builder = new StringBuilder();
  }
}
