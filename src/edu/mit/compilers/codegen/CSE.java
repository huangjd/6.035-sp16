package edu.mit.compilers.codegen;

import java.util.HashMap;

public class CSE extends BasicBlockAnalyzeTransformPass {

  class State extends BasicBlockAnalyzeTransformPass.State {

    HashMap<Instruction, Instruction> table;

    State(HashMap<Instruction, Instruction> map) {
      this.table = map;
    }

    @Override
    public State transform(BasicBlockAnalyzeTransformPass.State t) {
      HashMap<Instruction, Instruction> newTable = new HashMap<Instruction, Instruction>();
      HashMap<Instruction, Instruction> table2 = ((State)t).table;
      for (Instruction ins : table.keySet()) {
        if (table2.get(ins) == null || table2.get(ins) == table.get(ins)) {
          newTable.put(ins, table.get(ins));
        }
      }
      for (Instruction ins : table2.keySet()) {
        if (table.get(ins) == null) {
          newTable.put(ins, table2.get(ins));
        }
      }
      return new State(newTable);
    }

    @Override
    protected State clone() {
      return new State((HashMap<Instruction, Instruction>) table.clone());
    }

    @Override
    public boolean equals(Object arg0) {
      if (!(arg0 instanceof State)) {
        return false;
      }
      HashMap<Instruction, Instruction> table2 = ((State)arg0).table;
      if (table.size() != table2.size()) {
        return false;
      }
      for (Instruction ins : table.keySet()) {
        if (table2.get(ins) != table.get(ins)) {
          return false;
        }
      }
      return true;
    }
  }

  @Override
  public State getInitValue() {
    return new State(new HashMap<Instruction, Instruction>());
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    HashMap<Instruction, Instruction> tableIn = ((State) in).table;
    HashMap<Instruction, Instruction> tableOut = (HashMap<Instruction, Instruction>) tableIn.clone();
    for (Instruction ins : b) {
      if (!ins.twoOperand) {
        switch (ins.op) {
        case ADD:
        case SUB:
        case IMUL:
        case XOR:
        case SAL:
        case SAR:

        }
      }
    }
    return new State(tableOut);
  }

  @Override
  public void transform(BasicBlock b) {
    // TODO Auto-generated method stub

  }


}



























