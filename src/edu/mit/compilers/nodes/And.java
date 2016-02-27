package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class And extends BinaryOpExpr {

  public And(ExpressionNode left, ExpressionNode right, SourcePosition pos) {
    super(left, right, pos);
    hashCache = left.hashCode() * 127 + right.hashCode();
    if (left.getType() != Type.BOOLEAN || right.getType() != Type.BOOLEAN) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
    }
  }

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "(" + left.toString() + " && " + right.toString() + ")";
  }

  @Override
  public String getOpString() {
    return "&&";
  }

  @Override
  public Type getType() {
    return Type.BOOLEAN;
  }
}
