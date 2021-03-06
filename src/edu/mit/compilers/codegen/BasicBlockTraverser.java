package edu.mit.compilers.codegen;

public abstract class BasicBlockTraverser extends BasicBlockVisitor<BasicBlockTraverser.T> {

  public static class T implements Mergable<T> {
    public final static T t = new T();

    @Override
    public T merge(T t) {
      return t;
    }
  }

  @Override
  public T visit(BasicBlock b, T in) {
    visit(b);
    return in;
  }

  @Override
  public T getInitValue() {
    return T.t;
  }

  protected abstract void visit(BasicBlock b);
}
