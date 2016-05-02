package edu.mit.compilers.codegen;

import java.util.HashMap;

public class CPPass extends BasicBlockVisitor{

  @Override
  public Mergable getInitValue() {
    return new TransformableCP(new HashMap<Operand, Imm64>());
  }

  @Override
  public Mergable visit(BasicBlock b, Mergable in) {
    HashMap<Operand, Imm64> varConstMap = new HashMap<Operand, Imm64>(((TransformableCP) in).varConstMap);
    for (Instruction instruction : b) {
      if (instruction.a instanceof Imm64 && instruction.op == Op.MOV && instruction.b == null) {
        varConstMap.put(instruction.dest, (Imm64) instruction.a);
      }
    }
    return new TransformableCP(varConstMap);
  }

  public BasicBlock finalPass(BasicBlock b, Mergable in) {
    HashMap<Operand, Imm64> varConstMap = new HashMap<Operand, Imm64>(((TransformableCP) in).varConstMap);
    BasicBlock modifiedBasicBlock = new BasicBlock();
    for (Instruction instruction : b) {
      if (varConstMap.containsKey(instruction.a)) {
        if (varConstMap.containsKey(instruction.b)) {
          modifiedBasicBlock.add(new Instruction(instruction.dest, instruction.op,varConstMap.get(instruction.a), varConstMap.get(instruction.b)));
        } else {
          modifiedBasicBlock.add(new Instruction(instruction.dest, instruction.op,varConstMap.get(instruction.a), instruction.b));
        }
      } else if (varConstMap.containsKey(instruction.b)) {
        modifiedBasicBlock.add(new Instruction(instruction.dest, instruction.op,instruction.a, varConstMap.get(instruction.b)));
      } else {
        if (instruction.a instanceof Imm64 && instruction.op == Op.MOV && instruction.b == null) {
          varConstMap.put(instruction.dest, (Imm64) instruction.a);
        }
      }
    }
    return modifiedBasicBlock;
  }

  public static class TransformableCP implements Mergable<TransformableCP> {

    private HashMap<Operand, Imm64> varConstMap;

    public TransformableCP(HashMap<Operand, Imm64> varConstMap) {
      this.varConstMap = varConstMap;
    }

    @Override
    public TransformableCP merge(TransformableCP t) {
      HashMap<Operand, Imm64> resultVarConstMap = new HashMap<Operand, Imm64>(t.varConstMap);
      resultVarConstMap.keySet().retainAll(varConstMap.keySet());
      return new TransformableCP(resultVarConstMap);
    }

  }

}
