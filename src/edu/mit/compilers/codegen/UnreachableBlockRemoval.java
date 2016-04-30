package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.codegen.CFG.CFGDesc;

public class UnreachableBlockRemoval extends BasicBlockAnalyzeTransformTraverser {

  HashSet<BasicBlock> reachableBlocks;
  CFGDesc desc;

  @Override
  public void analyze(BasicBlock b) {
    reachableBlocks.add(b);
  }

  @Override
  public void transform(BasicBlock b) {
    for (Iterator<BasicBlock> it = b.comefroms.iterator(); it.hasNext();) {
      BasicBlock temp = it.next();
      if (!reachableBlocks.contains(temp)) {
        it.remove();
      }
    }
  }

  @Override
  public void traverse(CFG cfg) {
    for (CFGDesc d : cfg) {
      reachableBlocks = new HashSet<>();
      desc = d;
      traverse(d.entry);
      for (Iterator<BasicBlock> it = d.exits.iterator(); it.hasNext();) {
        BasicBlock exit = it.next();
        if (!reachableBlocks.contains(exit)) {
          it.remove();
        }
      }
    }
  }
}
