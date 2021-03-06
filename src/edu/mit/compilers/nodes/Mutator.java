package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Mutator extends Visitor {
  protected Node returnNode;

  @Override
  public NodeProxy enter(NodeProxy node) {
    return node.accept(this);
  }

  @Override
  protected void visit(Program node) {
    SymbolTable globals = new SymbolTable();
    MethodTable methodTable = new MethodTable();
    Program p = new Program(methodTable, globals);

    boolean replace = false;

    for (StatementNode varDecl : node.varDecls) {
      StatementNode n = varDecl.accept(this);
      if (n != varDecl) {
        replace = true;
      }
      if (n != null) {
        p.varDecls.add(n);
      }
    }

    for (FunctionNode function : node.functions) {
      boolean main = false;
      if (function == node.main) {
        main = true;
      }

      FunctionNode f = function.accept(this);
      if (f != function) {
        replace = true;
      }
      if (f != null) {
        p.functions.add(f);
        if (main) {
          p.main = f;
        }
      }
    }

    if (replace) {
      returnNode = p;
    }
  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      StatementNode body = node.body.accept(this);
      if (body != node.body) {
        returnNode = new Function(node.id, node.returnType, node.nParams, node.localSymtab, body,
            node.getSourcePosition());
      }
    }
  }

  // ---------------- Expressions -----------
  @Override
  protected void visit(Add node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Add(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Sub node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Sub(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Mul node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Mul(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Div node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Div(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Mod node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Mod(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Lt node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Lt(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Le node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Le(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Ge node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Ge(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Gt node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Gt(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Eq node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Eq(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Ne node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Ne(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(And node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new And(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Or node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Or(left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Not node) {
    ExpressionNode right = node.right.accept(this);
    if (right != node.right) {
      returnNode = new Not(right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Minus node) {
    ExpressionNode right = node.right.accept(this);
    if (right != node.right) {
      returnNode = new Minus(right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Ternary node) {
    ExpressionNode cond = node.cond.accept(this);
    ExpressionNode left = node.trueExpr.accept(this);
    ExpressionNode right = node.falseExpr.accept(this);
    if (cond != node.cond || left != node.trueExpr || right != node.falseExpr) {
      returnNode = new Ternary(cond, left, right, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Call node) {
    ArrayList<ExpressionNode> expressions = new ArrayList<>();
    boolean replace = false;
    for (ExpressionNode arg : node.args) {
      ExpressionNode temp = arg.accept(this);
      if (temp != arg) {
        replace = true;
      }
      if (temp != null) {
        expressions.add(temp);
      }
    }
    if (replace) {
      returnNode = new Call(node.func, expressions, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Load node) {
    ExpressionNode index = node.index.accept(this);
    if (index != node.index) {
      returnNode = new Load(node.array, index, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
  }

  @Override
  protected void visit(VarExpr node) {
  }

  @Override
  protected void visit(Length node) {
  }

  @Override
  protected void visit(IntLiteral node) {
  }

  @Override
  protected void visit(BooleanLiteral node) {
  }

  @Override
  protected void visit(StringLiteral node) {
  }


  // ------------------- Statements -----------

  @Override
  protected void visit(Block node) {
    ArrayList<StatementNode> statements = new ArrayList<>();
    boolean replace = false;
    for (StatementNode statement : node.statements) {
      StatementNode temp = statement.accept(this);
      if (temp != statement) {
        replace = true;
      }
      if (temp != null) {
        statements.add(temp);
      }
    }
    if (replace) {
      returnNode = new Block(node.localSymbolTable, statements, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(CallStmt node) {
    returnNode = node.call;
    visit(node.call);
    if (returnNode != node.call) {
      if (returnNode instanceof Call) {
        returnNode = new CallStmt((Call) returnNode, node.getSourcePosition());
      }
    } else {
      returnNode = node;
    }
  }

  @Override
  protected void visit(Return node) {
    if (node.value != null) {
      ExpressionNode value = node.value.accept(this);
      if (value != node.value) {
        returnNode = new Return(node.context, value, node.getSourcePosition());
      }
    }
  }

  @Override
  protected void visit(If node) {
    ExpressionNode cond = node.cond.accept(this);
    StatementNode trueBlock = node.trueBlock.accept(this);
    StatementNode falseBlock = node.falseBlock.accept(this);
    if (cond != node.cond || trueBlock != node.trueBlock || falseBlock != node.falseBlock) {
      returnNode = new If(cond, trueBlock, falseBlock, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(For node) {
    ExpressionNode init = node.init.accept(this);
    ExpressionNode end = node.end.accept(this);
    StatementNode body = node.body.accept(this);
    if (init != node.init || end != node.end || body != node.body) {
      returnNode = new For(node.loopVar, init, end, node.increment, body, node.getSourcePosition());
    }
  };

  @Override
  protected void visit(While node) {
    ExpressionNode cond = node.cond.accept(this);
    StatementNode body = node.body.accept(this);
    if (cond != node.cond || body != node.body) {
      returnNode = new While(cond, body, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Assign node) {
    ExpressionNode value = node.value.accept(this);
    if (value != node.value) {
      returnNode =  new Assign(node.var, value, node.getSourcePosition());
    }
  }

  @Override
  protected void visit(Store node) {
    ExpressionNode index = node.index.accept(this);
    ExpressionNode value = node.value.accept(this);
    if (index != node.index || value != node.value) {
      returnNode = new Store(node.array, index, value, node.getSourcePosition(), node.cop);
    }
  }

  @Override
  protected void visit(Break node) {
  }

  @Override
  protected void visit(Continue node) {
  }

  @Override
  protected void visit(VarDecl node) {
  }

  @Override
  protected void visit(Pass node) {
  }

  @Override
  protected void visit(Die node) {
  }
}
