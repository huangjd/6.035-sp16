package edu.mit.compilers.codegen;

import java.util.HashMap;

import edu.mit.compilers.codegen.Instruction.*;
import edu.mit.compilers.codegen.Operand.Type;
import edu.mit.compilers.common.Util;

public class BasicStackAllocator extends BasicBlockTraverser {

  public static final int stage = 3;

  HashMap<Operand, Memory> relocationTable;
  int bssoffset;

  @Override
  protected void visit(BasicBlock b) {
    for (Instruction ins : b) {
      assert (ins.twoOperand || ins.op.stage() >= stage);

      //Memory a, b1, dest;
      if (ins.op == Op.LOCAL_ARRAY_DECL) {
        assert (!relocationTable.containsKey(ins.dest));
        bssoffset -= ((Imm64) ins.a).val * (ins.dest.getType() == Operand.Type.r64 ? 8 : 1);
        bssoffset = Util.roundDown(bssoffset, 16);
        Memory stack = new Memory(Register.orbp, bssoffset, Type.r64);
        relocationTable.put(ins.dest, stack);
        ins.dest = stack;
      }

      if (ins.a instanceof Array) {
        Memory stack = relocationTable.get(ins.a);
        assert (stack != null);
        ins.a = stack;
      }

      if (ins.op == Op.FAKE_CALL) {
        CallInstruction call = (CallInstruction) ins;
        for (int i = 0; i < call.args.size(); i++) {
          if (call.args.get(i) instanceof Value) {
            Memory stack = relocationTable.get(call.args.get(i));
            assert(stack != null);
            call.args.set(i, stack);
          }
        }
      }

      if (ins.b instanceof Value) {
        Value b2 = (Value) ins.b;
        Memory stack = relocationTable.get(b2);
        if (stack == null) {
          if (b2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundDown(bssoffset, 8);
          }
          if (b2.getType() == Operand.Type.r64) {
            bssoffset -= 8;
          } else {
            bssoffset -= 8;
          }
          stack = new Memory(Register.orbp, bssoffset, b2.getType());
          relocationTable.put(b2, stack);
        }
        ins.b = stack;
      }
      if (ins.a instanceof Value) {
        Value a2 = (Value) ins.a;
        Memory stack = relocationTable.get(a2);
        if (stack == null) {
          if (a2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundDown(bssoffset, 8);
          }
          if (a2.getType() == Operand.Type.r64) {
            bssoffset -= 8;
          } else {
            bssoffset -= 8;
          }
          stack = new Memory(Register.orbp, bssoffset, a2.getType());
          relocationTable.put(a2, stack);
        }
        ins.a = stack;
      }
      if (ins.dest instanceof Value && ins.dest != Value.dummy) {
        Value d2 = (Value) ins.dest;
        Memory stack = relocationTable.get(d2);
        if (stack == null) {
          if (d2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundDown(bssoffset, 8);
          }
          if (d2.getType() == Operand.Type.r64) {
            bssoffset -= 8;
          } else {
            bssoffset -= 8;
          }
          stack = new Memory(Register.orbp, bssoffset, d2.getType());
          relocationTable.put(d2, stack);
        }
        ins.dest = stack;
      }
      if (ins.dest2() instanceof Value && ins.dest2() != Value.dummy) {
        Value d2 = (Value) ins.dest2();
        Memory stack = relocationTable.get(d2);
        if (stack ==null) {
          if (d2.getType() == Operand.Type.r64) {
            bssoffset = Util.roundDown(bssoffset, 8);
          }
          if (d2.getType() == Operand.Type.r64) {
            bssoffset -= 8;
          } else {
            bssoffset -= 8;
          }
          stack = new Memory(Register.orbp, bssoffset, d2.getType());
          relocationTable.put(d2, stack);
        }
        ((DivInstruction) ins).dest2 = stack;
      }
    }
  }

  @Override
  public void reset() {
    relocationTable = new HashMap<>();
    bssoffset = 0;
  }
}
