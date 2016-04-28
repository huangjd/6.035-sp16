package edu.mit.compilers.codegen;

import java.util.HashMap;

public class CSEPass extends BasicBlockVisitor {

	@Override
	public Transformable getInitValue() {
		return new TransformableCSE(new HashMap<Operand, Subexpression>(), new HashMap<Subexpression, Operand>());
	}

	//first pass
	@Override
	public Transformable visit(BasicBlock b, Transformable in) {
		HashMap<Operand, Subexpression> varExpMap = new HashMap<Operand, Subexpression>(((TransformableCSE) in).varExpMap);
		HashMap<Subexpression, Operand> expTmpMap =  new HashMap<Subexpression, Operand>(((TransformableCSE) in).expTmpMap);
		
		for (Instruction instruction : b) {
			CSEPass.Subexpression sub = new CSEPass.Subexpression(instruction.a, instruction.b, instruction.op);
			varExpMap.put(instruction.dest, sub);
			if (!expTmpMap.containsKey(sub)) {
				expTmpMap.put(new Subexpression(instruction.dest), new Value());
			}
		}
		return new TransformableCSE(varExpMap, expTmpMap);
	}
	
	//final pass
	public BasicBlock finalPass(BasicBlock b, Transformable in) {
		HashMap<Operand, Subexpression> varExpMap = new HashMap<Operand, Subexpression>(((TransformableCSE) in).varExpMap);
		HashMap<Subexpression, Operand> expTmpMap =  new HashMap<Subexpression, Operand>(((TransformableCSE) in).expTmpMap);
		
		
		BasicBlock modifiedBasicBlock = new BasicBlock();
		
		for (Instruction instruction : b) {
			CSEPass.Subexpression sub = new CSEPass.Subexpression(instruction.a, instruction.b, instruction.op);
			if (expTmpMap.containsKey(sub)) {
				modifiedBasicBlock.add(new Instruction(instruction.dest, instruction.op, expTmpMap.get(sub)));
			} else {
				modifiedBasicBlock.add(instruction);
				if (!varExpMap.containsKey(instruction.dest)){
					modifiedBasicBlock.add(new Instruction(new Value(), instruction.op, instruction.dest));
				}
				varExpMap.put(instruction.dest, sub);
				if (!expTmpMap.containsKey(sub)) {
					expTmpMap.put(new Subexpression(instruction.dest), new Value());
				}	
			}
		}
		return modifiedBasicBlock;
	}
	
	public static class Subexpression {
		Operand a, b;
		Op op;
		public Subexpression(Operand a, Operand b, Op op) {
			this.a = a;
			this.b = b;
			this.op = op;
		}
		
		public Subexpression(Operand a) {
			this.a = a;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Subexpression) {
				Subexpression sub = (Subexpression) obj;
				return this.a.equals(sub.a) && this.b.equals(sub.b) && this.op.equals(sub.op);
			} else {
				return false;
			}
		}
	}
	
	public static class TransformableCSE implements Transformable<TransformableCSE> {

		private HashMap<Operand, Subexpression> varExpMap;
		private HashMap<Subexpression, Operand> expTmpMap;
		
		public TransformableCSE(HashMap<Operand, Subexpression> varExpMap, HashMap<Subexpression, Operand> expTmpMap) {
			this.varExpMap = varExpMap;
			this.expTmpMap = expTmpMap;
		}

		@Override
		public TransformableCSE transform(TransformableCSE t) {
			HashMap<Operand, Subexpression> resultVarExpMap = new HashMap<Operand, Subexpression>(t.varExpMap);
			HashMap<Subexpression, Operand> resultExpTmpMap = new HashMap<Subexpression, Operand>(t.expTmpMap);
			
			resultVarExpMap.keySet().retainAll(t.varExpMap.keySet()); // takes intersection of two maps
			resultExpTmpMap.keySet().retainAll(t.expTmpMap.keySet());
			return new TransformableCSE(resultVarExpMap, resultExpTmpMap);
		}
	}

}
