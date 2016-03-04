package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Call extends Expression {

  public Function func;
  public ArrayList<ExpressionNode> args;

  public Call(Function func, ArrayList<ExpressionNode> args, SourcePosition pos) {
    super(pos);
    this.func = func;
    this.args = args;

    ArrayList<Var> parameters = func.parameters.asList();

    if (!func.isCallout) {
      if (parameters.size() != args.size()) {
        throw new ArgumentsException(parameters.size(), args.size(), pos);
      }

      for (int i = 0; i < parameters.size(); i++) {
        if (parameters.get(i).type != args.get(i).getType()) {
          throw new TypeException(args.get(i), parameters.get(i).type);
        }
      }
    }

    hashCache = func.getName().hashCode() * 991 + args.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder();
    for (ExpressionNode expr : args) {
      s.append(expr.toString());
      s.append(", ");
    }
    String str = s.toString();
    str = str.substring(0, str.length() - 2);

    return func.getName() + "(" + str + ")";
  }

  @Override
  public Type getType() {
    return func.returnType;
  }
}
