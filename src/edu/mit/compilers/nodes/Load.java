package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Load extends Expression {

  public Var array;
  public ExpressionNode index;

  public Load(Var array, ExpressionNode node, SourcePosition pos) {
    super(pos);
    this.array = array;
    this.index = node;
    hashCache = array.hashCode() * 13 + node.hashCode();
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
