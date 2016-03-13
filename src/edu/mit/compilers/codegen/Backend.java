package edu.mit.compilers.codegen;

import edu.mit.compilers.nodes.*;

public class Backend extends Visitor {

  Register returnValue;
  IRBuilder builder;

  public Backend() {
  }

  protected void compile(ProgramNode node) {

  }

  protected void compile(FunctionNode node) {

  }

  protected void compile(StatementNode node) {

  }

  protected Register compile(ExpressionNode node) {

  }

  @Override
  protected void visit(Add node) {
    Register a = compile(node.left);
    Register b = compile(node.right);
    returnValue = builder.emitOp(Opcode.ADD, a, b);
  }

  @Override
  protected void visit(And node) {
		// TODO implement short circuiting
	}

  @Override
  protected void visit(Assign node) {
		// TODO implement 
	}

  @Override
  protected void visit(Block node) {
		// TODO implement 
	}

  @Override
  protected void visit(BooleanLiteral node) {
		returnValue = compile(node.box());
	}

  @Override
  protected void visit(Break node) {
		// TODO implement 
	}

  @Override
  protected void visit(Continue node) {
		// TODO implement 
	}

  @Override
  protected void visit(Die node) {
		// TODO implement 
	}

  @Override
  protected void visit(Div node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		returnValue = builder.emitDiv(a, b).getKey(); // dividend
	}

  @Override
  protected void visit(Eq node) {
  	Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETE, tmp);
	}

  @Override
  protected void visit(For node) {
		// TODO implement 
	}

  @Override
  protected void visit(Ge node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETGE, tmp);
	}

  @Override
  protected void visit(Gt node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETG, tmp);
	}

  @Override
  protected void visit(If node) {
		// TODO implement 
	}

  @Override
  protected void visit(IntLiteral node) {
		returnValue = compile(node.box());
	}

  @Override
  protected void visit(Le node) {
  	Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETLE, tmp);
	}

  @Override
  protected void visit(Lt node) {
  	Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETL, tmp);
	}

  @Override
  protected void visit(Length node) {
		Register a = compile(node.box());
		returnValue = Emits.emitLength(a);
	}

  @Override
  protected void visit(Load node) {
		// TODO
	}

  @Override
  protected void visit(Minus node) {
		Register a = compile(node.box());
		returnValue = Emits.emitMinus(a);
	}

  @Override
  protected void visit(Mod node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		returnValue = builder.emitDiv(a, b).getValue();
	}

  @Override
  protected void visit(Mul node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		returnValue = builder.emitOp(Opcode.IMUL, a, b);
	}

  @Override
  protected void visit(Ne node) {
  	Register a = compile(node.left);
		Register b = compile(node.right);
		Register tmp = builder.emitOp(Opcode.CMP, a, b);
		returnValue = builder.emitOp(Opcode.SETNE, tmp);
	}

  @Override
  protected void visit(Not node) {
		Register b = compile(node.right);
		returnValue = builder.emitOp(Opcode.XOR, b, new Immediate(0)); // flip bits
	}


  @Override
  protected void visit(Or node) {
		// TODO implement short circuiting
	}

  @Override
  protected void visit(Pass node) {
		// TODO pass
	}

  @Override
  protected void visit(Return node) {
		// TODO pass
	}

  @Override
  protected void visit(Store node) {
		// TODO pass
	}

  @Override
  protected void visit(StringLiteral node) {
		returnValue = compile(node.box());
	}

  @Override
  protected void visit(Ternary node) {
		Register cond = compile(node.cond); // TODO check over
		
		if (cond.value == 1) {
			returnValue = compile(node.trueExpr);
		} else {
			returnValue = compile(node.falseExpr);
		}
	}

  @Override
  protected void visit(UnparsedIntLiteral node) {
		Register a = compile(node.box());
		returnValue = Emits.emitUnparsedIntLiteral(a);
	}

  @Override
  protected void visit(While node) {
		// TODO
	}



  @Override
  protected void visit(Sub node) {
		Register a = compile(node.left);
		Register b = compile(node.right);
		returnValue = Emits.emitSub(a, b);
	}

}
