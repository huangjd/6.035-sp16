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
      throw new TypeException(loopVar, Type.INT, pos);
    }
    if (init.getType() != Type.INT) {
      throw new TypeException(init, Type.INT);
    }
    if (end.getType() != Type.INT) {
      throw new TypeException(end, Type.INT);
    }

    if (increment <= 0) {
      throw new BoundsException(increment, new Long(0), null, pos);
    }

    this.hashCache = loopVar.hashCode() + init.hashCode() * 3 + end.hashCode() * 5 +
        (int) increment * 7 + body.hashCode() * 11;
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
