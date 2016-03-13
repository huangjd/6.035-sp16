package edu.mit.compilers.codegen;

import java.util.ArrayList;

public class BasicBlock {

  public ArrayList<Instruction> seq;

  public BasicBlock(ArrayList<Instruction> seq) {
    this.seq = seq;
  }

  public BasicBlock() {
    // TODO Auto-generated constructor stub
  }

}
