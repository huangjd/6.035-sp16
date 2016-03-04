package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class If extends Statement {

  public final ExpressionNode cond;
  public final StatementNode trueBlock, falseBlock;

  public If(ExpressionNode cond, StatementNode trueBlock, StatementNode falseBlock, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.trueBlock = trueBlock;
    this.falseBlock = falseBlock;
    this.hashCache = cond.hashCode() + trueBlock.hashCode() * 17 + falseBlock.hashCode() * 19;
    if (cond.getType() != Type.BOOLEAN) {
      throw new TypeException(cond, Type.BOOLEAN);
    }
  }

  public If(ExpressionNode cond, StatementNode trueBlock, SourcePosition pos) {
    this(cond, trueBlock, new Pass(null).box(), pos);
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "if (" + cond.toString() + ") " + trueBlock.toString() + " else " + falseBlock.toString() + "\n";
  }
}
