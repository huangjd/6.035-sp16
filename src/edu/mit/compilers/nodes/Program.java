package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Program extends Node {

  public MethodTable methodTable;
  public SymbolTable globals;
  public ArrayList<VarDecl> varDecls;
  public ArrayList<FunctionNode> functions;
  public FunctionNode main;

  public Program(MethodTable methodTable, SymbolTable globals) {
    super(new SourcePosition());
    this.methodTable = methodTable;
    this.globals = globals;
    this.varDecls = new ArrayList<>();
    this.functions = new ArrayList<>();
    this.main = null;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (FunctionNode function : functions) {
      Function f = (Function) function.node;
      if (f.isCallout) {
        stringBuilder.append(f.toString());
      }
    }

    for (VarDecl varDecl : varDecls) {
      stringBuilder.append(varDecl.toString());
    }

    for (FunctionNode function : functions) {
      Function f = (Function) function.node;
      if (!f.isCallout) {
        stringBuilder.append(f.toString());
      }
    }

    if (main != null) {
      stringBuilder.append(main.toString());
    }
    return stringBuilder.toString();
  }
}