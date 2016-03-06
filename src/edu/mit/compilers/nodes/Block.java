package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Block extends Statement {

  public final SymbolTable localSymbolTable;
  public ArrayList<StatementNode> statements;

  public Block(SymbolTable symtab, ArrayList<StatementNode> statements, SourcePosition pos) {
    super(pos);
    this.localSymbolTable = symtab;
    this.statements = statements;

    if (symtab == null) {
      throw new NullPointerException();
    }

    hashCache = this.statements.hashCode();
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("{\n");
    for (StatementNode statement : statements) {
      stringBuilder.append(statement.toString());
    }
    stringBuilder.append("}");
    return stringBuilder.toString();
  }
}
