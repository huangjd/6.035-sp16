package edu.mit.compilers.codegen;

import java.util.*;

public abstract class BasicBlockVisitor<T> extends HashMap<BasicBlock, BasicBlockVisitor<T>.Data> {

  public class Data {
    T in;
    T out;
    BasicBlock comefrom;
  }

  PriorityQueue<BasicBlock> queue = new PriorityQueue<BasicBlock>(new Comparator<BasicBlock>() {
    @Override
    public int compare(BasicBlock a, BasicBlock b) {
      return Integer.compare(a.priority, b.priority);
    }
  });

  public void traverse(BasicBlock b, T init) {
    queue.add(b);
    while ((b = queue.poll()) != null) {
      if (b.attachedVisitors.contains(this)) {

      } else {

        if (b.taken != null) {
          queue.add(b.taken);
        }
        if (b.notTaken != null) {
          queue.add(b.notTaken);
        }
      }
    }
  }

  public void detach() {
    for (BasicBlock b : this.keySet()) {
      b.attachedVisitors.remove(this);
    }
  }

  public void enqueue(BasicBlock b) {
    queue.add(b);
  }

  public abstract T visit(BasicBlock b, T in);

  public abstract T revisit(BasicBlock b, T in);
}
