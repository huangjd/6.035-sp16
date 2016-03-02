package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class VarDecl extends Statement {

  public final Var var;

  public VarDecl(Var var, SourcePosition pos) {
    super(pos);

    if (!var.isVariable()) {
      throw new TypeException(var);
    }

    this.var = var;
    this.hashCache = var.hashCode() + 2;
  }

  public VarDecl(SymbolTable symtab, Var var, SourcePosition pos) {
    super(pos);

    if (!var.isVariable()) {
      throw new TypeException(var);
    }

    if (!symtab.insert(var)) {
      Var conflictingVar = symtab.lookup(var.id);
      if (conflictingVar != null) {
        throw new RedeclaredSymbolException(conflictingVar, var);
      } else {
        throw new IllegalStateException("This theoretically shouldn't happen");
      }
    }

    this.var = var;
    this.hashCache = var.hashCode() + 2;
  }

  @Override
  void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    if (var.isPrimitive()) {
      return var.type.toString() + " " + var.id + ";\n";
    } else if (var.isArray()){
      return var.type.getElementType().toString() + " " + var.id + "[" + Integer.toString(var.length) + "];\n";
    } else {
      throw new IllegalStateException("This theoretically shouldn't happen");
    }
  }
}
