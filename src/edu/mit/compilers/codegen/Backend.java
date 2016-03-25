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
  ArrayList<BasicBlock> deferredBlocks;

  int stackAdjust;
  public String binName;

  public class FunctionContent {
    ArrayList<BasicBlock> text;
    long baseStackAdjust;

    FunctionContent(ArrayList<BasicBlock> text, long baseStackAdjust) {
      this.text = text;
      this.baseStackAdjust = baseStackAdjust;
    }
  }
  ArrayList<FunctionContent> functions = new ArrayList<>();

  Function currentFunc;

  final BasicBlock exit0, exitM1, exitM2; // INIT
  public Backend(String outName) {
    binName = outName;
    builder = new IRBuilder();
    strtab = new SymbolTable();
    symtab = new ScopedMap<Var, Value>();

    String s1 = ".string.outofboundsmsg";
    String s2 = ".string.controlreachesend";

    strtab.insert(new Var(s1, binName + ": %d:%d %s: array index out of bounds\n"));
    strtab.insert(new Var(s2, binName + ": %d:%d %s: control reaches end of non-void function\n"));

    builder.insertFunction();

    exit0 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exit0);
    builder.emitInstruction(new Instruction(Opcode.MOV, new Immediate(0).box(), Register.RDI.box()));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol("exit").box()));

    exitM1 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exitM1);
    builder.emitInstruction(new Instruction(Opcode.MOV, new Symbol("stderr").box(), Register.RDI.box()));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Symbol(s1).box(), Register.RSI.box()));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol("fprintf").box()));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Immediate(-1).box(), Register.RDI.box()));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol("exit").box()));

    exitM2 = builder.createBasicBlock();
    builder.setCurrentBasicBlock(exitM2);
    builder.emitInstruction(new Instruction(Opcode.MOV, new Symbol("stderr").box(), Register.RDI.box()));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Symbol(s2).box(), Register.RSI.box()));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol("fprintf").box()));
    builder.emitInstruction(new Instruction(Opcode.MOV, new Immediate(-2).box(), Register.RDI.box()));
    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol("exit").box()));
    builder.emitInstruction(new Instruction(Opcode.RET));
  }

  @Override
  protected void visit(Program node) {
    for (Var global : node.globals.asList()) {
      symtab.insert(global, new Value(new Symbol(".bss." + global.id)));
    }

    for (FunctionNode f : node.functions) {
      f.accept(this);
    }
  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      deferredBlocks = new ArrayList<>();
      currentFuncCallingConvention = new CallingConventionX86_64Linux();
      stackAdjust = 16 + 8 * currentFuncCallingConvention.getCalleeSavedRegs().length;

      builder.insertFunction();
      currentFunc = node;
      strtab.insert(new Var(".func." + node.id, node.id));

      BasicBlock first = builder.createBasicBlock(".func." + node.id);

      builder.insertBasicBlock(first);
      builder.setCurrentBasicBlock(first);

      symtab = symtab.scope();

      ArrayList<Var> list = node.localSymtab.asList();
      int s = currentFuncCallingConvention.getNumArgsPassedByReg();

      for (int i = 0; i < Math.min(s, node.nParams); i++) {
        symtab.insert(list.get(i), new Register(currentFuncCallingConvention.getNthArgRegIndex(i)).box());
      }
      for (int i = s; i < node.nParams; i++) {
        symtab.insert(list.get(i), new Memory(Register.RBP.box(), null, currentFuncCallingConvention.getCalleeNthArgOffsetRbp(i), 8).box());
      }

      builder.emitInstruction(new Instruction(Opcode.PUSH, Register.RBP.box()));
      builder.emitMov(Register.RSP.box(), Register.RBP.box());
      builder.emitPrologue(currentFuncCallingConvention);

      BasicBlock body = builder.createBasicBlock();
      builder.emitBranch(body);
      builder.insertBasicBlock(body);
      builder.setCurrentBasicBlock(body);

      node.body.accept(this);

      for (BasicBlock bb : deferredBlocks) {
        builder.insertBasicBlock(bb);
      }
      symtab = symtab.unscope();

      functions.add(new FunctionContent(builder.basicBlocks, stackAdjust));
    }
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
  protected void visit(Sub node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitOp(Opcode.SUB, a, b);
  }

  @Override
  protected void visit(Mul node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitMul(a, b);
  }

  @Override
  protected void visit(Div node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getKey(); // dividend
  }

  @Override
  protected void visit(Mod node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitDiv(a, b).getValue();
  }

  @Override
  protected void visit(And node) {
    BasicBlock evalRight = builder.createBasicBlock();
    BasicBlock exit = builder.createBasicBlock();

    Value retr = builder.allocateRegister();
    Value a = compile(node.left);
    builder.emitStore(a, retr);
    builder.emitBranch(a, evalRight, exit);

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitStore(b, retr);
    builder.emitBranch(exit);

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);

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

    builder.insertBasicBlock(evalRight);
    builder.setCurrentBasicBlock(evalRight);
    Value b = compile(node.right);
    builder.emitStore(b, retr);
    builder.emitBranch(exit);

    builder.insertBasicBlock(exit);
    builder.setCurrentBasicBlock(exit);

    returnValue = builder.emitLoad(retr);
  }

  @Override
  protected void visit(Not node) {
    Value b = compile(node.right);
    returnValue = builder.allocateRegister();
    builder.emitInstruction(new Instruction(Opcode.MOV, new Immediate(1).box(), returnValue));
    builder.emitInstruction(new Instruction(Opcode.XOR, b, returnValue));
    builder.emitInstruction(new Instruction(Opcode.AND, new Immediate(1).box(), returnValue));
  }

  @Override
  protected void visit(Block node) {
    symtab = symtab.scope();
    super.visit(node);
    symtab = symtab.unscope();
  }

  @Override
  protected void visit(Die node) {
    SourcePosition pos = node.getSourcePosition();
    if (pos == null) {
      pos = currentFunc.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
    }
    builder.emitMov(pos.lineNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(3)).box());
    builder.emitMov(pos.colNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(4)).box());
    builder.emitMov(new Symbol(".func." + currentFunc.id).box(), new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(5)).box());

    switch (node.exitCode) {
    case -1:
      builder.emitBranch(exitM1);
      break;
    case -2:
      builder.emitBranch(exitM2);
      break;
    default:
      throw new IllegalArgumentException();
    }
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
  protected void visit(For node) {
    BasicBlock pushEntrance = currentBreakableEntrance;
    BasicBlock pushExit = currentBreakableExit;

    Value init = compile(node.init);
    Value end = compile(node.end);
    Value increment = compile(new IntLiteral(node.increment, null).box());

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
    Instruction i1 = builder.emitInstruction(new Instruction(Opcode.CMP, loopVar, end));
    builder.emitInstruction(new Instruction(Opcode.JGE, new Symbol(exit.label).box()).addDependency(i1,
        Instruction.NO_RFLAGS_MODIFICATION));
    builder.emitBranch(body);

    builder.insertBasicBlock(body);
    builder.setCurrentBasicBlock(body);
    node.body.accept(this);
    builder.emitBranch(incr);

    builder.insertBasicBlock(incr);
    builder.setCurrentBasicBlock(incr);
    builder.emitInstruction(new Instruction(Opcode.ADD, increment, loopVar));
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
  protected void visit(Eq node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETE, a, b);
  }

  @Override
  protected void visit(Ne node) {
    Value a = compile(node.left);
    Value b = compile(node.right);
    returnValue = builder.emitCmpToBool(Opcode.SETNE, a, b);
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
  protected void visit(BooleanLiteral node) {
    returnValue = new Immediate(node.value ? 1 : 0).box();
  }

  @Override
  protected void visit(IntLiteral node) {
    if (node.value <= Integer.MAX_VALUE && node.value >= Integer.MIN_VALUE) {
      returnValue = new Immediate(node.value).box();
    } else {
      returnValue = builder.emitMov(node.value, builder.allocateRegister());
    }
  }

  @Override
  protected void visit(Minus node) {
    Value a = compile(node.right);
    returnValue = builder.emitOp(Opcode.SUB, new Immediate(0).box(), a);
  }

  @Override
  protected void visit(Length node) {
    returnValue = new Immediate(node.array.length).box();
  }

  @Override
  protected void visit(Load node) {
    SourcePosition pos = node.getSourcePosition();
    if (pos == null) {
      pos = currentFunc.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
    }

    BasicBlock defer = builder.createBasicBlock();

    Value index = compile(node.index);
    if (node.checkBounds) {
      Instruction i1 = builder.emitInstruction(new Instruction(Opcode.CMP, index, new Immediate(0).box()));
      builder.emitInstruction(new Instruction(Opcode.JL, new Symbol(defer.label).box()).addDependency(i1,
          Instruction.NO_RFLAGS_MODIFICATION));

      Instruction i2 = builder.emitInstruction(new Instruction(Opcode.CMP, index, new Immediate(node.array.length).box()));
      builder.emitInstruction(new Instruction(Opcode.JGE, new Symbol(defer.label).box()).addDependency(i2,
          Instruction.NO_RFLAGS_MODIFICATION));
    }

    BasicBlock temp = builder.currentBB;
    builder.setCurrentBasicBlock(defer);
    builder.emitMov(pos.lineNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(3)).box());
    builder.emitMov(pos.colNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(4)).box());
    builder.emitMov(new Symbol(".func." + currentFunc.id).box(), new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(5)).box());
    builder.emitBranch(exitM1);
    deferredBlocks.add(defer);

    builder.setCurrentBasicBlock(temp);
    returnValue = builder.emitLoad(symtab.lookup(node.array), index);
  }

  @Override
  protected void visit(Store node) {
    SourcePosition pos = node.getSourcePosition();
    if (pos == null) {
      pos = currentFunc.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
    }

    BasicBlock defer = builder.createBasicBlock();

    Value index = compile(node.index);
    if (node.checkBounds) {
      Instruction i1 = builder.emitInstruction(new Instruction(Opcode.CMP, index, new Immediate(0).box()));
      builder.emitInstruction(new Instruction(Opcode.JL, new Symbol(defer.label).box()).addDependency(i1,
          Instruction.NO_RFLAGS_MODIFICATION));

      Instruction i2 = builder.emitInstruction(new Instruction(Opcode.CMP, index, new Immediate(node.array.length).box()));
      builder.emitInstruction(new Instruction(Opcode.JGE, new Symbol(defer.label).box()).addDependency(i2,
          Instruction.NO_RFLAGS_MODIFICATION));
    }
    BasicBlock temp = builder.currentBB;
    builder.setCurrentBasicBlock(defer);
    builder.emitMov(pos.lineNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(3)).box());
    builder.emitMov(pos.colNum, new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(4)).box());
    builder.emitMov(new Symbol(".func." + currentFunc.id).box(), new Register(new CallingConventionX86_64Linux().getNthArgRegIndex(5)).box());
    builder.emitBranch(exitM1);
    deferredBlocks.add(defer);

    builder.setCurrentBasicBlock(temp);
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
      builder.emitStore(value, new Register(currentFuncCallingConvention.getRetReg()).box());
    }
    builder.emitEpilogue(currentFuncCallingConvention);
    builder.emitMov(Register.RBP.box(), Register.RSP.box());
    builder.emitInstruction(new Instruction(Opcode.POP, Register.RBP.box()));
    builder.emitInstruction(new Instruction(Opcode.RET));
  }

  @Override
  protected void visit(Assign node) {
    Value value = compile(node.value);
    builder.emitStore(value, symtab.lookup(node.var));
  }

  protected static int strtabID = 0;
  @Override
  protected void visit(StringLiteral node) {
    String symbol = ".string." + Integer.toString(strtabID);
    strtab.insert(new Var(symbol, node.value));
    strtabID++;
    returnValue = new Symbol(symbol).box();
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

    ArrayList<Value> saved = new ArrayList<Value>();
    int[] callerSaved = currentFuncCallingConvention.getCallerSavedRegs();
    for (int i : callerSaved) {
      Value v = builder.allocateRegister(Register.MUST_STACK);
      builder.emitMov(new Register(i).box(), v);
    }

    for (int i = s; i < node.args.size(); i++) {
      builder.emitStore(args.get(i), Register.RSP.box(),
          new Immediate(currentFuncCallingConvention.getCallerNthArgOffsetRsp(i)).box());
    }
    for (int i = 0; i < Math.min(node.args.size(), s); i++) {
      builder.emitStore(args.get(i), builder.allocateRegister(currentFuncCallingConvention.getNthArgRegIndex(i)));
    }

    builder.emitInstruction(new Instruction(Opcode.CALL, new Symbol((node.func.isCallout ? "" : ".func.") + node.func.id).box()));

    returnValue = builder.emitMov(new Register(currentFuncCallingConvention.getRetReg()).box(), builder.allocateRegister());

    for (int i = 0; i < callerSaved.length; i++) {
      builder.emitMov(saved.get(i), new Register(i).box());
    }
  }

  @Override
  protected void visit(VarExpr node) {
    returnValue = builder.emitLoad(symtab.lookup(node.var));
  }

  @Override
  protected void visit(CallStmt node) {
    visit(node.call);
    returnValue = null;
  }

  @Override
  protected void visit(VarDecl node) {
    if (node.var.isPrimitive()) {
      Value val = builder.allocateRegister();
      symtab.insert(node.var, val);
      builder.emitMov(0 , val);
    } else {
      Value val = builder.allocateRegister();
      builder.emitInstruction(new Instruction(Opcode.LEA, new Memory(Register.RBP.box(), null, stackAdjust, 8).box(), val));
      symtab.insert(node.var, val);
      stackAdjust += node.var.length;

      BasicBlock loopBegin = builder.createBasicBlock();
      BasicBlock loopExit = builder.createBasicBlock();
      // set array to 0
      Value i = builder.allocateRegister();
      builder.emitMov(0, i);
      builder.emitBranch(loopBegin);

      builder.insertBasicBlock(loopBegin);
      builder.setCurrentBasicBlock(loopBegin);
      builder.emitStore(new Immediate(0).box(), val, i);
      builder.emitInstruction(new Instruction(Opcode.ADD, new Immediate(1).box(), i));
      builder.emitInstruction(new Instruction(Opcode.CMP, i, new Immediate(node.var.length).box()));
      builder.emitInstruction(new Instruction(Opcode.JL, new Symbol(loopBegin.label).box()));
      builder.emitBranch(loopExit);

      builder.insertBasicBlock(loopExit);
      builder.setCurrentBasicBlock(loopExit);
    }
  }
}
