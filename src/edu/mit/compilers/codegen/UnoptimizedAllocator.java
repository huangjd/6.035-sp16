package edu.mit.compilers.codegen;

import java.util.*;

class LinearViewEntry {
  Instruction inst;
  BasicBlock bb;
  int index;
  int availableRegs = 0b1111110000000000;
  int aLast = -1, bLast = -1;

  public LinearViewEntry(Instruction inst, BasicBlock bb, int index) {
    this.inst = inst;
    this.bb = bb;
    this.index = index;
  }
}

class LinearView extends ArrayList<LinearViewEntry> {
  public LinearView(ArrayList<BasicBlock> bbs) {
    HashMap<Register, Integer> last = new HashMap<Register, Integer>();

    for (BasicBlock bb : bbs) {
      for (int i = 0; i < bb.seq.size(); i++) {
        add(new LinearViewEntry(bb.seq.get(i), bb, i));
      }
    }
    for (int i = size() - 1; i >= 0; i--) {
      Instruction inst = get(i).inst;
      if (inst.a != null && inst.a.value instanceof Register && ((Register) inst.a.value).id > Register.VIRT_REG_START) {
        last.put((Register) inst.a.value, i);
      }
      if (inst.b != null && inst.b.value instanceof Register && ((Register) inst.b.value).id > Register.VIRT_REG_START) {
        last.put((Register) inst.b.value, i);
      }
    }
    for (LinearViewEntry entry : this) {
      Instruction inst = entry.inst;
      if (inst.a != null && inst.a.value instanceof Register && ((Register) inst.a.value).id > Register.VIRT_REG_START) {
        entry.aLast = last.get(inst.a.value);
      }
      if (inst.b != null && inst.b.value instanceof Register && ((Register) inst.b.value).id > Register.VIRT_REG_START) {
        entry.bLast = last.get(inst.b.value);
      }
    }
  }
}

public class UnoptimizedAllocator implements Allocator {

  @Override
  public long transform(ArrayList<BasicBlock> basicblocks, long offset) {
    LinearView linearView = new LinearView(basicblocks);
    for (LinearViewEntry entry : linearView) {
      Instruction inst = entry.inst;
      if (inst.a != null && inst.a.isVirtualReg()) {
        Memory mem = new Memory(Register.RBP.box(), null, -offset, 8);
        inst.a.value = mem;
        offset += 8;
      }
      if (inst.b != null && inst.b.isVirtualReg()) {
        Memory mem = new Memory(Register.RBP.box(), null, -offset, 8);
        inst.b.value = mem;
        offset += 8;
      }
      if (inst.c != null && inst.c.isVirtualReg()) {
        Memory mem = new Memory(Register.RBP.box(), null, -offset, 8);
        inst.c.value = mem;
        offset += 8;
      }
    }
    return offset;
  }
}

class LowerMemMem implements Allocator {

  @Override
  public long transform(ArrayList<BasicBlock> basicblocks, long offset) {
    LinearView linearView = new LinearView(basicblocks);
    for (int i = linearView.size() - 1; i >= 0; i--) {
      LinearViewEntry entry = linearView.get(i);
      Instruction inst = entry.inst;
      if (inst.a != null && inst.a.value instanceof Memory &&
          inst.b != null && inst.b.value instanceof Memory) {
        entry.bb.seq.set(entry.index, new Instruction(inst.op, Register.RAX.box(), inst.b));
        entry.bb.seq.add(entry.index, new Instruction(Opcode.MOV, inst.a, Register.RAX.box()));
      }
    }
    return offset;
  }

}
