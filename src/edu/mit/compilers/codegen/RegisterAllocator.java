package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.codegen.CFG.CFGDesc;
import edu.mit.compilers.codegen.Instruction.*;
import edu.mit.compilers.common.Util;

public class RegisterAllocator extends BasicBlockTraverser {

  HashMap<Value, Integer> heuristic;
  boolean omitrbp;
  CFG cfg;

  public RegisterAllocator(boolean omitrbp) {
    this.omitrbp = omitrbp;
  }

  @Override
  public void traverse(CFG cfg) {
    this.cfg = cfg;
    super.traverse(cfg);
  }

  void add(Value val, int i) {
    Integer key = heuristic.get(val);
    if (key != null) {
      heuristic.put(val, key + i);
    } else {
      heuristic.put(val, i);
    }
  }

  @Override
  protected void visit(BasicBlock b) {
    for (Instruction ins : b) {
      if (ins.op == Op.LOOP_START) {
        if (b.taken != null && b.taken.notTaken != null) {
          add((Value) ins.dest, 3 * b.taken.notTaken.size());
        } else {
          add((Value) ins.dest, 10);
        }
      }
      if (ins.dest instanceof Value && ins.dest != Value.dummy) {
        if (!heuristic.containsKey(ins.dest)) {
          heuristic.put((Value) ins.dest, 0);
        }
      }

      for (Operand op : ins.getReadOperand()) {
        if (op instanceof Value && op != Value.dummy) {
          add((Value) op, 1);
          if (op.equals(ins.dest)) {
            add((Value) op, 3);
          }
        }
      }
    }
  }

  @Override
  public void reset() {
    super.reset();
    heuristic = new HashMap<Value, Integer>();
  }

  static int or(int[] vals) {
    int res = 0;
    for (int val : vals) {
      if (val >= 0) {
        res |= 1 << val;
      }
    }
    return res;
  }

  class Validator extends BasicBlockAnalyzeTransformPass {
    boolean ok;
    Value replacee;
    Register replacer;

    class State extends BasicBlockAnalyzeTransformPass.State {
      int inuse;

      State(int val) {
        inuse = val;
      }

      @Override
      public State merge(BasicBlockAnalyzeTransformPass.State t) {
        return new State(inuse | ((State) t).inuse);
      }

      @Override
      protected State clone() {
        return new State(inuse);
      }

      @Override
      public boolean equals(Object arg0) {
        return arg0 instanceof State && inuse == ((State) arg0).inuse;
      }
    }

    Validator(Value value, int regIndex) {
      ok = true;
      replacee = value;
      replacer = Register.regs[regIndex];
    }

    @Override
    public State getInitValue() {
      return new State(0);
    }

    @Override
    public State analyze(BasicBlock b, BasicBlockAnalyzeTransformPass.State in) {
      int inuse = ((State) in).inuse;
      for (int i = b.size() - 1; i >= 0; i--) {
        Instruction ins = b.get(i);
        Operand[] opRead = ins.getReadOperand();
        Operand[] opWrite = ins.getDestOperand();
        Register[] regRead = ins.getRegRead();
        Register[] regWrite = ins.getRegWrite();
        int readset = or(Register.regsToIndices(regRead));
        int writeset = or(Register.regsToIndices(regWrite));

        if ((inuse & (1 << 16)) != 0) {
          for (Register reg : regWrite) {
            if (reg.equals(replacer)) {
              ok = false;
              break;
            }
          }
        }

        inuse &= ~(writeset);

        if (Util.in(opWrite, replacee)) {
          inuse &= ~(1 << 16);
        }

        inuse |= readset;

        for (Operand op : opRead) {
          if (op.equals(replacee) && ((1 << replacer.id) & inuse) != 0) {
            ok = false;
            break;
          }
        }
        for (Operand op : opWrite) {
          if (op.equals(replacee) && ((1 << replacer.id) & (writeset | inuse)) != 0) {
            ok = false;
            break;
          }
        }



        if (ins instanceof DivInstruction) {
          if (replacer.equals(ins.dest) && replacee.equals(ins.dest2()) ||
              replacee.equals(ins.dest) && replacer.equals(ins.dest2())) {
            ok = false;
            break;
          }
        }

        if (Util.in(opRead, replacee)) {
          inuse |= 1 << 16;
        }

      }
      return new State(inuse);
    }

