package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Function extends Node {

  public String id;
  public Type returnType;
  public boolean isCallout;
  public SymbolTable parameters;
  public StatementNode body;

  public Function(String name, SourcePosition pos) {
    super(pos);
    this.id = name;
    this.returnType = Type.INT;
    this.isCallout = true;
    this.parameters = new SymbolTable();
    this.body = null;
    this.hashCache = id.hashCode();
  }

  public Function(String name, Type returnType, SymbolTable parameters, StatementNode body, SourcePosition pos) {
    super(pos);
    this.id = name;
    this.returnType = returnType;
    this.isCallout = false;
    this.parameters = parameters;
    this.body = body;
    this.hashCache = id.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    if (isCallout) {
      return "callout " + id + ";\n";
    } else {
      String temp = "";
      ArrayList<Var> param = parameters.asList();
      for (Var var : param) {
        temp += (var.type.toString() + " " + var.id + ", ");
      }
      temp = temp.substring(0, temp.length() - 2);

      return returnType.toString() + " " + id + "(" + temp + ") " + body.toString() + "\n";
    }
  }

  public String getSignature() {
    if (isCallout) {
      return "<callout> int " + id + "(...);";
    } else {
      String temp = "";
      ArrayList<Var> param = parameters.asList();
      for (Var var : param) {
        temp += (var.type.toString() + ", ");
      }
      temp = temp.substring(0, temp.length() - 2);
      return returnType.toString() + " " + id + "(" + temp + ");";
    }
  }

  public FunctionNode box() {
    return new FunctionNode(this);
  }

  public String getName() {
    return id;
  }
}
