package edu.mit.compilers.codegen;

import java.util.ArrayList;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;

public class MidEnd extends Visitor {

  ArrayList<BasicBlock> functions = new ArrayList<>();
  IRBuilder builder = new IRBuilder();

  public ScopedMap<Var, Value> symtab;

  public MidEnd() {

  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      BasicBlock bb = builder.createBasicBlock(node.id);
      functions.add(bb);

      switch () {

      }

    }
  }
}
