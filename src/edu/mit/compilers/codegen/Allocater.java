package edu.mit.compilers.codegen;

import java.util.*;
import java.util.Map.Entry;

public class Allocater {

	Queue<Register> availableRegisters;
	Queue<Register> inuseRegisters;
	ArrayList<BasicBlock> basicBlocks;
	int stackCounter = 0;
	static final int WORD_SIZE = 8;
	
	private Allocater instance = new Allocater();

	private Allocater() {
		this.availableRegisters = new LinkedList<Register>();
		for (int i = 10; i <= 15; i++) {
			this.availableRegisters.add(new Register(i));
		}
		this.availableRegisters.add(new Register(5));
		this.inuseRegisters = new LinkedList<Register>();
	}
	
	public Allocater getInstance() {
		return this.instance;
	}

	public void transformFunctions(ArrayList<ArrayList<BasicBlock>> functions) {
		for (ArrayList<BasicBlock> blocks : functions){
			for (BasicBlock bb : blocks) {
				transformBasicBlock(bb);
			}
		}
	}
	
	public void recycleRegisters(HashMap<Integer, Integer> firstUse, 
															 HashMap<Integer, Integer> lastUse, 
															 int currentIndex) {
		Iterator<Entry<Integer, Integer>> it = firstUse.entrySet().iterator();
		while (it.hasNext()) {
      @SuppressWarnings("unchecked")
			Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
      Register current = new Register(pair.getKey());
      if (this.inuseRegisters.contains(current) && pair.getValue() > currentIndex) {
      	this.inuseRegisters.remove(current);
      }
      it.remove(); // avoids a ConcurrentModificationException
		}
		
		it = lastUse.entrySet().iterator();
		while (it.hasNext()) {
      @SuppressWarnings("unchecked")
			Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)it.next();
      Register current = new Register(pair.getKey());
      if (this.inuseRegisters.contains(current) && pair.getValue() < currentIndex) {
      	this.inuseRegisters.remove(current);
      }
      it.remove(); // avoids a ConcurrentModificationException
		}
	}
	
	
	public void transformBasicBlock(BasicBlock bb) {
		
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
			
			//allocates destination register
			if (current.dest != null && current.dest.value instanceof Register) {
				ValueImpl tmp = registerPass((Register) current.dest.value);
				if (tmp == null) { // MUST_REG and out of register
					currentList.addAll(convertInstructionByRestoringRegister(current));
				} else {
					Value newValue = new Value(tmp);
					current = new Instruction(newValue, current.op, current.a);
				}			
			}
			
			//allocates current.a register
			if (current.a != null && current.a.value instanceof Register) {
				ValueImpl tmp = registerPass((Register) current.a.value);
				if (tmp == null) {
					currentList.addAll(convertInstructionByRestoringRegister(current));
				} else {
					Value newValue = new Value(tmp);
					current = new Instruction(current.dest, current.op, newValue);
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
				if (instruction.dest != null && instruction.a != null && 
						instruction.dest.value instanceof Memory && instruction.a.value instanceof Memory) {
					intermediate2.addAll(convertInstructionByRestoringRegister(instruction));
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
	public ValueImpl registerPass(Register register) { 
		if (register.id > 128) {
			// virtual register
			if (register.hint == "MUST_REG")) {
				if (!this.availableRegisters.isEmpty()) {
					Register tmp = this.availableRegisters.remove();
					this.inuseRegisters.add(tmp);
					return tmp;
				} else { // out of registers
					return null; // BAD CODE: null is a flag for the parent function to push and pop registers.
				}
				
			} else if (register.hint == "MUST_STACK") {
				Memory tmp = new Memory(Register.RSP, null, stackCounter, WORD_SIZE);
				this.stackCounter += WORD_SIZE;
				return tmp;
			} else {
				return getAvailableRegisterOrMemory();
			}
		} else {
			// allocate the rest
			return getAvailableRegisterOrMemory();
		}
	}
	
	private ArrayList<Instruction> convertSetXxxInstruction(Instruction instruction){
		assert(instruction.op.toString().substring(3).equals("SET"));
		ArrayList<Instruction> result = new ArrayList<Instruction>();
		result.add(new Instruction(instruction.dest, Opcode.XOR, instruction.a, instruction.a));
		result.add(instruction);
		return result;
	}

	private ArrayList<Instruction> convertInstructionByRestoringRegister(Instruction instruction) {
		ArrayList<Instruction> result = new ArrayList<Instruction>();
		Register rax = new Register(0); // 0 => %rax
		Value restore = new Value(rax);
		result.add(new Instruction(restore, Opcode.MOV, instruction.a));
		result.add(new Instruction(restore, instruction.op, instruction.b));
		result.add(new Instruction(instruction.b, Opcode.MOV, restore));
		return result;
	}
	
	private ValueImpl getAvailableRegisterOrMemory() {
		if (!this.availableRegisters.isEmpty()) { // allocate register
			Register tmp = this.availableRegisters.remove();
			this.inuseRegisters.add(tmp);
			return tmp;
		} else { // out of registers
			Memory tmp = new Memory(Register.RSP, null, stackCounter, WORD_SIZE);
			this.stackCounter += WORD_SIZE;
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
			else if (current.dest != null && current.dest.equals(variable)) {
				return false;
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
