package edu.mit.compilers.visitors;

import edu.mit.compilers.nodes.*;

public class IRPrinter extends Visitor {

  private int indent = 0;

  public IRPrinter() {
  }

  void indent() {
    indent++;
  }

  void unindent() {
    indent--;
    if (indent < 0) {
      throw new RuntimeException();
    }
  }

  @Override
  protected void visit(Add node) {
    System.out.print("(");
    enter(node.left);
    System.out.print(" + ");
    enter(node.right);
    System.out.println(")");
  }

  @Override
  protected void visit(Sub node) {
    System.out.print("(");
    enter(node.left);
    System.out.print(" - ");
    enter(node.right);
    System.out.println(")");
  }

  @Override
  protected void visit(IntLiteral node) {
    System.out.print(node.toString());
  }

  @Override
  protected void visit(Length node) {
    System.out.print("@");
    System.out.print(node.toString());
  }

  @Override
  protected void visit(Load node) {
    System.out.print(node.array);
    System.out.print("[");
    enter(node.index);
    System.out.print("]");
  }

  @Override
  protected void visit(Store node) {
    System.out.print(node.array);
    System.out.print("[");
    enter(node.index);
    System.out.print("] = ");
    enter(node.value);
  }
}
