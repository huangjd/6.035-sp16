package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Store extends Statement {

  public Var array;
  public ExpressionNode index;
  public ExpressionNode value;

  public Store(Var array, ExpressionNode index, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.array = array;
    this.index = index;
    this.value = value;
    hashCache = array.hashCode() * 11 + index.hashCode() * 37 + value.hashCode();
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
