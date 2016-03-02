package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class For extends Statement implements Breakable {

  public final Var loopVar;
  public final ExpressionNode init, end;
  public final long increment;
  public final StatementNode body;

  public For(Var loopVar, ExpressionNode init, ExpressionNode end, long increment, StatementNode body,
      SourcePosition pos) {
    super(pos);
    this.loopVar = loopVar;
    this.init = init;
    this.end = end;
    this.increment = increment;
    this.body = body;

    if (loopVar.type != Type.INT) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
      throw new TypeException(loopVar, Type.INT);
    }

    if (init.getType() != Type.INT) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
      throw new TypeException(init, Type.INT);
    }

    if (end.getType() != Type.INT) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
      throw new TypeException(end, Type.INT);
    }

    if (increment <= 0) {
      ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
      throw new IndexOutOfBoundsException(Long.toString(increment));
    }

    this.hashCache = loopVar.hashCode() + init.hashCode() * 3 + end.hashCode() * 5 +
        Long.hashCode(increment) * 7 + body.hashCode() * 11;
  }

  public For(Var loopVar, ExpressionNode init, ExpressionNode cond, StatementNode body, SourcePosition pos) {
    this(loopVar, init, cond, 1l, body, pos);
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "for (" + loopVar.id + " = " + init.toString() + ", " + end.toString() + ", " + Long.toString(increment)
        + ") " + body.toString() + "\n";
  }
}
