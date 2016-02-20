package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Le extends BinaryOpExpr {

  public Le(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = left.hashCode() * 103 + right.hashCode();
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " <= " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "<=";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
