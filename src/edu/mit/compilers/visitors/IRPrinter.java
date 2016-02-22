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

  @Override
  protected void visit(Length node) {
    System.out.print("@");
    System.out.print(node.toString());
  }

  @Override
  protected void visit(Load node) {
    System.out.print(node.arrayName);
    System.out.print("[");
    enter(node.index);
    System.out.print("]");
  }
  
  @Override
  protected void visit(Store node) {
    System.out.print(node.arrayName);
    System.out.print("[");
    enter(node.index);
    System.out.print("] = ");
    enter(node.value);
  }
}
