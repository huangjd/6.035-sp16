package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Assign extends Statement {

  public Var var;
  public ExpressionNode value;

  public Assign(Var var, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.var = var;
    this.value = value;
    hashCache = var.hashCode() * 37 + value.hashCode();

    if (!var.isPrimitive()) {
      throw new TypeException(var, false, pos);
    }
    if (!value.getType().isPrimitive()) {
      throw new TypeException(value, false);
    }
    if (var.type != value.getType()) {
      throw new TypeException(var, value);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return var.id + " = " + value.toString() + ";\n";
  }
}
