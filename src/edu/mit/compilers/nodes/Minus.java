package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Minus extends UnaryOpExpr {

  protected Minus(ExpressionNode right, SourcePosition pos) {
    super(right, pos);
    hashCache = right.hashCode() + 0b11010001000111010011110101011011;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "-" + right.toString();
  }

  @Override
  public String getOpString() {
    return "-";
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
