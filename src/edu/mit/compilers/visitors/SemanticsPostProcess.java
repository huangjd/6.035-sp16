package edu.mit.compilers.visitors;

import java.util.ArrayList;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;

public final class SemanticsPostProcess extends Mutator {

  Breakable currentBreakable = null;
  public boolean ok = true;

  Program p;

  @Override
  protected void visit(Program node) {
    p = node;
    super.visit(node);
    node = (Program) returnNode;
    ArrayList<Var> vars = node.globals.asList();
    for (Var var : vars) {
      var.bss = true;
    }

    for (FunctionNode function : ((Program) returnNode).functions) {
      Function func = (Function) (function.getNode());
      if (func.id.equals("main") && func.getParams().size() == 0 && func.returnType == Type.NONE && !func.isCallout) {
        ((Program) returnNode).main = function;
      }
    }

    if (((Program) returnNode).main != null) {
      return;
    }

    ErrorLogger.logError(new GeneralException("'main()' function is not declared", null));

    ok = false;
  }

  @Override
  protected void visit(Return node) {
    if (((Function) (node.context.getNode())).id == "main") {
      returnNode = new Return(node.context, new IntLiteral(0, null).box(), node.getSourcePosition());
    } else {
      super.visit(node);
      Function func = ((Function) (node.context.getNode()));
      if (func.returnType == Type.NONE) {
        if (node.value != null) {
          ErrorLogger.logError(new GeneralException("returning a value from a void function", null));
        }
      } else {
        if (node.value.getType() != func.returnType) {
          ErrorLogger.logError(new TypeException(func.returnType, node.value.getType(), null));
        }
      }
    }
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
      returnNode = new Break(currentBreakable, node.getSourcePosition());
    } else {
      ErrorLogger.logError(new InvalidBreakStatementException(false, node.getSourcePosition()));
      ok = false;
    }
  }

  @Override
  protected void visit(Minus node) {
    if (node.right.getNode() instanceof UnparsedIntLiteral) {
      UnparsedIntLiteral i = (UnparsedIntLiteral) (node.right.getNode());
      try {
        if (i.value.length() > 2 && i.value.charAt(0) == '0' && i.value.charAt(1) == 'x') {
          returnNode = new IntLiteral(Long.parseLong("-" + i.value.substring(2), 16), node.getSourcePosition());
        } else {
          returnNode = new IntLiteral(Long.parseLong("-" + i.value), node.getSourcePosition());
        }
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
      if (node.value.length() > 2 && node.value.charAt(0) == '0' && node.value.charAt(1) == 'x') {
        returnNode = new IntLiteral(Long.parseLong(node.value.substring(2), 16), node.getSourcePosition());
      } else {
        returnNode = new IntLiteral(Long.parseLong(node.value), node.getSourcePosition());
      }
    } catch (NumberFormatException e) {
      ErrorLogger.logError(new GeneralException(
          "int literal \"" + node.value + "\" is out of range", node.getSourcePosition()));
    }
  }
}
