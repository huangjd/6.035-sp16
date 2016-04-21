package edu.mit.compilers.codegen;

public class BasicBlockPrinter extends BasicBlockTraverser {

  StringBuilder builder = new StringBuilder();

  @Override
  public String toString() {
    return builder.toString();
  }

  @Override
  public T visit(BasicBlock b, T in) {
    builder.append(b.label).append(":");
    for (Instruction i : b) {
      builder.append("\n\t").append(i.toString());
    }

    builder.append('\n');
    return in;
  }

  @Override
  public void clear() {
    super.clear();
    builder = new StringBuilder();
  }
}
