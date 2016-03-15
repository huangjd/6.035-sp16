package edu.mit.compilers.codegen;

import java.util.AbstractMap;

import edu.mit.compilers.common.SymbolTable;
import edu.mit.compilers.nodes.*;

public class Backend extends Visitor {

  Value returnValue;
  IRBuilder builder;
  SymbolTable currentSymtab;

  BasicBlock currentBreakableEntrance, currentBreakableExit;

  BasicBlock exit, exitM1, exitM2; // INIT

  public Backend() {
  }

  protected Value compile(ExpressionNode node) {
    returnValue = null;
    node.accept(this);
    assert (returnValue != null);
    Value temp = returnValue;
    returnValue = null;
    return temp;
  }

  @Override
  protected void visit(Add node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitOp(Opcode.ADD, a, b);
  }

  @Override
  protected void visit(And node) {
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();


    Value a = compile(node.left);
    builder.emitBranch(a, evalRight, exit);
    BasicBlock leftout = builder.getCurrentBasicBlock();


    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitBranch(exit);
    BasicBlock rightout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);
    AbstractMap.SimpleEntry<?, ?>[] comeFroms = new AbstractMap.SimpleEntry<?, ?>[]{
        new AbstractMap.SimpleEntry<Value, BasicBlock>(a, leftout),
        new AbstractMap.SimpleEntry<Value, BasicBlock>(b, rightout)
    };

