package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Store extends Statement {

  public final Var array;
  public final ExpressionNode index;
  public final ExpressionNode value;
  public boolean checkBounds = true;

  public Store(Var array, ExpressionNode index, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.array = array;
    this.index = index;
    this.value = value;
    hashCache = array.hashCode() * 11 + index.hashCode() * 37 + value.hashCode();

    if (!array.isArray()){
      throw new TypeException(array, false, pos);
    }
    if (index.getType() != Type.INT){
      throw new TypeException(index, Type.INT);
    }
    if (array.type.getElementType() != value.getType()) {
      throw new TypeException(value, array.type.getElementType());
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return array.id + "[" + index.toString() + "]" + " = " + value.toString() + ";\n";
  }
}
