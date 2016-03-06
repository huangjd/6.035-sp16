package edu.mit.compilers.nodes;

import java.util.List;

import edu.mit.compilers.common.*;

public class Function extends Node {

  public final String id;
  public final Type returnType;
  public final boolean isCallout;
  public final SymbolTable localSymtab;
  public final StatementNode body;
  public final int nParams;

  public Function(String name, SourcePosition pos) {
    super(pos);
    this.id = name;
    this.returnType = Type.INT;
    this.isCallout = true;
    this.localSymtab = new SymbolTable();
    this.body = null;
    this.hashCache = id.hashCode();
    this.nParams = -1;
  }

  public Function(String name, Type returnType, int nParams, SymbolTable parameters, StatementNode body, SourcePosition pos) {
    super(pos);
    this.id = name;
    this.returnType = returnType;
    this.isCallout = false;
    this.localSymtab = parameters;
    this.body = body;
    this.hashCache = id.hashCode();
    this.nParams = nParams;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  public List<Var> getParams() {
    return localSymtab.asList().subList(0, nParams);
  }

  @Override
  public String toString() {
    if (isCallout) {
      return "callout " + id + ";\n";
    } else {
      String temp = "";
      for (Var var : getParams()) {
        temp += (var.type.toString() + " " + var.id + ", ");
      }
      try {
        temp = temp.substring(0, temp.length() - 2);
      } catch (StringIndexOutOfBoundsException e) {

      }

      return returnType.toString() + " " + id + "(" + temp + ") " + body.toString() + "\n";
    }
  }

  public String getSignature() {
    if (isCallout) {
      return "<callout> int " + id + "(...);";
    } else {
      String temp = "";
      for (Var var : getParams()) {
        temp += (var.type.toString() + ", ");
      }
      try {
        temp = temp.substring(0, temp.length() - 2);
      } catch (StringIndexOutOfBoundsException e) {

      }
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
