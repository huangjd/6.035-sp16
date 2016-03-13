package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock {

  public ArrayList<Instruction> seq;



  public BasicBlock() {
    seq = new ArrayList<>();
  }

  public void add(Instruction inst) {
    seq.add(inst);
  }

}
