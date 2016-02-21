package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Store extends Expression {

  private String arrayName;
  private ExpressionNode index;
  private ExpressionNode value;

  public Store (String arrayName, ExpressionNode index, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.arrayName = arrayName;
    this.index = index;
    this.value = value;
    hashCache = arrayName.hashCode() * 13 + index.hashCode() * 31 + value.hashCode();
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return  arrayName + "[" + index.toString() + "]" + " = " + value.toString();
  }

  @Override
  public Type getType() {
    // TODO 
    return null;
  }
}
