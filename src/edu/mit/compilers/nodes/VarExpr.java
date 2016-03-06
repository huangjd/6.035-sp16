package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class VarExpr extends Expression {

  public final Var var;

  public VarExpr(Var var, SourcePosition pos) {
    super(pos);
    this.var = var;

    if (!var.isPrimitive()) {
      throw new TypeException(var, false, pos);
    }

    this.hashCache = var.hashCode();
  }

  @Override
  public Type getType() {
    return var.type;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return var.id;
  }
}