    returnValue = builder.createPhiNode((AbstractMap.SimpleEntry<Value, BasicBlock>[]) comeFroms);
  }

  @Override
  protected void visit(Assign node) {
    Value value = compile(node.value);
    builder.emitStore(value, node.var);
  }

  @Override
  protected void visit(Block node) {
    super.visit(node);
  }

  @Override
  protected void visit(BooleanLiteral node) {
    returnValue = new Immediate(node.value ? 1l : 0l);
  }

  @Override
  protected void visit(Break node) {
    assert (currentBreakableExit != null);
    builder.emitBranch(currentBreakableExit);
  }

  @Override
  protected void visit(Continue node) {
    assert (currentBreakableEntrance != null);
    builder.emitBranch(currentBreakableEntrance);
  }

  @Override
  protected void visit(Die node) {
    switch (node.exitCode) {
    case -1:
      builder.emitBranch(exitM1);
      break;
    case -2:
      builder.emitBranch(exitM2);
      break;
    default:
      builder.prepareArgument(0, new Immediate(node.exitCode));
      builder.emitBranch(exit);
    }
  }

  @Override
  protected void visit(Div node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getKey(); // dividend
  }

  @Override
  protected void visit(Eq node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETE, a, b);
  }

  @Override
  protected void visit(For node) {
    BasicBlock pushEntrance = currentBreakableEntrance;
    BasicBlock pushExit = currentBreakableExit;

    Value init = compile(node.init);
    Value end = compile(node.end);
    Immediate increment = new Immediate(node.increment);
    builder.emitStore(init, node.loopVar);

    BasicBlock cond = builder.createBasicBlock();
    BasicBlock incr = builder.createBasicBlock();
    BasicBlock body = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    currentBreakableEntrance = incr;
    currentBreakableExit = exit;

    builder.emitBranch(cond);

    builder.insertBasicBlock(cond);
    builder.setCurrentBasicBlock(cond);
    Value loopVar = builder.emitLoad(node.loopVar);
    builder.emitCmp(loopVar, end);
    builder.emitBranch(Opcode.SETGE, exit);

    builder.insertBasicBlock(body);
    builder.setCurrentBasicBlock(body);
    node.body.accept(this);
    builder.emitBranch(incr);

    builder.insertBasicBlock(incr);
    builder.setCurrentBasicBlock(incr);
    builder.emitOp(Opcode.ADD, loopVar, increment);
    builder.emitBranch(cond);

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);

    currentBreakableExit = pushExit;
    currentBreakableEntrance = pushEntrance;
  }

  @Override
  protected void visit(Ge node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETGE, a, b);
  }

  @Override
  protected void visit(Gt node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETG, a, b);
  }

  @Override
  protected void visit(If node) {
    BasicBlock t = builder.createBasicBlock();
    BasicBlock f = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();
    Value cond = compile(node.cond);
    builder.emitBranch(cond, t, f);

    builder.insertBasicBlock(t);
    builder.setCurrentBasicBlock(t);
    node.trueBlock.accept(this);
    builder.emitBranch(exit);;

    builder.insertBasicBlock(f);
    builder.setCurrentBasicBlock(f);
    node.falseBlock.accept(this);
    builder.emitBranch(exit);;

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);
  }

  @Override
  protected void visit(IntLiteral node) {
    returnValue = new Immediate(node.value);
  }

  @Override
  protected void visit(Le node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETLE, a, b);
  }

  @Override
  protected void visit(Lt node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETL, a, b);
  }

  @Override
  protected void visit(Length node) {
    returnValue = new Immediate(node.array.length);
  }

  @Override
  protected void visit(Load node) {
    Value index = compile(node.index);
    if (node.checkBounds) {
      BasicBlock ok = builder.createBasicBlock();
      builder.emitCmp(index, new Immediate(0));
      builder.emitBranch(Opcode.SETL, exitM1);
      builder.emitCmp(index, new Immediate(node.array.length));
      builder.emitBranch(Opcode.SETGE, exitM1);
    }
    returnValue = builder.emitLoad(node.array, index);
  }

  @Override
  protected void visit(Minus node) {
    Value a = compile(node.right);
    returnValue = builder.emitOp(Opcode.SUB, new Immediate(0), a);
  }

  @Override
  protected void visit(Mod node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getValue();
  }

  @Override
  protected void visit(Mul node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitMul(a, b);
  }

  @Override
  protected void visit(Ne node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETNE, a, b);
  }

  @Override
  protected void visit(Not node) {
    Value b = compile(node.right);
    returnValue = new Value();
    builder.emitInstruction(new Instruction(Opcode.SETE, ));
    returnValue = builder.emitNot(b);
  }

  @Override
  protected void visit(Or node) {
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    Value a = compile(node.left);
    builder.emitBranch(a, exit, evalRight);
    BasicBlock leftout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitBranch(exit);
    BasicBlock rightout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);
    AbstractMap.SimpleEntry<?, ?>[] comeFroms = new AbstractMap.SimpleEntry<?, ?>[]{
        new AbstractMap.SimpleEntry<Value, BasicBlock>(a, leftout),
        new AbstractMap.SimpleEntry<Value, BasicBlock>(b, rightout)
    };

    returnValue = builder.createPhiNode((AbstractMap.SimpleEntry<Value, BasicBlock>[]) comeFroms);
  }

  @Override
  protected void visit(Pass node) {
    for (int i = 0; i < node.nop; i++) {
      builder.emitInstruction(new Instruction(Opcode.NOP, null, null));
    }
  }

  @Override
  protected void visit(Return node) {
    if (node.value)
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
    Value cond = compile(node.cond); // TODO check over

    if (cond.value == 1) {
      returnValue = compile(node.trueExpr);
    } else {
      returnValue = compile(node.falseExpr);
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    throw new RuntimeException("Unparsed Int Literal should not be here in this stage");
  }

  @Override
  protected void visit(While node) {
    // TODO
  }

  @Override
  protected void visit(Call node) {
    // TODO Auto-generated method stub
    super.visit(node);
  }

  @Override
  protected void visit(VarExpr node) {
    // TODO Auto-generated method stub
    super.visit(node);
  }

  @Override
  protected void visit(CallStmt node) {
    // TODO Auto-generated method stub
    super.visit(node);
  }

  @Override
  protected void visit(VarDecl node) {
    // TODO Auto-generated method stub
    super.visit(node);
  }

  @Override
  protected void visit(Sub node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = Emits.emitSub(a, b);
  }

}
