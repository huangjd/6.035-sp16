package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Return extends Statement {

  public Function context;
  public ExpressionNode value;

  public Return(Function node, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = null;

    if (node != null) {
      if (node.returnType != Type.NONE) {
        throw new TypeException(node.returnType, Type.NONE, pos);
      }
    }
    hashCache = node.hashCode();
  }

  public Return(Function node, ExpressionNode value, SourcePosition pos) {
    super(pos);
    this.context = node;
    this.value = value;

    if (!value.getType().isPrimitive()) {
      throw new TypeException(value, false);
    }

    if (node != null) {
      if (node.returnType != value.getType()) {
        throw new TypeException(node.returnType, value.getType(), pos);
      }
    }

    hashCache = node.hashCode() + 43 * value.hashCode();
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
