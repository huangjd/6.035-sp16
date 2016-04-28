package edu.mit.compilers.codegen;

import java.util.HashSet;

public abstract class BasicBlockAnalyzeTransformPass extends BasicBlockVisitor<BasicBlockAnalyzeTransformPass.State> {

  public static abstract class State implements Transformable<State> {
    @Override
    public abstract State transform(State t);

    @Override
    protected abstract State clone();
  }

  protected final class Transformer extends BasicBlockTraverser {
    @Override
    protected void visit(BasicBlock b) {
      transform(b);
    }
  }

  Transformer transformer = new Transformer();

  @Override
  public State visit(BasicBlock b, State in) {
    return analyze(b, in);
  }

  public abstract State analyze(BasicBlock b, State in);

  public abstract void transform(BasicBlock b);

  public void transformerReset() {
  }

  @Override
  public void reset() {
    super.reset();
    transformerReset();
  }

  @Override
  public void fini(BasicBlock entry) {
    transformer.visit(entry);
  }

  @Override
  public void fini(HashSet<BasicBlock> exits) {
    transformer.reverseTraverse(exits);
  }
}
