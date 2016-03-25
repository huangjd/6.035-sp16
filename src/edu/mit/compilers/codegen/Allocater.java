package edu.mit.compilers.codegen;

import java.util.*;

public class Allocater {
	
	Queue<Register> availableRegisters;
	Queue<Register> inuseRegisters;
	
	
	public Allocater() {
		availableRegisters = new LinkedList<Register>();
		for (int i = 0; i < 128; i++) {
			availableRegisters.add(new Register(i));
		}
		inuseRegisters = new LinkedList<Register>();
	}
	
	public ArrayList<Instruction> flattenFunctions(ArrayList<ArrayList<BasicBlock>> functions) {
		ArrayList<Instruction> flattenedInstructions = new ArrayList<Instruction>();
		for (ArrayList<BasicBlock> basicBlocks : functions) {
			for (BasicBlock block : basicBlocks) {
				for (Instruction instruction : block.seq) {
					flattenedInstructions.add(instruction);
				}
			}
		}
		return flattenedInstructions;
	}
	
	public ArrayList<Instruction> convertSetXxxInstruction(Instruction instruction){
		assert(instruction.op.toString().substring(3).equals("SET"));
		ArrayList<Instruction> result = new ArrayList<Instruction>();
		result.add(new Instruction(instruction.dest, Opcode.XOR, instruction.a, instruction.a));
		result.add(instruction);
		return result;
	}
	
	// 
	public ArrayList<Instruction> convertByRestoringRegisters(Instruction instruction) {
		assert(instruction.a.value instanceof Memory && instruction.b.value instanceof Memory);
		ArrayList<Instruction> result = new ArrayList<Instruction>();
		
		if (!availableRegisters.isEmpty()) { 
			// if we have registers available, just use one and replace MEM, MEM instruction
			Register tmp = availableRegisters.remove();
			Value restore = new Value(tmp);
			result.add(new Instruction(restore, Opcode.MOV, instruction.a));
			result.add(new Instruction(restore, instruction.op, instruction.b));
			
		} else {
			/*   Otherwise perform following transformation
			 * 
			 * 	 Mov %r10, mem3
	   			 mov mem1, %r10
	         op %r10, mem2
   		     mov mem3, %r10
			 * */
			Value tmpStack = new Value(new Memory(Register.RSP, null, 0, 8));
			Register tmp = inuseRegisters.remove();
			Value restore = new Value(tmp);
			result.add(new Instruction(tmpStack, Opcode.MOV, restore));
			result.add(new Instruction(restore, Opcode.MOV, instruction.a));
			result.add(new Instruction(instruction.b, instruction.op, restore));
			result.add(new Instruction(restore, Opcode.MOV, tmpStack));
			inuseRegisters.add(tmp);
		}
		return result;
	}
	
	/*
	 * add dest, reg1, reg2 =>
	 * 
	 * mov dest, reg1 
	 * add dest, reg2 
	 * */
	public ArrayList<Instruction> convertThreeAddrInstructionToIsa(Instruction instruction) {
		assert(instruction.isa);
		assert(instruction.a != null && instruction.b != null); // check instruction has two operands
		ArrayList<Instruction> result = new ArrayList<Instruction>();
		result.add(new Instruction(Opcode.MOV, instruction.dest, instruction.a));
		result.add(new Instruction(instruction.op, instruction.dest, instruction.b));
		return result;
	}
	
	public ValueImpl allocatePhysicalMemory(Register register) {
		// TODO check allocate available register if register.id >= 128??
		
		if (!availableRegisters.isEmpty()) {
			return availableRegisters.remove();
		} else if (register.hint.equals("MUST_REG")){
			for (Register reg : inuseRegisters) {
				if (reg)
			}
		} else {
			return new Memory(Register.RSP, null, 0, 8); // TODO check offset
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
		
//		int usageIndex = instructions.size();
//		int definitionIndex = instructions.size();
//		for (int i = point; i < instructions.size(); i++) {
//			Instruction current = instructions.get(i); 
//			if  (current.dest != null && current.dest.equals(variable)){
//				definitionIndex = i; 
//			}
//			if (current.a != null && current.a.equals(variable)) {
//				usageIndex = i; break;
//			}
//			if (current.b != null && current.b.equals(variable)) {
//				usageIndex = i; break;
//			}
//		}
//		return usageIndex < definitionIndex;
//	}
	
}
