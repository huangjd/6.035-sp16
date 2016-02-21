package edu.mit.compilers.nodes;

public class Visitor {

  public NodeProxy enter(NodeProxy node) {
    node.accept(this);
    return node;
  }

  protected void visit(Add node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Sub node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Mul node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Div node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Mod node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Lt node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Le node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Ge node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Gt node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Eq node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Ne node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(And node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Or node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Not node) {
    node.right.accept(this);
  }

  protected void visit(Minus node) {
    node.right.accept(this);
  }

  protected void visit(Ternary node) {
    node.cond.accept(this);
    node.trueExpr.accept(this);
    node.falseExpr.accept(this);
  }

  protected void visit(Length node) {
  }

  protected void visit(IntLiteral node) {
  }

  protected void visit(BooleanLiteral node) {
  }

  protected void visit(StringLiteral node) {
  }

  protected void visit(Pass node) {
  }
}
