package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class BooleanLiteral extends Literal {

  public final boolean value;

  public BooleanLiteral(boolean v, SourcePosition pos) {
    super(pos);
    value = v;
    hashCache = value ? 1 : 0;
  }

  @Override
  public String toString() {
    return Boolean.toString(value);
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  public boolean getValue() {
    return value;
  }

  @Override
  public long getValueLong() {
    if (value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}