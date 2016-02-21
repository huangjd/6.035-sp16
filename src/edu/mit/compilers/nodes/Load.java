package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Load extends Expression {

  private String arrayName;
  private ExpressionNode index;
  public Load(String arrayName, ExpressionNode node, SourcePosition pos) {
    super(pos);
    this.arrayName = arrayName;
    this.index = node;
    hashCache = arrayName.hashCode() * 13 + node.hashCode();
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return  arrayName + "[" + node.toString() + "]";
  }

  @Override
  public Type getType() {
    // TODO 
    return null;
  }
}
