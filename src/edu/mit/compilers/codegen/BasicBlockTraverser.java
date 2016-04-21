package edu.mit.compilers.codegen;

public abstract class BasicBlockTraverser extends BasicBlockVisitor<BasicBlockTraverser.T> {

  public class T implements Transformable<T> {
    @Override
    public T transform(T t) {
      return t;
    }
  }

  public void traverse(BasicBlock entry) {
    super.traverse(entry, new T());
  }
}
