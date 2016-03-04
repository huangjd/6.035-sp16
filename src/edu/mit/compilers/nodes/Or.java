package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Or extends BinaryOpExpr {

  public Or(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = left.hashCode() * 8191 + right.hashCode();
    if (left.getType() != Type.BOOLEAN) {
      throw new TypeException(left, Type.BOOLEAN);
    }
    if (right.getType() != Type.BOOLEAN) {
      throw new TypeException(right, Type.BOOLEAN);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " || " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "||";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
