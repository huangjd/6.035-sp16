package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.codegen.CFG.CFGDesc;

public abstract class BasicBlockVisitor<T extends Mergable<T>>
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

  public void traverse(CFG cfg) {
    for (CFGDesc b : cfg) {
      reset();
      traverse(b.entry);
    }
  }

  public void reverseTraverse(CFG cfg) {
    for (CFGDesc b : cfg) {
      reset();
      traverse(b.exits);
    }
  }

  void traverse(PriorityQueue<QueueNode> queue, boolean direction) {
    QueueNode node;
    while ((node = queue.poll()) != null) {
      BasicBlock b = node.b;
      Data oldData = get(b);

      T oldIn, newIn;
      if (oldData != null) {
        oldIn = oldData.in;
        newIn = oldIn.merge(node.in);
      } else {
        oldIn = null;
        newIn = node.in;
      }

      if (!newIn.equals(oldIn)) {
        T newOut = visit(b, newIn);
        put(b, new Data(newIn, newOut));

        if (direction) {
          if (b.taken != null) {
            queue.add(new QueueNode(b.taken, newOut));
          }
          if (b.notTaken != null) {
            queue.add(new QueueNode(b.notTaken, newOut));
          }
        } else {
          for (BasicBlock predecessor : b.comefroms) {
            queue.add(new QueueNode(predecessor, newIn));
          }
        }
      }
    }
  }

  public void traverse(BasicBlock entry) {
    PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>(new Comparator<QueueNode>() {
      @Override
      public int compare(QueueNode a, QueueNode b) {
        return Integer.compare(a.b.priority, b.b.priority);
      }
    });

    init(entry);
    queue.add(new QueueNode(entry, getInitValue()));
    QueueNode node;

    traverse(queue, true);

    fini(entry);
  }

  public void traverse(HashSet<BasicBlock> exits) {
    PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>(new Comparator<QueueNode>() {
      @Override
      public int compare(QueueNode a, QueueNode b) {
        return Integer.compare(b.b.priority, a.b.priority);
      }
    });

    init(exits);
    for (BasicBlock exit : exits) {
      queue.add(new QueueNode(exit, getInitValue()));
    }
    QueueNode node;
    traverse(queue, false);

    fini(exits);
  }

  public void init(BasicBlock entry) {
  }

  public void init(HashSet<BasicBlock> exits) {
    for (BasicBlock b : exits) {
      init(b);
    }
  }

  public void fini(BasicBlock entry) {
  }

  public void fini(HashSet<BasicBlock> exits) {
    for (BasicBlock b : exits) {
      fini(b);
    }
  }

  public abstract T getInitValue();

  public abstract T visit(BasicBlock b, T in);

  public void reset() {
    // Optional operation for each function BB
  }
}
