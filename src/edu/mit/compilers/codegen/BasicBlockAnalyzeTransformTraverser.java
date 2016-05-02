package edu.mit.compilers.codegen;

public abstract class BasicBlockAnalyzeTransformTraverser extends BasicBlockAnalyzeTransformPass {

  public static class State extends BasicBlockAnalyzeTransformPass.State {
    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      return (State) t;
    }

    @Override
    protected State clone() {
      return this;
    }

    @Override
    public boolean equals(Object arg0) {
      return super.equals(arg0);
    }
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    analyze(b);
    return (State) in;
  }

  public abstract void analyze(BasicBlock b);

  @Override
  public abstract void transform(BasicBlock b);

  @Override
  public State getInitValue() {
    return new State();
  }
}
