package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class UnparsedIntLiteral extends Expression {

  public final String value;

  public UnparsedIntLiteral(String v, SourcePosition pos) {
    super(pos);
    value = v;
    hashCache = v.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}