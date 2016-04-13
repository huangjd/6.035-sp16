package edu.mit.compilers.codegen;

import java.util.HashMap;

abstract class BasicBlockInfo<T extends BasicBlockInfo<T>> {
  abstract protected void update(BasicBlock b);

  @Override
  abstract protected T clone() throws CloneNotSupportedException;

  public T apply(BasicBlock b) {
    try {
      T copy = this.clone();
      copy.update(b);
      b.info = copy;
      return copy;
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException();
    }
  }
}

public class BasicBlockVisitor<T extends BasicBlockInfo<T>> {

  public T init;

  HashMap<BasicBlock, T> map = new HashMap<BasicBlock, T>();

  public BasicBlockVisitor(BasicBlock b, T initInfo) {
    init = initInfo;
    visit(b, initInfo);
  }

  protected void visit(BasicBlock b, T info) {
    if (map.get(b) != null) {
      T newinfo = info.apply(b);
      map.put(b, newinfo);
      if (b.positive != null) {
        visit(b.positive, newinfo);
      }
      if (b.negative != null) {
        visit(b.negative, newinfo);
      }
      if (b.zero != null) {
        visit(b.zero, newinfo);
      }
    }
  }

  public T query(BasicBlock b) {
    return map.get(b);
  }
}
