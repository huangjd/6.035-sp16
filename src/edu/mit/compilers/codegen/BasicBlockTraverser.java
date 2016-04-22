package edu.mit.compilers.codegen;

public abstract class BasicBlockTraverser extends BasicBlockVisitor<BasicBlockTraverser.T> {

  public class T implements Transformable<T> {
    @Override
    public T transform(T t) {
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
    return new T();
  }

  protected abstract void visit(BasicBlock b);
}
