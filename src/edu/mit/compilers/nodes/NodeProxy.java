package edu.mit.compilers.nodes;

import edu.mit.compilers.common.Type;

public abstract class NodeProxy {
  Node node;

  protected NodeProxy(Node node) {
    this.node = node;
  }

  public void accept(Visitor visitor) {
    node.dispatch(visitor);
  }

  public abstract NodeProxy accept(Mutator mutator);

  @Override
  public String toString() {
    return node.toString();
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }
}

class ProgramNode extends NodeProxy {
  public ProgramNode(Program prog) {
    super(prog);
  }

  @Override
  public ProgramNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new ProgramNode((Program) temp);
  }
}

class FunctionNode extends NodeProxy {
  public FunctionNode(Function func) {
    super(func);
  }

  @Override
  public FunctionNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new FunctionNode((Function) temp);
  }
}

class StatementNode extends NodeProxy {
  public StatementNode(Statement stmt) {
    super(stmt);
  }

  @Override
  public StatementNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new StatementNode((Statement) temp);
  }
}

class ExpressionNode extends NodeProxy {
  public ExpressionNode(Expression expr) {
    super(expr);
  }

  @Override
  public ExpressionNode accept(Mutator mutator) {
    mutator.returnNode = node;
    node.dispatch(mutator);
    Node temp = mutator.returnNode;
    mutator.returnNode = null;
    return new ExpressionNode((Expression) temp);
  }

  Type getType() {
    return ((Expression) node).getType();
  }
}
