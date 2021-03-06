package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class While extends Statement implements Breakable {

  public final ExpressionNode cond;
  public final StatementNode body;

  public While(ExpressionNode cond, StatementNode body, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.body = body;
    this.hashCache = cond.hashCode() + body.hashCode() * 37;
    if (cond.getType() != Type.BOOLEAN) {
      throw new TypeException(cond, Type.BOOLEAN);
    }
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "while (" + cond.toString() + ") " + body.toString() + "\n";
  }
}
