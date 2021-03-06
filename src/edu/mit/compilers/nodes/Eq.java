package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Eq extends BinaryOpExpr implements RelOp {

  private static int hashMask = 0b00011011100010000011100010111111;

  public Eq(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = (left.hashCode() + hashMask) ^ (right.hashCode() + hashMask);

    if (!left.getType().isPrimitive()) {
      throw new TypeException(left, false);
    }
    if (!right.getType().isPrimitive()) {
      throw new TypeException(right, false);
    }
    if (left.getType() != right.getType()) {
      throw new TypeException(left, right);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " == " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "==";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
