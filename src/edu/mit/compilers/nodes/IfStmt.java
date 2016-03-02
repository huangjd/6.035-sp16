package edu.mit.compilers.nodes;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import edu.mit.compilers.common.ErrorLogger;
import edu.mit.compilers.common.ErrorType;
import edu.mit.compilers.common.SourcePosition;
import edu.mit.compilers.common.TypeException;

public class IfStmt extends Statement {

  public final ExpressionNode cond;
  public final StatementNode trueBlock, falseBlock;

  public IfStmt(ExpressionNode cond, StatementNode trueBlock, StatementNode falseBlock, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.trueBlock = trueBlock;
    this.falseBlock = falseBlock;
    this.hashCache = cond.hashCode() + trueBlock.hashCode() * 17 + falseBlock.hashCode() * 19;
    if (cond.getType() != Type.BOOLEAN) {
        ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
        throw new TypeException(cond, Type.BOOLEAN);
      }
  }

  public IfStmt(ExpressionNode cond, StatementNode trueBlock, SourcePosition pos) {
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