    @Override
    public void transform(BasicBlock b) {
      for (Instruction ins : b) {
        if (ins.dest != null && ins.dest.equals(replacee)) {
          ins.dest = replacer;
        }
        if (ins.dest2() != null && ins.dest2().equals(replacee)) {
          ((DivInstruction) ins).dest2 = replacer;
        }
        if (ins.a != null && ins.a.equals(replacee)) {
          ins.a = replacer;
        }
        if (ins.b != null && ins.b.equals(replacee)) {
          ins.b = replacer;
        }
        if (ins instanceof CallInstruction) {
          CallInstruction call = (CallInstruction) ins;
          for (int i = 0; i < call.args.size(); i++) {
            if (call.args.get(i).equals(replacee)) {
              call.args.set(i, replacer);
            }
          }
        }
      }
    }

    @Override
    public void traverse(HashSet<BasicBlock> exits) {
      PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>(exits.size(), new Comparator<QueueNode>() {
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

      while ((node = queue.poll()) != null) {
        BasicBlock b = node.b;
        Data oldData = get(b);

        State oldOut, newOut;
        if (oldData != null) {
          oldOut = (State) oldData.out;
          newOut = oldOut.merge(node.in);
        } else {
          oldOut = null;
          newOut = (State) node.in;
        }

        if (!newOut.equals(oldOut)) {
          State newIn = (State) visit(b, newOut);
          if (!ok) {
            return;
          }
          put(b, new Data(newIn, newOut));

          for (BasicBlock predecessor : b.comefroms) {
            queue.add(new QueueNode(predecessor, newIn));
          }
        }
      }
      fini(exits);
    }
  }

  @Override
  public void fini(BasicBlock entry) {
    ArrayList<SimpleEntry<Value, Integer>> values0 = new ArrayList<>();
    for (Value val : heuristic.keySet()) {
      values0.add(new SimpleEntry<Value, Integer>(val, heuristic.get(val)));
    }
    Collections.sort(values0, new Comparator<SimpleEntry<Value, Integer>>() {
      @Override
      public int compare(SimpleEntry<Value, Integer> arg0, SimpleEntry<Value, Integer> arg1) {
        return arg1.getValue().compareTo(arg0.getValue());
      }
    });
    ArrayList<Value> values = new ArrayList<>();
    for (SimpleEntry<Value, Integer> temp : values0) {
      values.add(temp.getKey());
    }

    HashMap<Value, Register> renameTable = new HashMap<>();
    for (Value value : values) {
      int[] avail;
      if (omitrbp) {
        avail = new int[]{Register.r11.id, Register.r10.id, Register.r9.id, Register.r8.id, Register.rcx.id,
            Register.rdx.id, Register.rsi.id, Register.rdi.id, Register.rbp.id, Register.rbx.id, Register.r12.id,
            Register.r13.id, Register.r14.id, Register.r15.id};
      } else {
        avail = new int[]{Register.r11.id, Register.r10.id, Register.r9.id, Register.r8.id, Register.rcx.id,
            Register.rdx.id, Register.rsi.id, Register.rdi.id, Register.rbx.id, Register.r12.id, Register.r13.id,
            Register.r14.id, Register.r15.id};
      }

      for (int i : avail) {
        Validator validator = new Validator(value, i);
        for (CFGDesc desc : cfg) {
          if (desc.entry == entry) {
            validator.traverse(desc.exits);
            break;
          }
        }
        if (validator.ok) {
          renameTable.put(value, Register.regs[i]);
          break;
        }

      }
    }
    HashSet<Register> usedRegs = new HashSet<>(renameTable.values());
    for (Register usedReg : usedRegs) {
      if ((Register.calleeSavedRegs & (1 << usedReg.id)) != 0) {
        Value saveReg = new Value();
        for (int i = 0; i < entry.size(); i++) {
          if (entry.get(i).op == Op.PROLOGUE) {
            entry.add(++i, new Instruction(Op.MOV, usedReg, saveReg));
            break;
          }
        }
        for (CFGDesc desc : cfg) {
          if (desc.entry == entry) {
            for (BasicBlock exit : desc.exits) {
              for (int i = exit.size() - 1; i >= 0; i--) {
                if (exit.get(i).op == Op.EPILOGUE) {
                  exit.add(i, new Instruction(Op.MOV, saveReg, usedReg));
                  break;
                }
              }
            }
            break;
          }
        }
      }
    }
  }
}
