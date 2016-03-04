package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Load extends Expression {

  public Var array;
  public ExpressionNode index;

  public Load(Var array, ExpressionNode index, SourcePosition pos) {
    super(pos);
    this.array = array;
    this.index = index;
    hashCache = array.hashCode() * 13 + index.hashCode();
    if (!array.isArray()) {
      throw new TypeException(array, false, pos);
    }
    if (index.getType() != Type.INT){
      throw new TypeException(index,Type.INT);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return array.id + "[" + index.toString() + "]";
  }

  @Override
  public Type getType() {
    return array.type;
  }
}
