package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;
import com.sun.xml.internal.ws.org.objectweb.asm.Type;

import edu.mit.compilers.common.ErrorLogger;
import edu.mit.compilers.common.SourcePosition;

public class While extends Statement implements Breakable {

  public final ExpressionNode cond;
  public final StatementNode body;

  public While(ExpressionNode cond, StatementNode body, SourcePosition pos) {
    super(pos);
    this.cond = cond;
    this.body = body;
    this.hashCache = cond.hashCode() + body.hashCode() * 37;
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
    return "while (" + cond.toString() + ") " + body.toString() + "\n";
  }
}
