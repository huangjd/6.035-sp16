package edu.mit.compilers.visitors;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;

public final class SemanticsPostProcess extends Mutator {

  Breakable currentBreakable = null;
  public boolean ok = true;

  @Override
  protected void visit(Program node) {
    super.visit(node);

    for (FunctionNode function : node.functions) {
      Function func = (Function) (function.getNode());
      if (func.id.equals("main") && func.getParams().size() == 0) {
        node.main = function;
        node.functions.remove(node.main);
        returnNode = node;
        return;
      }
    }

    ErrorLogger.logError(new GeneralException("'main()' function is not declared", null));
    ok = false;
  }

  @Override
  protected void visit(For node) {
    Breakable temp = currentBreakable;
    currentBreakable = node;
    super.visit(node);
    currentBreakable = temp;
  }

  @Override
  protected void visit(While node) {
    Breakable temp = currentBreakable;
    currentBreakable = node;
    super.visit(node);
    currentBreakable = temp;
  }

  @Override
  protected void visit(Continue node) {
    if (currentBreakable != null) {
      returnNode = new Continue(currentBreakable, node.getSourcePosition());
    } else {
      ErrorLogger.logError(new InvalidBreakStatementException(true, node.getSourcePosition()));
      ok = false;
    }
  }

  @Override
  protected void visit(Break node) {
    if (currentBreakable != null) {
      returnNode = new Continue(currentBreakable, node.getSourcePosition());
    } else {
      ErrorLogger.logError(new InvalidBreakStatementException(false, node.getSourcePosition()));
      ok = false;
    }
  }

  @Override
  protected void visit(Minus node) {
    if (node.right.getNode() instanceof UnparsedIntLiteral) {
      try {
        long value = Long.parseLong("-" + ((UnparsedIntLiteral) (node.right.getNode())).value);
        returnNode = new IntLiteral(value, node.right.getSourcePosition());
      } catch (NumberFormatException e) {
        ErrorLogger.logError(new GeneralException(
            "int literal \"" + ((UnparsedIntLiteral) (node.right.getNode())).value + "\" is out of range",
            node.right.getSourcePosition()));
      }
    } else {
      super.visit(node);
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    try {
      returnNode = new IntLiteral(Long.parseLong(node.value), node.getSourcePosition());
    } catch (NumberFormatException e) {
      ErrorLogger.logError(new GeneralException(
          "int literal \"" + node.value + "\" is out of range", node.getSourcePosition()));
    }
  }
}
