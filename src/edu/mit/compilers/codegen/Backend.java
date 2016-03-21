package edu.mit.compilers.codegen;

import java.util.ArrayList;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;

public class Backend extends Visitor {

  Value returnValue;
  IRBuilder builder;
  BasicBlock currentBreakableEntrance, currentBreakableExit;
  CallingConvention currentFuncCallingConvention;
  SymbolTable strtab;
  ScopedMap<Var, Value> symtab;

  public String binName;

  final BasicBlock exit0, exitM1, exitM2; // INIT

  public Backend(String outName) {
    binName = outName;
    builder = new IRBuilder();
    strtab = new SymbolTable();
    symtab = new ScopedMap();

    strtab.insert(
        new Var("string@outofboundsmsg", binName + ": %d:%d %s: array index out of bounds\n"));
    strtab.insert(
        new Var("string@controlreachesend", binName + ": %d:%d %s: control reaches end of non-void function\n"));

    builder.insertFunction(new Function("@@exit", null));

    exit0 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exit0);
    builder.emitInstruction(
        new Instruction(Opcode.MOV, new Value(new Immediate(0)), new Value(new Register(Register.rdi))));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Value(new Symbol("exit"))));

    exitM1 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exitM1);
    builder.emitInstruction(
        new Instruction(Opcode.MOV, new Value(new Symbol("stderr")), new Value(new Register(Register.rdi))));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Value(new Symbol("string@outofboundsmsg")),
        new Value(new Register(Register.rsi))));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Value(new Symbol("fprintf"))));
    builder.emitInstruction(
        new Instruction(Opcode.MOV, new Value(new Immediate(-1)), new Value(new Register(Register.rdi))));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Value(new Symbol("exit"))));

    exitM2 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exitM2);
    builder.emitInstruction(
        new Instruction(Opcode.MOV, new Value(new Symbol("stderr")), new Value(new Register(Register.rdi))));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Value(new Symbol("string@controlreachesend")),
        new Value(new Register(Register.rsi))));
    builder.emitInstruction(
        new Instruction(Opcode.MOV, new Value(new Immediate(-2)), new Value(new Register(Register.rdi))));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Value(new Symbol("exit"))));
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
  protected void visit(Function node) {
    if (!node.isCallout) {
      builder.insertFunction(node);
      BasicBlock first = builder.createBasicBlock();
      builder.insertBasicBlock(first);
      builder.setCurrentBasicBlock(first);
      builder.emitInstruction(new Label(node.id));

      currentFuncCallingConvention = new CallingConventionX86_64Linux();

      symtab = symtab.scope();
      ArrayList<Var> list = node.localSymtab.asList();
      for (int i = 0; i < node.nParams; i++) {
        symtab.insert(list.get(i), currentFuncCallingConvention.getNthArg(i));
      }

      builder.emitPrologue(currentFuncCallingConvention);
      node.body.accept(this);
      symtab = symtab.unscope();
    }
  }


  @Override
  protected void visit(Add node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitOp(Opcode.ADD, a, b);
  }

  @Override
  protected void visit(Sub node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitOp(Opcode.SUB, a, b);
  }

  @Override
  protected void visit(And node) {
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    Value retr = builder.allocateRegister();

    Value a = compile(node.left);
    builder.emitStore(a, retr);
    builder.emitBranch(a, evalRight, exit);

    // BasicBlock leftout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitStore(b, retr);
    builder.emitBranch(exit);
    // BasicBlock rightout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);

    /*AbstractMap.SimpleEntry<?, ?>[] comeFroms = new AbstractMap.SimpleEntry<?, ?>[]{
      new AbstractMap.SimpleEntry<Value, BasicBlock>(a, leftout),
      new AbstractMap.SimpleEntry<Value, BasicBlock>(b, rightout)
    };

    returnValue = builder.createPhiNode((AbstractMap.SimpleEntry<Value, BasicBlock>[]) comeFroms);*/
    returnValue = builder.emitLoad(retr);
  }

  @Override
  protected void visit(Or node) {
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    Value retr = builder.allocateRegister();
    Value a = compile(node.left);
    builder.emitStore(a, retr);
    builder.emitBranch(a, exit, evalRight);
    // BasicBlock leftout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitStore(b, retr);
    builder.emitBranch(exit);
    // BasicBlock rightout = builder.getCurrentBasicBlock();

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);
    /*
     * AbstractMap.SimpleEntry<?, ?>[] comeFroms = new
     * AbstractMap.SimpleEntry<?, ?>[]{ new AbstractMap.SimpleEntry<Value,
     * BasicBlock>(a, leftout), new AbstractMap.SimpleEntry<Value,
     * BasicBlock>(b, rightout) };
     *
     * returnValue = builder.createPhiNode((AbstractMap.SimpleEntry<Value,
     * BasicBlock>[]) comeFroms);
     */

    returnValue = builder.emitLoad(retr);
  }

  @Override
  protected void visit(Assign node) {
    Value value = compile(node.value);
    builder.emitStore(value, symtab.lookup(node.var));
  }

  @Override
  protected void visit(Block node) {
    symtab = symtab.scope();
    super.visit(node);
    symtab = symtab.unscope();
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
    builder.emitStore(init, symtab.lookup(node.loopVar));

    BasicBlock cond = builder.createBasicBlock();
    BasicBlock incr = builder.createBasicBlock();
    BasicBlock body = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    currentBreakableEntrance = incr;
    currentBreakableExit = exit;

    builder.emitBranch(cond);

    builder.insertBasicBlock(cond);
    builder.setCurrentBasicBlock(cond);
    Value loopVar = builder.emitLoad(symtab.lookup(node.loopVar));
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
  protected void visit(While node) {
    BasicBlock pushEntrance = currentBreakableEntrance;
    BasicBlock pushExit = currentBreakableExit;

    BasicBlock cond = builder.createBasicBlock();
    BasicBlock body = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    currentBreakableEntrance = cond;
    currentBreakableExit = exit;

    builder.emitBranch(cond);

    builder.insertBasicBlock(cond);
    builder.setCurrentBasicBlock(cond);
    Value test = compile(node.cond);
    builder.emitBranch(test, body, exit);

    builder.insertBasicBlock(body);
    builder.setCurrentBasicBlock(body);
    node.body.accept(this);
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
    builder.emitBranch(exit);

    builder.insertBasicBlock(f);
    builder.setCurrentBasicBlock(f);
    node.falseBlock.accept(this);
    builder.emitBranch(exit);

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
      builder.emitCmp(index, new Immediate(0));
      builder.emitBranch(Opcode.SETL, exitM1);
      builder.emitCmp(index, new Immediate(node.array.length));
      builder.emitBranch(Opcode.SETGE, exitM1);
    }
    returnValue = builder.emitLoad(symtab.lookup(node.array), index);
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
    returnValue = builder.allocateRegister();
    builder.emitInstruction(new Instruction(returnValue, Opcode.SETE, b));
  }



  @Override
  protected void visit(Pass node) {
    for (int i = 0; i < node.nop; i++) {
      builder.emitInstruction(new Instruction(Opcode.NOP));
    }
  }

  @Override
  protected void visit(Return node) {
    if (node.value != null) {
      Value value = compile(node.value);
      builder.emitStore(value, builder.allocateRegister(currentFuncCallingConvention.getRetReg()));
    }
    builder.emitEpilogue(currentFuncCallingConvention);
  }

  @Override
  protected void visit(Store node) {
    Value index = compile(node.index);
    if (node.checkBounds) {
      builder.emitCmp(index, new Immediate(0));
      builder.emitBranch(Opcode.SETL, exitM1);
      builder.emitCmp(index, new Immediate(node.array.length));
      builder.emitBranch(Opcode.SETGE, exitM1);
    }
    Value value = compile(node.value);
    switch (node.cop) {
    case NONE:
      builder.emitStore(value, symtab.lookup(node.array), index);
      break;
    case PLUS:
      Value old = builder.emitLoad(symtab.lookup(node.array), index);
      Value newVal = builder.emitOp(Opcode.ADD, old, value);
      builder.emitStore(newVal, symtab.lookup(node.array), index);
      break;
    case MINUS:
      old = builder.emitLoad(symtab.lookup(node.array), index);
      newVal = builder.emitOp(Opcode.SUB, old, value);
      builder.emitStore(newVal, symtab.lookup(node.array), index);
      break;
    }
  }

  protected static int strtabID = 0;

  @Override
  protected void visit(StringLiteral node) {
    String symbol = "@string" + Integer.toString(strtabID);
    strtab.insert(new Var(symbol, node.value));
    strtabID++;
    returnValue = new Symbol(symbol);
  }

  @Override
  protected void visit(Ternary node) {
    BasicBlock t = builder.createBasicBlock();
    BasicBlock f = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    Value retr = builder.allocateRegister();
    Value cond = compile(node.cond);
    builder.emitBranch(cond, t, f);

    builder.insertBasicBlock(t);
    builder.setCurrentBasicBlock(t);
    Value tval = compile(node.trueExpr);
    builder.emitStore(tval, retr);
    builder.emitBranch(exit);


    builder.insertBasicBlock(f);
    builder.setCurrentBasicBlock(f);
    Value fval = compile(node.falseExpr);
    builder.emitStore(fval, retr);
    builder.emitBranch(exit);

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);
    returnValue = retr;
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    throw new RuntimeException("Unparsed Int Literal is not allowed in this stage");
  }

  @Override
  protected void visit(Call node) {
    ArrayList<Value> args = new ArrayList<Value>();
    for (int i = 0; i < node.args.size(); i++) {
      args.add(compile(node.args.get(i)));
    }
    int s = currentFuncCallingConvention.getNumArgsPassedByReg(); // == 6

    for (int i = 0; i < Math.min(node.args.size(), s); i++) {
      builder.emitStore(args.get(i), builder.allocateRegister(currentFuncCallingConvention.getNthArg(i)));
    }
    for (int i = s; i < node.args.size(); i++) {
      builder.emitStore(args.get(i), new Memory(Register.RSP, null, 8 * (i - s), 8));
    }
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol(node.func.id)));
    returnValue = builder.getReturnValue();
  }

  @Override
  protected void visit(VarExpr node) {
    returnValue = builder.emitLoad(symtab.lookup(node.var));
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

}
