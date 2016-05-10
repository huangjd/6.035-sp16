package edu.mit.compilers.codegen;

public class Derange extends BasicBlockAnalyzeTransformPass {

  public static class State extends BasicBlockAnalyzeTransformPass.State {
    final int val;

    State(int val) {
      this.val = val;
    }

    @Override
    public State merge(BasicBlockAnalyzeTransformPass.State t) {
      return new State(val | ((State) t).val);
    }

    @Override
    protected State clone() {
      return new State(val);
    }

    @Override
    public boolean equals(Object arg0) {
      return arg0 instanceof State && val == ((State) arg0).val;
    }
  }

  @Override
  public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void transform(BasicBlock b) {
    // TODO Auto-generated method stub

  }

  @Override
  public State getInitValue() {
    // TODO Auto-generated method stub
    return null;
  }
}
