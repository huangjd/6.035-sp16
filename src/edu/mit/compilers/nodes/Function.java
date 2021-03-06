package edu.mit.compilers.nodes;

import java.util.*;

import edu.mit.compilers.common.*;

public class Function extends Node {

  public String id;
  public final Type returnType;
  public final boolean isCallout;
  public final SymbolTable localSymtab;
  public final StatementNode body;
  public final int nParams;
  public int stackFrameReserve = 0;

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
    ArrayList<Var> a = new ArrayList<Var>(localSymtab.asList().subList(0, nParams));
    Collections.reverse(a);
    return a;
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

  public String getMangledName() {
    if (!isCallout && (id.equals("printf") || id.equals("exit"))) {
      return "_F." + id;
    } else {
      return id;
    }
  }
  /*
    if (isCallout || id.equals("main")) {
      return id;
    } else {
      String res = "_Z" + String.valueOf(id.length()) + id;
      if (getParams().size() == 0) {
        res += "v";
      } else {
        for (Var v : getParams()) {
          switch (v.type) {
          case INT:
            res += "l";
            break;
          case BOOLEAN:
            res += "b";
            break;
          }
        }
      }
      return res;
    }
  }*/

  public FunctionNode box() {
    return new FunctionNode(this);
  }

  public String getName() {
    return id;
  }
}
