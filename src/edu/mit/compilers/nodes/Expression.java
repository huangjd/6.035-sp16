package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.Node;

public abstract class Expression extends Node {
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
