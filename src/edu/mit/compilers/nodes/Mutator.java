package edu.mit.compilers.nodes;

public class Mutator extends Visitor {
  Node returnNode;

  @Override
  public NodeProxy enter(NodeProxy node) {
    return node.accept(this);
  }

  @Override
  protected void visit(Add node) {
    ExpressionNode left = node.left.accept(this);
    ExpressionNode right = node.right.accept(this);
    if (left != node.left || right != node.right) {
      returnNode = new Add(left, right, node.getSourcePosition());
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
  protected void visit(IntLiteral node) {
  }

  @Override
  protected void visit(BooleanLiteral node) {
  }

  @Override
  protected void visit(StringLiteral node) {
  }

  @Override
  protected void visit(Pass node) {
  }
}
