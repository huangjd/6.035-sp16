package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public abstract class Node {
  protected SourcePosition position;
  protected int hashCache;

  abstract void dispatch(Visitor visitor);

  protected Node(SourcePosition pos) {
    position = pos;
  }

  public SourcePosition getSourcePosition() {
    return position;
  }

  @Override
  public String toString() {
    return "";
  }

  @Override
  public int hashCode() {
    return hashCache;
  }
}

abstract class Program extends Node {

  protected Program(SourcePosition pos) {
    super(pos);
    // TODO Auto-generated constructor stub
  }

}

abstract class Function extends Node {

  protected Function(SourcePosition pos) {
    super(pos);
    // TODO Auto-generated constructor stub
  }

}

abstract class Statement extends Node {
  protected Statement(SourcePosition pos) {
    super(pos);
  }

  public StatementNode box() {
    return new StatementNode(this);
  }
}

abstract class Expression extends Node {
  protected Expression(SourcePosition pos) {
    super(pos);
  }

  public ExpressionNode box() {
    return new ExpressionNode(this);
  }

  public abstract Type getType();
}

abstract class BinaryOpExpr extends Expression {
  public ExpressionNode left, right;

  protected BinaryOpExpr(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(pos);
    this.left = left;
    this.right = right;
  }

  public abstract String getOpString();
}

abstract class UnaryOpExpr extends Expression {
  public ExpressionNode right;

  protected UnaryOpExpr(ExpressionNode right, SourcePosition pos) {
    super(pos);
    this.right = right;
  }

  public abstract String getOpString();
}

abstract class Literal extends Expression {
  public Literal(SourcePosition pos) {
    super(pos);
  }

  public abstract long getValueLong();
}

