package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Ne extends BinaryOpExpr {

  private static int hashMask = 0b11001001000100010110111101101100;

  public Ne(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = (left.hashCode() + hashMask) ^ (right.hashCode() + hashMask);
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " != " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "!=";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
