package edu.mit.compilers.codegen;

import java.util.HashSet;

import edu.mit.compilers.codegen.CFG.CFGDesc;

public class Linearizer extends BasicBlockTraverser {

  BasicBlock huge = new BasicBlock();

  @Override
  protected void visit(BasicBlock b) {
    huge.add(new Instruction.HardCode(b.label));
    for (Instruction i : b) {
      huge.add(i);
    }
  }

  @Override
  public void traverse(CFG cfg) {
    super.traverse(cfg);

    cfg.clear();
    cfg.add(new CFGDesc(huge, new HashSet<BasicBlock>(){{add(huge);}}));
  }
}
