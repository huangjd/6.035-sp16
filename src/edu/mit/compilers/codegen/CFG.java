package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.*;

public class CFG extends ArrayList<CFG.CFGDesc> {
  ScopedMap<Var, Operand> symtab;
  HashMap<String, Operand> strtab;
  public String fileName;

  public static class CFGDesc {
    BasicBlock entry;
    HashSet<BasicBlock> exits;

    public CFGDesc(BasicBlock entry, HashSet<BasicBlock> exits) {
      this.entry = entry;
      this.exits = exits;
    }
  }

  public CFG(ArrayList<CFGDesc> functionEntries, ScopedMap<Var, Operand> symtab, HashMap<String, Operand> strtab) {
    super(functionEntries);
    this.symtab = symtab;
    this.strtab = strtab;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\t.file\t\"").append(fileName).append("\"\n");
    sb.append("\t.bss\n");

    for (Operand operand : symtab.values()) {
      assert (operand instanceof BSSObject);
      BSSObject object = (BSSObject) operand;

      if (object.isArray) {
        sb.append("\t.align\t16\n\t.type\t").append(object.symbol).append(", @object\n")
        .append(object.symbol).append(":\n\t.zero\t")
        .append(object.length * (object.type == Operand.Type.r8 ? 1 : 8)).append('\n');
      } else {
        if (object.type == Operand.Type.r8) {
          sb.append("\t.type\t").append(object.symbol).append(", @object\n")
          .append(object.symbol).append(":\n\t.zero\t1\n");
        } else {
          sb.append("\t.align\t8\n\t.type\t").append(object.symbol).append(", @object\n")
          .append(object.symbol).append(":\n\t.zero\t8\n");
        }
      }
    }
    sb.append("\t.section\t.rodata\n");

    ArrayList<Operand> a = new ArrayList<>(strtab.values());
    Collections.sort(a, new Comparator<Operand>() {

      @Override
      public int compare(Operand arg0, Operand arg1) {
        return ((StringObject) arg0).symbol.compareTo(((StringObject) arg1).symbol);
      }
    });
    for (Operand operand : a) {
      assert (operand instanceof StringObject);
      StringObject object = (StringObject) operand;
      sb.append(object.symbol).append(":\n\t.string\t\"").append(object.content).append("\"\n");
    }

    sb.append("\t.text\n\t.globl\tmain\n");
    for (CFGDesc desc : this) {
      sb.append("\t.type\t").append(desc.entry.label).append(", @function\n");
      sb.append(desc.entry.toString());
    }
    sb.append("\n");
    return sb.toString();
  }
}
