package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Add extends BinaryOpExpr {

  private static int hashMask = 0b10001000010010111101010110101011;

  public Add(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = (left.hashCode() + hashMask) ^ (right.hashCode() + hashMask);
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " + " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "+";
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
