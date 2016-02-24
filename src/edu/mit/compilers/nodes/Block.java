package edu.mit.compilers.nodes;

import java.util.ArrayList;

import edu.mit.compilers.common.*;

public class Block extends Statement {

  public SymbolTable localSymbolTable;
  public ArrayList<StatementNode> statements;

  public Block(SourcePosition pos) {
    super(pos);
    statements = new ArrayList<>();
    hashCache = statements.hashCode();
  }

  public Block(ArrayList<StatementNode> statements, SourcePosition pos) {
    super(pos);
    this.statements = statements;
    hashCache = this.statements.hashCode();
  }

  public void addStatement(StatementNode node) {
    statements.add(node);
    hashCache = 31 * hashCache + node.hashCode();
  }

  public void addStatement(Statement node) {
    statements.add(node.box());
    hashCache = 31 * hashCache + node.hashCode();
  }

  public void addStatement(ArrayList<StatementNode> node) {
    statements.addAll(node);
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
