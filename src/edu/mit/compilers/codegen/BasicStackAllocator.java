package edu.mit.compilers.codegen;

import java.util.HashMap;

import edu.mit.compilers.codegen.CFG.CFGDesc;
import edu.mit.compilers.codegen.Operand.Type;
import edu.mit.compilers.common.Util;

public class BasicStackAllocator extends BasicBlockTraverser {

  public static final int stage = 3;

  HashMap<Operand, Memory> relocationTable;
  int bssoffset;

  @Override
  protected void visit(BasicBlock b) {
    for (Instruction ins : b) {
      assert (!ins.op.pseudoOp() || ins.op.stage() >= stage);

      //Memory a, b1, dest;
      if (ins.op == Op.LOCAL_ARRAY_DECL) {
        assert (!relocationTable.containsKey(ins.dest));
        bssoffset = Util.roundUp(bssoffset, 16);
        Memory stack = new Memory(Register.orbp, -bssoffset, Type.r64);
        bssoffset += ((Imm64) ins.a).val * (ins.dest.getType() == Operand.Type.r64 ? 8 : 1);
        relocationTable.put(ins.dest, stack);
      }

      if (ins.a instanceof Array) {
        Memory stack = relocationTable.get(ins.a);
        assert (stack != null);
        ins.a = stack;
      }

      if (ins.b instanceof Value) {
        Value b2 = (Value) ins.b;
        Memory stack = relocationTable.get(b2);
        if (stack == null) {
          if (b2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundUp(bssoffset, 8);
          }
          stack = new Memory(Register.orbp, -bssoffset, b2.getType());
          if (b2.getType() == Operand.Type.r64) {
            bssoffset += 8;
          } else {
            bssoffset += 1;
          }
          relocationTable.put(b2, stack);
        }
        ins.b = stack;
      }
      if (ins.a instanceof Value) {
        Value a2 = (Value) ins.a;
        Memory stack = relocationTable.get(a2);
        if (stack == null) {
          if (a2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundUp(bssoffset, 8);
          }
          stack = new Memory(Register.orbp, -bssoffset, a2.getType());
          if (a2.getType() == Operand.Type.r64) {
            bssoffset += 8;
          } else {
            bssoffset += 1;
          }
          relocationTable.put(a2, stack);
        }
        ins.a = stack;
      }
      if (ins.dest instanceof Value && ins.dest != Value.dummy) {
        Value d2 = (Value) ins.dest;
        Memory stack = relocationTable.get(d2);
        if (stack == null) {
          if (d2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundUp(bssoffset, 8);
          }
          stack = new Memory(Register.orbp, -bssoffset, d2.getType());
          if (d2.getType() == Operand.Type.r64) {
            bssoffset += 8;
          } else {
            bssoffset += 1;
          }
          relocationTable.put(d2, stack);
        }
        ins.dest = stack;
      }
    }

  }

  @Override
  public void traverse(CFG cfg) {
    for (CFGDesc b : cfg) {
      relocationTable = new HashMap<>();
      bssoffset = 0;
      traverse(b.entry);
    }
  }
}
