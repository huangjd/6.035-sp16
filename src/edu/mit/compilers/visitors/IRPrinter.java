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
  
  @Override
  protected void visit(And node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" && ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Not node) {
    System.out.print("!");
    enter(node.right);
  }
  
  @Override
  protected void visit(Or node) {
    System.out.print("(");
    enter(node.left);
    System.out.print(" || ");
    enter(node.right);
    System.out.println(")");
  }
 
  
  @Override
  protected void visit(Ternary node) {
    System.out.print("(");
    enter(node.cond);
    System.out.print(" ? ");
    enter(node.trueExpr);
    System.out.print(" : ");
    enter(node.falseExpr);
    System.out.println(")");
  }
  
  @Override
  protected void visit(Div node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" / ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Eq node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" == ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Ge node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" >= ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Gt node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" > ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Le node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" <= ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Lt node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" < ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Ne node) {
	  System.out.println("(");
	  enter(node.left);
	  System.out.println(" != ");
	  enter(node.right);
	  System.out.println(")");
  }
  
  @Override
  protected void visit(Mul node) {
    System.out.print("(");
    enter(node.left);
    System.out.print(" * ");
    enter(node.right);
    System.out.println(")");
  }
  
  @Override
  protected void visit(Mod node) {
    System.out.print("(");
    enter(node.left);
    System.out.print(" % ");
    enter(node.right);
    System.out.println(")");
  }
  
  @Override
  protected void visit(Minus node) {
    System.out.print("-");
    enter(node.right);
  }
  
  
  
  
}
