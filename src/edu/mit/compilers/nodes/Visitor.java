package edu.mit.compilers.nodes;

public class Visitor {

  public NodeProxy enter(NodeProxy node) {
    node.accept(this);
    return node;
  }

  public void visit(Program node) {
    for (FunctionNode function : node.functions) {
      function.accept(this);
    }
    if (node.main != null) {
      node.main.accept(this);
    }
    for (VarDecl varDecl : node.varDecls) {
      visit(varDecl);
    }
  }

  public void visit(Function node) {
    if (!node.isCallout) {
      visit(node.body);
    }
  }

  // ------------ Expressions ----------

  protected void visit(Add node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Sub node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Mul node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Div node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Mod node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Lt node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Le node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Ge node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Gt node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Eq node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Ne node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(And node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Or node) {
    node.left.accept(this);
    node.right.accept(this);
  }

  protected void visit(Not node) {
    node.right.accept(this);
  }

  protected void visit(Minus node) {
    node.right.accept(this);
  }

  protected void visit(Ternary node) {
    node.cond.accept(this);
    node.trueExpr.accept(this);
    node.falseExpr.accept(this);
  }

  protected void visit(Call node) {
    for (ExpressionNode e : node.args) {
      e.accept(this);
    }
  }

  protected void visit(Load node) {
    node.index.accept(this);
  }

  protected void visit(VarExpr node) {
  }

  protected void visit(Length node) {
  }

  protected void visit(IntLiteral node) {
  }

  protected void visit(BooleanLiteral node) {
  }

  protected void visit(StringLiteral node) {
  }
  
  protected void visit(IntLiteralUnparsed node) {
  	node.box().accept(this);
  }

  // ------------- Statements ----------------

  protected void visit(Block node) {
    for (StatementNode statement : node.statements) {
      statement.accept(this);
    }
  }

  protected void visit(CallStmt node) {
    visit(node.call);
  }

  protected void visit(ReturnStmt node) {
    if (node.value != null) {
      node.value.accept(this);
    }
  }

  protected void visit(IfStmt node) {
    node.cond.accept(this);
    node.trueBlock.accept(this);
    node.falseBlock.accept(this);
  }

  protected void visit(For node) {
    node.init.accept(this);
    node.end.accept(this);
    node.body.accept(this);
  }

  protected void visit(While node) {
    node.cond.accept(this);
    node.body.accept(this);
  }

  protected void visit(Assign node) {
    node.value.accept(this);
  }

  protected void visit(Store node) {
    node.index.accept(this);
    node.value.accept(this);
  }

  protected void visit(BreakStmt node) {
  }

  protected void visit(ContinueStmt node) {
  }

  protected void visit(VarDecl node) {
  }

  protected void visit(Pass node) {
  }
}
