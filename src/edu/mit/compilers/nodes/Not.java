package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Not extends UnaryOpExpr {

  protected Not(ExpressionNode right, SourcePosition pos) {
    super(right, pos);
    hashCache = right.hashCode() + 0b10011101000111101011101110001010;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "!" + right.toString();
  }

  @Override
  public String getOpString() {
    return "!";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
