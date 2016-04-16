package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Return extends Statement {

  public final FunctionNode context;
  public final ExpressionNode value;

  public Return(FunctionNode node, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = null;

    if (((Function) (node.node)).returnType != Type.NONE) {
      throw new TypeException(((Function) (node.node)).returnType, Type.NONE, pos);
    }

    hashCache = node.hashCode();
  }

  public Return(FunctionNode node, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = value;

    if (!value.getType().isPrimitive()) {
      throw new TypeException(value, false);
    }

    if (!((Function) (node.node)).id.equals("main") && value.getType() == Type.INT) {
      if (((Function) (node.node)).returnType != value.getType()) {
        throw new TypeException(value, ((Function) (node.node)).returnType);
      }
    }

    hashCache = node.hashCode() + value.hashCode() * 37;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "return" + (value != null ? " " + value.toString() : "") + ";\n";
  }
}
