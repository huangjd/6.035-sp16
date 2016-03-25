package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock {

  public ArrayList<Instruction> seq;
  public String label;

  public BasicBlock(String label) {
    seq = new ArrayList<>();
    this.label = label;
  }

  public void add(Instruction inst) {
    seq.add(inst);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(label).append(":\n");
    for (Instruction ins : seq) {
      sb.append("\t").append(ins.toString()).append('\n');
    }
    return sb.toString();
  }

}
