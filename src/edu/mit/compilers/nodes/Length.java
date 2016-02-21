package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Length extends Expression {
  private String variableName;
  public Length (String variableName, SourcePosition pos) {
    super(pos);
    hashCache = variableName.hashCode() + 0b10001011101010101;

  } 

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "@" + variableName.toString();
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
