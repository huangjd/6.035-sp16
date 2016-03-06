package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class StringLiteral extends Expression {

  public final String value;

  public StringLiteral(String string, SourcePosition pos) {
    super(pos);
    value = string;
    hashCache = string.hashCode();
  }

  @Override
  public String toString() {
    return value;
  }

  public String getValue() {
    return value;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public Type getType() {
    return Type.STRING;
  }
}
