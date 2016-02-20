package edu.mit.compilers.visitors;

import edu.mit.compilers.nodes.*;

public class IRPrinter extends Visitor {

  public IRPrinter() {
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
}
