package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Ge extends BinaryOpExpr {

  public Ge(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = left.hashCode() * 107 + right.hashCode();
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " >= " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return ">=";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
