package edu.mit.compilers.codegen;

import java.util.*;

import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;
import edu.mit.compilers.nodes.Store.CoOperand;


public class Midend extends Visitor {
  // State variables
  Operand returnValue;
  BasicBlock currentBB;
  BasicBlock breakBB, continueBB;
  Operand currentAssignDest;

  // Function data
  ScopedMap<Var, Operand> symtab;
  BasicBlock funcEntryBB;
  Operand currentFunctionName;

  // Global data
  HashMap<String, Operand> strtab;
  boolean bssMode = false;

  // Const
  BasicBlock outOfBounds;
  BasicBlock controlReachesEnd;

  // Output
  public CFG cfg;

  public Midend() {
  }

  @Override
  protected void visit(Program node) {
    strtab = new HashMap<>();

    outOfBounds = new BasicBlock();
    Operand s1 = compile(new StringLiteral("%s(): %s:%s: Array index out of bounds\n", null).box());
    outOfBounds.add(new Instruction.CallInstruction(Value.dummy, new Symbol("printf"), new ArrayList<Operand>(){{add(s1);}}, true, 0));
    outOfBounds.add(Op.RET);

    controlReachesEnd = new BasicBlock();
    Operand s2 = compile(new StringLiteral("%s(): %s:%s: Control reaches end of non-void function\n", null).box());
    controlReachesEnd.add(new Instruction.CallInstruction(Value.dummy, new Symbol("printf"), new ArrayList<Operand>(){{add(s2);}}, true, 0));
    outOfBounds.add(Op.RET);

    currentBB = new BasicBlock(); // dummy, because var decl automatic emits initialization code,
    // but .bss segment is set to 0 anyway

    symtab = new ScopedMap<>();
    bssMode = true;
    for (StatementNode s : node.varDecls) {
      s.accept(this);
    }
    bssMode = false;

    ArrayList<BasicBlock> entries = new ArrayList<>();
    for (FunctionNode f : node.functions) {
      if (!((Function)(f.getNode())).isCallout) {
        currentBB = null;
        funcEntryBB = null;
        f.accept(this);
        assert(funcEntryBB != null);
        entries.add(funcEntryBB);
      }
    }
    entries.add(outOfBounds);
    entries.add(controlReachesEnd);

    cfg = new CFG(entries, symtab, strtab);
  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      currentFunctionName = compile(new StringLiteral(node.getMangledName(), null).box());

      currentBB = new BasicBlock(node.getMangledName());
      funcEntryBB = currentBB;
      symtab.scope();
      currentBB.add(Value.dummy, Op.PROLOGUE);
      int i = 0;
      for (Var v : node.getParams()) {
        Operand arg = null;
        switch (v.type) {
        case INT:
          arg = new Value();
          break;
        case BOOLEAN:
          arg = new Value(false);
          break;
        default:
          throw new TypeException(v.type, false, null);
        }
        currentBB.add(arg, Op.GET_ARG, new Imm64(i));
        i++;
      }
      node.body.accept(this);
      symtab.unscope();
    }
  }

  Operand compile(ExpressionNode e) {
    returnValue = null;
    e.accept(this);
    Operand temp = returnValue;
    returnValue = null;
    return temp;
  }

  @Override
  protected void visit(Add node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.ADD, a, b);
  }

  @Override
  protected void visit(Sub node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.SUB, a, b);
  }

  @Override
  protected void visit(Mul node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.IMUL, a, b);
    currentBB.add(Value.dummy, Op.TEST, returnValue, returnValue);
  }

  @Override
  protected void visit(Div node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }
    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(new Instruction.DivInstruction(returnValue, a, b, false));
    currentBB.add(Value.dummy, Op.TEST, returnValue, returnValue);
  }

  @Override
  protected void visit(Mod node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }
    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(new Instruction.DivInstruction(returnValue, a, b, true));
    currentBB.add(Value.dummy, Op.TEST, returnValue, returnValue);
  }

  @Override
  protected void visit(Minus node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value();
    }

    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.NEG, b);
  }

  @Override
  protected void visit(IntLiteral node) {
    returnValue = new Imm64(node.value);
  }

  @Override
  protected void visit(BooleanLiteral node) {
    returnValue = new Imm8(node.value);
    currentBB.add(Value.dummy, Op.TEST, returnValue, returnValue);
  }

  @Override
  protected void visit(Eq node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETE);
  }

  @Override
  protected void visit(Ne node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETNE);
  }

  @Override
  protected void visit(Gt node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETG);
  }

  @Override
  protected void visit(Ge node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETGE);
  }

  @Override
  protected void visit(Lt node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETL);
  }

  @Override
  protected void visit(Le node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand a = compile(node.left);
    Operand b = compile(node.right);
    currentBB.add(Value.dummy, Op.CMP, a, b)
    .add(returnValue, Op.SETLE);
  }

  @Override
  protected void visit(And node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    BasicBlock falseBlock = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    Operand a = compile(node.left);
    currentBB.add(returnValue, Op.MOV, a)
    .add(Op.JE);

    currentBB.setTaken(exit);
    currentBB.setNotTaken(falseBlock);

    currentBB = falseBlock;
    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.MOV, b)
    .add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = exit;
  }

  @Override
  protected void visit(Or node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    BasicBlock falseBlock = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    Operand a = compile(node.left);
    currentBB.add(returnValue, Op.MOV, a)
    .add(Op.JNE);

    currentBB.setTaken(exit);
    currentBB.setNotTaken(falseBlock);

    currentBB = falseBlock;
    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.MOV, b)
    .add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = exit;
  }

  @Override
  protected void visit(Not node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = new Value(false);
    }

    Operand b = compile(node.right);
    currentBB.add(returnValue, Op.XOR, new Imm8(true), b);
  }

  @Override
  protected void visit(VarDecl node) {
    if (bssMode) {
      switch (node.var.type) {
      case INT:
        symtab.insert(node.var, new BSSObject(node.var.id, Operand.Type.r64));
        return;
      case BOOLEAN:
        symtab.insert(node.var, new BSSObject(node.var.id, Operand.Type.r8));
        return;
      case INTARRAY:
        symtab.insert(node.var, new BSSObject(node.var.id, Operand.Type.r64, node.var.length));
        return;
      case BOOLEANARRAY:
        symtab.insert(node.var, new BSSObject(node.var.id, Operand.Type.r8, node.var.length));
        return;
      default:
        throw new TypeException(node.var, new SourcePosition());
      }
    } else {
      Operand temp = null;
      switch (node.var.type) {
      case INT:
        temp = new Value();
        symtab.insert(node.var, temp);
        currentBB.add(Op.MOV, new Imm64(0), temp);
        return;
      case BOOLEAN:
        temp = new Value(false);
        symtab.insert(node.var, temp);
        currentBB.add(Op.MOV, new Imm64(0), temp);
        return;
      case INTARRAY:
        temp = new Array(Operand.Type.r64, node.var.length);
        symtab.insert(node.var, temp);
        break;
      case BOOLEANARRAY:
        temp = new Array(Operand.Type.r8, node.var.length);
        symtab.insert(node.var, temp);
        break;
      default:
        throw new TypeException(node.var, new SourcePosition());
      }
      BasicBlock loop = new BasicBlock();
      BasicBlock exit = new BasicBlock();

      Operand i = new Value();
      currentBB.add(Op.MOV, new Imm64(node.var.length - 1), i)
      .add(Op.JMP);
      currentBB.setTaken(loop);

      currentBB = loop;
      currentBB.add(new Imm64(0), Op.STORE, temp, i)
      .add(Op.DEC, i)
      .add(Op.JGE);
      currentBB.setTaken(loop);
      currentBB.setNotTaken(exit);

      currentBB = exit;
    }
  }

  @Override
  protected void visit(VarExpr node) {
    returnValue = symtab.lookup(node.var);
  }

  @Override
  protected void visit(Assign node) {
    currentAssignDest = symtab.lookup(node.var);

    Operand value = compile(node.value);
    Operand dest = symtab.lookup(node.var);

    if (value != dest) {
      currentBB.add(dest, Op.MOV, value);
    }

    currentAssignDest = null;
  }

  @Override
  protected void visit(Load node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = node.array.type.getElementType() == Type.INT ? new Value() : new Value(false);
    }

    Operand index = compile(node.index);
    long length = node.array.length;
    if (node.checkBounds) {
      BasicBlock check1 = new BasicBlock();
      BasicBlock ok = new BasicBlock();
      BasicBlock die = new BasicBlock();

      currentBB.add(Value.dummy, Op.CMP, index, new Imm64(0))
      .add(Op.JL);
      currentBB.setTaken(die);
      currentBB.setNotTaken(check1);

      currentBB = check1;
      currentBB.add(Value.dummy, Op.CMP, index, new Imm64(length))
      .add(Op.JGE);
      currentBB.setTaken(die);
      currentBB.setNotTaken(ok);

      currentBB = die;
      currentBB.priority = 1000000000;
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      String l = pos.lineNum == 0 ? "??" : String.valueOf(pos.lineNum);
      String c = pos.colNum == 0 ? "??" : String.valueOf(pos.colNum);
      currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new Imm64(Util.strToBytes(l)), new Imm64(Util.strToBytes(c)))
      .add(Op.RET);

      currentBB = ok;
    }
    Operand base = symtab.lookup(node.array);
    currentBB.add(returnValue, Op.LOAD, base, index);
  }

  @Override
  protected void visit(Store node) {
    Operand index = compile(node.index);
    long length = node.array.length;
    if (node.checkBounds) {
      BasicBlock check1 = new BasicBlock();
      BasicBlock ok = new BasicBlock();
      BasicBlock die = new BasicBlock();

      currentBB.add(Value.dummy, Op.CMP, index, new Imm64(0))
      .add(Op.JL);
      currentBB.setTaken(die);
      currentBB.setNotTaken(check1);

      currentBB = check1;
      currentBB.add(Value.dummy, Op.CMP, index, new Imm64(length))
      .add(Op.JGE);
      currentBB.setTaken(die);
      currentBB.setNotTaken(ok);

      currentBB = die;
      currentBB.priority = 1000000000;
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      String l = pos.lineNum == 0 ? "??" : String.valueOf(pos.lineNum);
      String c = pos.colNum == 0 ? "??" : String.valueOf(pos.colNum);
      currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new Imm64(Util.strToBytes(l)), new Imm64(Util.strToBytes(c)))
      .add(Op.RET);

      currentBB = ok;
    }
    Operand value = compile(node.value);
    Operand base = symtab.lookup(node.array);
    Operand oldValue = node.array.type.getElementType() == Type.INT ? new Value() : new Value(false);
    Operand newValue = node.array.type.getElementType() == Type.INT ? new Value() : new Value(false);
    if (node.cop == CoOperand.PLUS) {
      currentBB.add(oldValue, Op.LOAD, base, index)
      .add(newValue, Op.ADD, oldValue, value);
    } else if (node.cop == CoOperand.MINUS) {
      currentBB.add(oldValue, Op.LOAD, base, index)
      .add(newValue, Op.SUB, oldValue, value);
    } else {
      newValue = value;
    }
    currentBB.add(newValue, Op.STORE, base, index);
  }

  @Override
  protected void visit(Block node) {
    symtab.scope();
    super.visit(node);
    symtab.unscope();
  }

  @Override
  protected void visit(Break node) {
    currentBB.add(Op.JMP);
    currentBB.setTaken(breakBB);
  }

  @Override
  protected void visit(Continue node) {
    currentBB.add(Op.JMP);
    currentBB.setTaken(continueBB);
  }

  @Override
  protected void visit(Die node) {
    if (node.exitCode == Die.CONTROL_REACHES_END_OF_NON_VOID_FUNCTION) {
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      String l = pos.lineNum == 0 ? "??" : String.valueOf(pos.lineNum);
      String c = pos.colNum == 0 ? "??" : String.valueOf(pos.colNum);
      currentBB.add(currentFunctionName, Op.CONTROL_REACHES_END, new Imm64(Util.strToBytes(l)),
          new Imm64(Util.strToBytes(c)))
      .add(Op.RET);
    } else {
      ArrayList<Operand> args = new ArrayList<>();
      args.add(new Imm64(node.exitCode));
      currentBB.add(new Instruction.CallInstruction(Value.dummy, new Symbol("exit"), args, false, 0));
      currentBB.add(Op.RET);
    }
  }

  @Override
  protected void visit(Call node) {
    if (node.func.returnType != Type.NONE) {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = node.func.returnType == Type.INT ? new Value() : new Value(false);
      }
      currentBB.add(Op.MOV, Register.rax, returnValue);
    }

    // final int offset = (Math.max(6, node.args.size()) - 6) * 8;
    currentBB.add(Value.dummy, Op.ALLOCATE, new Imm64(9 + Math.max(6, node.args.size()) - 6));
    ArrayList<Operand> args = new ArrayList<>();
    for (ExpressionNode e : node.args) {
      args.add(compile(e));
    }


    boolean variadic = node.func.isCallout && (node.func.id.contains("printf") || node.func.id.contains("scanf"));
    currentBB
    .add(new Instruction.CallInstruction(returnValue, new Symbol(node.func.getMangledName()), args, variadic, 0));


    /*.add(Op.MOV, Register.rax, new Memory(Register.rsp, offset, Operand.Type.r64))
    .add(Op.MOV, Register.rcx, new Memory(Register.rsp, offset + 8, Operand.Type.r64))
    .add(Op.MOV, Register.rdx, new Memory(Register.rsp, offset + 16, Operand.Type.r64))
    .add(Op.MOV, Register.rdi, new Memory(Register.rsp, offset + 24, Operand.Type.r64))
    .add(Op.MOV, Register.rsi, new Memory(Register.rsp, offset + 32, Operand.Type.r64))
    .add(Op.MOV, Register.r8, new Memory(Register.rsp, offset + 40, Operand.Type.r64))
    .add(Op.MOV, Register.r9, new Memory(Register.rsp, offset + 48, Operand.Type.r64))
    .add(Op.MOV, Register.r10, new Memory(Register.rsp, offset + 56, Operand.Type.r64))
    .add(Op.MOV, Register.r11, new Memory(Register.rsp, offset + 64, Operand.Type.r64));
     */


    /*switch (node.args.size()) { // fallthrough
    default:
      for (int i = 6; i < node.args.size(); i++) {
        currentBB.add(new Memory(Register.rsp, (i - 6) * 8, Operand.Type.r64), Op.MOV, args.get(i));
      }
    case 6:
      currentBB.add(Op.MOV, args.get(5), Register.r9);
    case 5:
      currentBB.add(Op.MOV, args.get(4), Register.r8);
    case 4:
      currentBB.add(Op.MOV, args.get(3), Register.rcx);
    case 3:
      currentBB.add(Op.MOV, args.get(2), Register.rdx);
    case 2:
      currentBB.add(Op.MOV, args.get(1), Register.rsi);
    case 1:
      currentBB.add(Op.MOV, args.get(0), Register.rdi);
    case 0:
    }
    if (node.func.isCallout && (node.func.id.contains("printf") || node.func.id.contains("scanf"))) {
      currentBB.add(Op.XOR, Register.rax, Register.rax);
    }
    currentBB.add(Op.CALL, new Symbol(node.---
    if (node.func.returnType != Type.NONE) {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = node.func.returnType == Type.INT ? new Value() : new Value(false);
      }
      currentBB.add(Op.MOV, Register.rax, returnValue);
    }

    currentBB.add(Op.MOV, new Memory(Register.rsp, offset + 64, Operand.Type.r64), Register.r11)
    .add(Op.MOV, new Memory(Register.rsp, offset + 56, Operand.Type.r64), Register.r10)
    .add(Op.MOV, new Memory(Register.rsp, offset + 48, Operand.Type.r64), Register.r9)
    .add(Op.MOV, new Memory(Register.rsp, offset + 40, Operand.Type.r64), Register.r8)
    .add(Op.MOV, new Memory(Register.rsp, offset + 32, Operand.Type.r64), Register.rsi)
    .add(Op.MOV, new Memory(Register.rsp, offset + 24, Operand.Type.r64), Register.rdi)
    .add(Op.MOV, new Memory(Register.rsp, offset + 16, Operand.Type.r64), Register.rdx)
    .add(Op.MOV, new Memory(Register.rsp, offset + 8, Operand.Type.r64), Register.rcx)
    .add(Op.MOV, new Memory(Register.rsp, offset, Operand.Type.r64), Register.rax)

    .add(Value.dummy, Op.DEALLOCATE, new Imm64(9 + Math.max(6, node.args.size()) - 6));*/
  }

  @Override
  protected void visit(CallStmt node) {
    visit(node.call);
  }

  @Override
  protected void visit(For node) {
    BasicBlock pushBreak = breakBB;
    BasicBlock pushContinue = continueBB;

    BasicBlock cond = new BasicBlock();
    BasicBlock body = new BasicBlock();
    BasicBlock incr = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    breakBB = exit;
    continueBB = incr;

    Operand init = compile(node.init);
    Operand end = compile(node.end);
    Operand i = symtab.lookup(node.loopVar);

    currentBB.add(i, Op.MOV, init)
    .add(Op.JMP);
    currentBB.setTaken(cond);

    currentBB = cond;
    currentBB.add(Value.dummy, Op.CMP, i, end)
    .add(Op.JGE);
    currentBB.setTaken(exit);
    currentBB.setNotTaken(body);

    currentBB = body;
    node.body.accept(this);
    currentBB.add(Op.JMP);
    currentBB.setTaken(incr);

    currentBB = incr;
    if (node.increment == 1) {
      currentBB.add(Op.INC, i);
    } else {
      currentBB.add(i, Op.ADD, new Imm64(node.increment), i);
    }
    currentBB.add(Op.JMP);
    currentBB.setTaken(cond);

    currentBB = exit;

    breakBB = pushBreak;
    continueBB = pushContinue;
  }

  @Override
  protected void visit(While node) {
    BasicBlock pushBreak = breakBB;
    BasicBlock pushContinue = continueBB;

    BasicBlock cond = new BasicBlock();
    BasicBlock body = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    breakBB = exit;
    continueBB = cond;

    currentBB.add(Op.JMP);
    currentBB.setTaken(cond);

    currentBB = cond;
    compile(node.cond);
    currentBB.add(Op.JE);
    currentBB.setTaken(exit);
    currentBB.setNotTaken(body);

    currentBB = body;
    node.body.accept(this);
    currentBB.add(Op.JMP);
    currentBB.setTaken(cond);

    currentBB = exit;

    breakBB = pushBreak;
    continueBB = pushContinue;
  }

  @Override
  protected void visit(If node) {
    BasicBlock t = new BasicBlock();
    BasicBlock f = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    compile(node.cond);
    currentBB.add(Op.JE);
    currentBB.setTaken(f);
    currentBB.setNotTaken(t);

    currentBB = t;
    node.trueBlock.accept(this);
    currentBB.add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = f;
    node.falseBlock.accept(this);
    currentBB.add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = exit;
  }

  @Override
  protected void visit(Ternary node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = node.trueExpr.getType() == Type.INT ? new Value() : new Value(false);
    }

    BasicBlock t = new BasicBlock();
    BasicBlock f = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    compile(node.cond);
    currentBB.add(Op.JE);
    currentBB.setTaken(f);
    currentBB.setNotTaken(t);

    currentBB = t;
    Operand trueVal = compile(node.trueExpr);
    currentBB.add(returnValue, Op.MOV, trueVal);
    currentBB.add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = f;
    Operand falseVal = compile(node.falseExpr);
    currentBB.add(returnValue, Op.MOV, falseVal);
    currentBB.add(Op.JMP);
    currentBB.setTaken(exit);

    currentBB = exit;
  }

  @Override
  protected void visit(Length node) {
    returnValue = new Imm64(node.array.length);
  }

  @Override
  protected void visit(Pass node) {
  }

  @Override
  protected void visit(Return node) {
    if (node.value != null) {
      currentAssignDest = Register.rax;
      compile(node.value);
    }
    currentBB.add(Value.dummy, Op.EPILOGUE)
    .add(Op.RET);
  }

  @Override
  protected void visit(StringLiteral node) {
    returnValue = strtab.get(node.value);
    if (returnValue == null) {
      returnValue = new BSSObject(node.value);
      strtab.put(node.value, returnValue);
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    throw new RuntimeException("UnparsedIntLiteral is not allowed in backend");
  }
}
