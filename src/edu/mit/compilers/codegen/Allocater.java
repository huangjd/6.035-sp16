package edu.mit.compilers.codegen;

import java.util.*;
import java.util.Map.Entry;

public class Allocater implements Allocator{

  Queue<Register> availableRegisters;
  Queue<Register> inuseRegisters;
  ArrayList<BasicBlock> basicBlocks;

  static final int WORD_SIZE = 8;

  public Allocater() {
    this.availableRegisters = new LinkedList<Register>();
    for (int i = 11; i <= 15; i++) {
      this.availableRegisters.add(new Register(i));
    }
    this.availableRegisters.add(new Register(5));
    this.inuseRegisters = new LinkedList<Register>();
  }

  public void recycleRegisters(HashMap<Integer, Integer> firstUse,
      HashMap<Integer, Integer> lastUse,
      int currentIndex) {
    Iterator<Entry<Integer, Integer>> it = firstUse.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("unchecked")
      Map.Entry<Integer, Integer> pair = it.next();
      Register current = new Register(pair.getKey());
      if (this.inuseRegisters.contains(current) && pair.getValue() > currentIndex) {
        this.inuseRegisters.remove(current);
      }
      it.remove(); // avoids a ConcurrentModificationException
    }

    it = lastUse.entrySet().iterator();
    while (it.hasNext()) {
      @SuppressWarnings("unchecked")
      Map.Entry<Integer, Integer> pair = it.next();
      Register current = new Register(pair.getKey());
      if (this.inuseRegisters.contains(current) && pair.getValue() < currentIndex) {
        this.inuseRegisters.remove(current);
      }
      it.remove(); // avoids a ConcurrentModificationException
    }
  }

  @Override
  public long transform(ArrayList<BasicBlock> basicblocks, long offset) {
    Integer stackCounter = 0;
    for (BasicBlock bb : basicblocks) {
      transformBasicBlock(bb, stackCounter);
    }
    return stackCounter;
  }

  public void transformBasicBlock(BasicBlock bb, Integer stackCounter) {

    ArrayList<Instruction> result = new ArrayList<Instruction>();

    // life time analysis of registers
    HashMap<Integer, Integer> firstUse = new HashMap<Integer, Integer> (); // <Register.id, index>
    HashMap<Integer, Integer> lastUse = new HashMap<Integer, Integer>();
    analyzeRegisterLifetime(bb.seq, firstUse, lastUse);


    // iterate through block and perform transformation passes
    for (int i = 0; i < bb.seq.size(); i++) {
      recycleRegisters(firstUse, lastUse, i); // at every step, check if registers can be recycled.
      Instruction current = bb.seq.get(i);

      // if we push and pop reg, current is now multiple instructions
      ArrayList<Instruction> currentList = new ArrayList<Instruction>();

      boolean isRaxUsed = false;

      //allocates destination register
      if (current.b != null && current.b.value instanceof Register) {
        ValueImpl tmp = registerPass((Register) current.b.value, stackCounter);
        if (tmp == null) { // MUST_REG and out of register
          currentList.addAll(convertInstructionByRestoringRegister(current, isRaxUsed));
          isRaxUsed = true;
        } else {
          Value newValue = new Value(tmp);
          current = new Instruction(current.op, current.a, newValue);
        }
      }

      //allocates current.a register
      if (current.a != null && current.a.value instanceof Register) {
        ValueImpl tmp = registerPass((Register) current.a.value, stackCounter);
        if (tmp == null) {
          currentList.addAll(convertInstructionByRestoringRegister(current, isRaxUsed));
        } else {
          Value newValue = new Value(tmp);
          current = new Instruction(current.op, newValue, current.b);
          currentList.add(current);
        }
      }

      ArrayList<Instruction> intermediate = new ArrayList<Instruction>();
      ArrayList<Instruction> intermediate2 = new ArrayList<Instruction>();
      ArrayList<Instruction> xxxConvert = new ArrayList<Instruction>();
      xxxConvert.addAll(currentList);

      //pass to convert set instructions
      for (Instruction instruction : xxxConvert) {
        if (instruction.op.toString().substring(3).equals("SET")) {
          intermediate.addAll(convertSetXxxInstruction(instruction));
        } else {
          intermediate.add(instruction);
        }
      }

      //pass to convert mem mem instructions
      for (Instruction instruction : intermediate) {
        if (instruction.b != null && instruction.a != null &&
            instruction.b.value instanceof Memory && instruction.a.value instanceof Memory) {
          intermediate2.addAll(convertInstructionByRestoringRegister(instruction, false));
        } else {
          intermediate2.add(instruction);
        }
      }
      result.addAll(intermediate2);
    }
    bb.seq = result;
  }


  // stackCounter = -1 if not MUST_STACK
  // TODO: check if having stackCounter as a field is okay and we can keep incrementing
  public ValueImpl registerPass(Register register, Integer stackCounter) {
    if (register.id > 128) {
      // virtual register
      if (register.hint == Register.MUST_REG) {
        if (!this.availableRegisters.isEmpty()) {
          Register tmp = this.availableRegisters.remove();
          this.inuseRegisters.add(tmp);
          return tmp;
        } else { // out of registers
          return null; // BAD CODE: null is a flag for the parent function to push and pop registers.
        }

      } else if (register.hint == Register.MUST_STACK) {
        Memory tmp = new Memory(Register.RSP.box(), null, stackCounter, WORD_SIZE);
        stackCounter += WORD_SIZE;
        return tmp;
      } else {
        return getAvailableRegisterOrMemory(stackCounter);
      }
    } else {
      // allocate the rest
      return getAvailableRegisterOrMemory(stackCounter);
    }
  }

  private ArrayList<Instruction> convertSetXxxInstruction(Instruction instruction){
    assert(instruction.op.toString().substring(3).equals("SET"));
    ArrayList<Instruction> result = new ArrayList<Instruction>();
    result.add(new Instruction(Opcode.XOR, instruction.a, instruction.a));
    result.add(instruction);
    return result;
  }

  private ArrayList<Instruction> convertInstructionByRestoringRegister(Instruction instruction, boolean isRaxUsed) {
    ArrayList<Instruction> result = new ArrayList<Instruction>();
    int spareRegister = isRaxUsed ? 10 : 0;
    Register rax = new Register(spareRegister); // 0 => %rax
    Value restore = new Value(rax);
    result.add(new Instruction(Opcode.MOV, instruction.a, restore));
    result.add(new Instruction(instruction.op, instruction.b, restore));
    result.add(new Instruction(Opcode.MOV, restore, instruction.b));
    return result;
  }

  private ValueImpl getAvailableRegisterOrMemory(Integer stackCounter) {
    if (!this.availableRegisters.isEmpty()) { // allocate register
      Register tmp = this.availableRegisters.remove();
      this.inuseRegisters.add(tmp);
      return tmp;
    } else { // out of registers
      Memory tmp = new Memory(Register.RSP.box(), null, stackCounter, WORD_SIZE);
      stackCounter += WORD_SIZE;
      return tmp;
    }
  }



  public void analyzeRegisterLifetime(ArrayList<Instruction> instructions,
      HashMap<Integer, Integer> firstUse,
      HashMap<Integer, Integer> lastUse) {

    for (int i = 0; i < instructions.size(); i++) {
      Instruction instruction = instructions.get(i);
      if (instruction.a != null && instruction.a.value instanceof Register) {
        recordUse((Register)instruction.a.value, i, firstUse);
      }
      if (instruction.b != null && instruction.b.value instanceof Register) {
        recordUse((Register)instruction.b.value, i, firstUse);
      }
      if (instruction.c != null && instruction.c.value instanceof Register) {
        recordUse((Register)instruction.c.value, i, firstUse);
      }
    }

    for (int i = instructions.size()-1; i >= 0; i--) {
      Instruction instruction = instructions.get(i);
      if (instruction.a != null && instruction.a.value instanceof Register) {
        recordUse((Register)instruction.a.value, i, lastUse);
      }
      if (instruction.b != null && instruction.b.value instanceof Register) {
        recordUse((Register)instruction.b.value, i, lastUse);
      }
      if (instruction.c != null && instruction.c.value instanceof Register) {
        recordUse((Register)instruction.c.value, i, lastUse);
      }
    }
  }

  private void recordUse(Register register, int index, HashMap<Integer, Integer> use) {
    if (!use.containsKey(register.id)) {
      use.put(register.id, index);
    }
  }















  // TODO: currently using Value to represent a variable. might not be right class.
  private boolean isLive(ArrayList<Instruction> instructions, int point, Value variable) {
    for (int i = point; i < instructions.size(); i++) {
      Instruction current = instructions.get(i);
      if (current.a != null && current.a.equals(variable)) {
        return true;
      }
      else if (current.b != null && current.b.equals(variable)) {
        return true;
      }
    }
    return false;
  }




  //public ArrayList<Instruction> flattenFunctions(ArrayList<ArrayList<BasicBlock>> functions) {
  //ArrayList<Instruction> flattenedInstructions = new ArrayList<Instruction>();
  //for (ArrayList<BasicBlock> basicBlocks : functions) {
  //	for (BasicBlock block : basicBlocks) {
  //		for (Instruction instruction : block.seq) {
  //			flattenedInstructions.add(instruction);
  //		}
  //	}
  //}
  //return flattenedInstructions;
  //}


}
