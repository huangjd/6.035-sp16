package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock {

  public ArrayList<Instruction> seq;
  public String label;
  public BasicBlock positive, negative, zero;
  public BasicBlockInfo info;

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
    if (positive != null) {
      if (negative != null) {
        if (zero != null) {
          sb.append("\tjg\t").append(positive.label).append('\n');
          sb.append("\tjl\t").append(negative.label).append('\n');
          sb.append("\tjz\t").append(zero.label).append('\n');
        } else {
          sb.append("\tjnz\t").append(positive.label).append('\n');
          sb.append("\tjz\t").append(negative.label).append('\n');
        }
      } else {
        sb.append("\tjmp\t").append(negative.label).append('\n');
      }
    }
    return sb.toString();
  }
}

