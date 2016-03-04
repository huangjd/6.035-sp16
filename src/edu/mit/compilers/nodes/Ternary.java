package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Ternary extends Expression {

  public ExpressionNode cond, trueExpr, falseExpr;

  public Ternary(ExpressionNode cond, ExpressionNode trueExpr, ExpressionNode falseExpr, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.trueExpr = trueExpr;
    this.falseExpr = falseExpr;
    hashCache = (cond.hashCode() * 131 + trueExpr.hashCode()) * 133 + falseExpr.hashCode();
    if (cond.getType() != Type.BOOLEAN) {
      throw new TypeException(cond, Type.BOOLEAN);
    }
    if (!trueExpr.getType().isPrimitive()) {
      throw new TypeException(trueExpr, false);
    }
    if (!falseExpr.getType().isPrimitive()) {
      throw new TypeException(falseExpr, false);
    }
    if (trueExpr.getType() != falseExpr.getType()) {
      throw new TypeException(trueExpr, falseExpr);
    }
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + cond.toString() + " ? " + trueExpr.toString() + " : " + falseExpr + ")";
  }

  @Override
  public Type getType() {
    return trueExpr.getType();
  }
}