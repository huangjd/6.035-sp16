package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.*;

public class CFG extends ArrayList<BasicBlock> {
  ScopedMap<Var, Operand> symtab;
  HashMap<String, Operand> strtab;

  public CFG(ArrayList<BasicBlock> functionEntries, ScopedMap<Var, Operand> symtab, HashMap<String, Operand> strtab) {
    super(functionEntries);
    this.symtab = symtab;
    this.strtab = strtab;
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
  }
}
