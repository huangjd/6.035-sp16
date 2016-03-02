package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Ternary extends Expression {

  public ExpressionNode cond, trueExpr, falseExpr;

  protected Ternary(ExpressionNode cond, ExpressionNode trueExpr, ExpressionNode falseExpr, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.trueExpr = trueExpr;
    this.falseExpr = falseExpr;
    hashCache = (cond.hashCode() * 131 + trueExpr.hashCode()) * 133 + falseExpr.hashCode();
    if (cond.getType() != Type.BOOLEAN) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
      throw new TypeException(cond, Type.BOOLEAN);
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