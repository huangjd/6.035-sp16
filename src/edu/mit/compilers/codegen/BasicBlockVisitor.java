package edu.mit.compilers.codegen;

import java.util.*;

public abstract class BasicBlockVisitor<T extends Transformable<T>>
extends HashMap<BasicBlock, BasicBlockVisitor<T>.Data> {

  public class Data {
    T in;
    T out;

    Data(T in, T out) {
      this.in = in;
      this.out = out;
    }
  }

  class QueueNode {
    final BasicBlock b;
    final T in;

    public QueueNode(BasicBlock b, T in) {
      this.b = b;
      this.in = in;
    }
  }

  PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>(new Comparator<QueueNode>() {
    @Override
    public int compare(QueueNode a, QueueNode b) {
      return Integer.compare(a.b.priority, b.b.priority);
    }
  });

  public void traverse(BasicBlock entry, T init) {
    queue.add(new QueueNode(entry, init));
    QueueNode node;

    while ((node = queue.poll()) != null) {
      BasicBlock b = node.b;
      Data oldData = get(b);

      T oldIn, newIn;
      if (oldData != null) {
        oldIn = oldData.in;
        newIn = oldIn.transform(node.in);
      } else {
        oldIn = null;
        newIn = node.in;
      }

      if (!newIn.equals(oldIn)) {
        T newOut = visit(b, newIn);
        put(b, new Data(newIn, newOut));

        if (b.taken != null) {
          queue.add(new QueueNode(b.taken, newOut));
        }
        if (b.notTaken != null) {
          queue.add(new QueueNode(b.notTaken, newOut));
        }
      }
    }
  }

  public void reverseTraverse(HashSet<BasicBlock> exits, T init) {
    for (BasicBlock exit : exits) {
      queue.add(new QueueNode(exit, init));
    }
    QueueNode node;

    while ((node = queue.poll()) != null) {
      BasicBlock b = node.b;
      Data oldData = get(b);

      T oldOut, newOut;
      if (oldData != null) {
        oldOut = oldData.out;
        newOut = oldOut.transform(node.in);
      } else {
        oldOut = null;
        newOut = node.in;
      }

      if (!newOut.equals(oldOut)) {
        T newIn = visit(b, newOut);
        put(b, new Data(newIn, newOut));

        for (BasicBlock predecessor : b.comefroms) {
          queue.add(new QueueNode(predecessor, newIn));
        }
      }
    }
  }

  public abstract T visit(BasicBlock b, T in);
}
