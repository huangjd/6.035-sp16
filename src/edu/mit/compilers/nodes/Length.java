package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Length extends Expression {
  public final Var array;
  public Length (Var array, SourcePosition pos) {
    super(pos);
    hashCache = array.hashCode() + 0b10001011101010101000101110101010;

    if (!array.isArray()) {
      throw new TypeException(array, true, pos);
    }
    this.array = array;
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "@" + array.toString();
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
