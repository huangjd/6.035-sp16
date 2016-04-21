package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.ArithOp.DivOp;

public class Div extends BinaryOpExpr implements DivOp {

  public Div(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = left.hashCode() * 43 + right.hashCode();

    if (left.getType() != Type.INT) {
      throw new TypeException(left, Type.INT);
    }
    if (right.getType() != Type.INT) {
      throw new TypeException(right, Type.INT);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " / " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "/";
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
