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
  BasicBlock trueTarget, falseTarget;
  HashSet<BasicBlock> funcExits;

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
  String fileName;

  public Midend(String filename) {
    this.fileName = filename;
  }

  Operand compile(ExpressionNode e) {
    Operand pushReturnValue = returnValue;
    returnValue = null;
    e.accept(this);
    Operand temp = returnValue;
    returnValue = pushReturnValue;
    return temp;
  }

  @Override
  protected void visit(Program node) {
    BasicBlock.priorityCounter = 0;

    strtab = new HashMap<>();

    outOfBounds = new BasicBlock();
    Operand s1 = compile(new StringLiteral("\"%s(): %s:%s: Array index out of bounds\\n\"", null).box());
    outOfBounds.add(new Instruction(Op.MOV, s1, Register.rdi));
    outOfBounds.add(new Instruction(Op.XOR, Register.rax, Register.rax));
    outOfBounds.add(new Instruction(Op.CALL, new Symbol("printf")));
    outOfBounds.add(new Instruction(Op.MOV, new Imm64(-1), Register.rax));
    outOfBounds.add(new Instruction(Op.CALL, new Symbol("exit")));
    outOfBounds.add(new Instruction(Op.NO_RETURN));

    controlReachesEnd = new BasicBlock();
    Operand s2 = compile(new StringLiteral("\"%s(): %s:%s: Control reaches end of non-void function\\n\"", null).box());
    controlReachesEnd.add(new Instruction(Op.MOV, s1, Register.rdi));
    controlReachesEnd.add(new Instruction(Op.XOR, Register.rax, Register.rax));
    controlReachesEnd.add(new Instruction(Op.CALL, new Symbol("printf")));
    controlReachesEnd.add(new Instruction(Op.MOV, new Imm64(-2), Register.rax));
    controlReachesEnd.add(new Instruction(Op.CALL, new Symbol("exit")));
    controlReachesEnd.add(new Instruction(Op.NO_RETURN));

    currentBB = new BasicBlock(); // dummy, because var decl automatic emits initialization code,
    // but .bss segment is set to 0 anyway

    symtab = new ScopedMap<>();
    bssMode = true;
    for (StatementNode s : node.varDecls) {
      s.accept(this);
    }
    bssMode = false;

    ArrayList<CFG.CFGDesc> entries = new ArrayList<>();
    for (FunctionNode f : node.functions) {
      if (!((Function)(f.getNode())).isCallout) {
        f.accept(this);
        assert(funcEntryBB != null);
        entries.add(new CFG.CFGDesc(funcEntryBB, funcExits));
      }
    }
    entries.add(new CFG.CFGDesc(outOfBounds, new HashSet<BasicBlock>(){{add(outOfBounds);}}));
    entries.add(new CFG.CFGDesc(controlReachesEnd, new HashSet<BasicBlock>(){{add(controlReachesEnd);}}));

    cfg = new CFG(entries, symtab, strtab);
    cfg.fileName = fileName;
  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      currentBB = null;
      funcEntryBB = null;
      funcExits = new HashSet<>();
      BasicBlock.priorityCounter = 0;

      currentFunctionName = compile(new StringLiteral("\"" + node.getMangledName() + "\"", null).box());

      currentBB = new BasicBlock(node.getMangledName());
      funcEntryBB = currentBB;
      symtab = symtab.scope();
      currentBB.add(Op.PROLOGUE);
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
        symtab.insert(v, arg);
        i++;
      }
      node.body.accept(this);
      symtab = symtab.unscope();
    }
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
    currentBB.add(new Instruction.DivInstruction(returnValue, Value.dummy, a, b));
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
    currentBB.add(new Instruction.DivInstruction(Value.dummy, returnValue, a, b));
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
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
      currentBB.add(returnValue, Op.MOV, new Imm64(node.value));
    } else {
      returnValue = new Imm64(node.value);
    }
  }

  @Override
  protected void visit(BooleanLiteral node) {
    if (trueTarget != null) {
      if (node.value == true) {
        currentBB.addJmp(trueTarget);
      } else {
        currentBB.addJmp(falseTarget);
      }
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
        currentBB.add(returnValue, Op.MOV, new Imm8(node.value));
      } else {
        returnValue = new Imm8(node.value);
      }
    }
  }

  @Override
  protected void visit(Eq node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JE, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETE);
    }
  }

  @Override
  protected void visit(Ne node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JNE, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETNE);
    }
  }

  @Override
  protected void visit(Gt node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JG, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETG);
    }
  }

  @Override
  protected void visit(Ge node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JGE, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETGE);
    }
  }

  @Override
  protected void visit(Lt node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JL, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETL);
    }
  }

  @Override
  protected void visit(Le node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = null;
      falseTarget = null;
      Operand a = compile(node.left);
      trueTarget = null;
      falseTarget = null;
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .addJmp(Op.JLE, pushTrue, pushFalse);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      Operand a = compile(node.left);
      Operand b = compile(node.right);
      currentBB.add(Op.CMP, a, b)
      .add(returnValue, Op.SETLE);
    }
  }

  @Override
  protected void visit(And node) {
    if (trueTarget != null) {
      BasicBlock right = new BasicBlock();

      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      trueTarget = right;
      compile(node.left);

      currentBB = right;
      trueTarget = pushTrue;
      falseTarget = pushFalse;
      compile(node.right);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      BasicBlock right = new BasicBlock();
      BasicBlock mov0 = new BasicBlock();
      BasicBlock mov1 = new BasicBlock();
      BasicBlock exit = new BasicBlock();

      mov0.add(returnValue, Op.MOV, new Imm8(false));
      mov0.addJmp(exit);

      mov1.add(returnValue, Op.MOV, new Imm8(true));
      mov1.addJmp(exit);

      trueTarget = right;
      falseTarget = mov0;
      compile(node.left);

      currentBB = right;
      trueTarget = mov1;
      falseTarget = mov0;
      compile(node.right);
      trueTarget = null;
      falseTarget = null;

      currentBB = exit;
    }
  }

  @Override
  protected void visit(Or node) {
    if (trueTarget != null) {
      BasicBlock right = new BasicBlock();

      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;
      falseTarget = right;
      compile(node.left);

      currentBB = right;
      trueTarget = pushTrue;
      falseTarget = pushFalse;
      compile(node.right);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }

      BasicBlock right = new BasicBlock();
      BasicBlock mov0 = new BasicBlock();
      BasicBlock mov1 = new BasicBlock();
      BasicBlock exit = new BasicBlock();

      mov0.add(returnValue, Op.MOV, new Imm8(false));
      mov0.addJmp(exit);

      mov1.add(returnValue, Op.MOV, new Imm8(true));
      mov1.addJmp(exit);

      trueTarget = mov1;
      falseTarget = right;
      compile(node.left);

      currentBB = right;
      trueTarget = mov1;
      falseTarget = mov0;
      compile(node.right);
      trueTarget = null;
      falseTarget = null;

      currentBB = exit;
    }
  }

  @Override
  protected void visit(Not node) {
    if (trueTarget != null) {
      BasicBlock pushTrue = trueTarget;
      BasicBlock pushFalse = falseTarget;

      trueTarget = pushFalse;
      falseTarget = pushTrue;
      compile(node.right);

    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = new Value(false);
      }
      Operand b = compile(node.right);
      currentBB.add(returnValue, Op.XOR, new Imm8(true), b);
    }
  }

  @Override
  protected void visit(If node) {
    BasicBlock t = new BasicBlock();
    BasicBlock f = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    trueTarget = t;
    falseTarget = f;
    compile(node.cond);
    trueTarget = null;
    falseTarget = null;

    t.deferPriority();
    f.deferPriority();
    exit.deferPriority();

    currentBB = t;
    node.trueBlock.accept(this);
    currentBB.addJmp(exit);

    currentBB = f;
    node.falseBlock.accept(this);
    currentBB.addJmp(exit);

    currentBB = exit;
  }

  @Override
  protected void visit(Ternary node) {
    BasicBlock t = new BasicBlock();
    BasicBlock f = new BasicBlock();

    if (trueTarget != null) {
      assert (node.getType() == Type.BOOLEAN);

      BasicBlock pushTrueTarget = trueTarget;
      BasicBlock pushFalseTarget = falseTarget;

      trueTarget = t;
      falseTarget = f;
      compile(node.cond);

      t.deferPriority();
      f.deferPriority();

      currentBB = t;
      trueTarget = pushTrueTarget;
      falseTarget = pushFalseTarget;
      compile(node.trueExpr);

      currentBB = f;
      trueTarget = pushTrueTarget;
      falseTarget = pushFalseTarget;
      compile(node.falseExpr);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = node.trueExpr.getType() == Type.INT ? new Value() : new Value(false);
      }

      BasicBlock exit = new BasicBlock();

      trueTarget = t;
      falseTarget = f;
      compile(node.cond);

      t.deferPriority();
      f.deferPriority();
      exit.deferPriority();

      currentBB = t;
      currentAssignDest = returnValue;
      compile(node.trueExpr);
      currentBB.addJmp(exit);

      currentBB = f;
      currentAssignDest = returnValue;
      compile(node.falseExpr);
      currentBB.addJmp(exit);

      currentBB = exit;
    }
  }

  @Override
  protected void visit(VarDecl node) {
    if (bssMode) {
      String mangled = node.var.getMangledName();
      switch (node.var.type) {
      case INT:
        symtab.insert(node.var, new BSSObject(mangled, Operand.Type.r64));
        return;
      case BOOLEAN:
        symtab.insert(node.var, new BSSObject(mangled, Operand.Type.r8));
        return;
      case INTARRAY:
        symtab.insert(node.var, new BSSObject(mangled, Operand.Type.r64, node.var.length));
        return;
      case BOOLEANARRAY:
        symtab.insert(node.var, new BSSObject(mangled, Operand.Type.r8, node.var.length));
        return;
      default:
        throw new TypeException(node.var, new SourcePosition());
      }
    } else {
      Operand zero = (node.var.type == Type.INT || node.var.type == Type.INTARRAY ? new Imm64(0) : new Imm8(false));

      Operand temp = null;
      switch (node.var.type) {
      case INT:
        temp = new Value();
        symtab.insert(node.var, temp);
        currentBB.add(temp, Op.MOV, zero);
        return;
      case BOOLEAN:
        temp = new Value(false);
        symtab.insert(node.var, temp);
        currentBB.add(temp, Op.MOV, zero);
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
      currentBB.add(i, Op.MOV, new Imm64(node.var.length - 1))
      .addJmp(loop);

      currentBB = loop;
      currentBB.add(zero, Op.STORE, temp, i)
      .add(i, Op.DEC, i)
      .addJmp(Op.JGE, loop, exit);

      currentBB = exit;
    }
  }

  @Override
  protected void visit(VarExpr node) {
    if (trueTarget != null) {
      currentBB.add(Op.TEST, symtab.lookup(node.var), symtab.lookup(node.var));
      currentBB.addJmp(Op.JNE, trueTarget, falseTarget);
    } else {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
        currentBB.add(returnValue, Op.MOV, symtab.lookup(node.var));
      } else {
        returnValue = symtab.lookup(node.var);
      }
    }
  }

  @Override
  protected void visit(Assign node) {
    currentAssignDest = symtab.lookup(node.var);
    Operand pushCurrent = currentAssignDest;
    Operand value = compile(node.value);
    assert (value == pushCurrent);
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

      currentBB.add(Op.CMP, index, new Imm64(0))
      .addJmp(Op.JL, die, check1);

      currentBB = check1;
      currentBB.add(Op.CMP, index, new Imm64(length))
      .addJmp(Op.JGE, die, ok);

      currentBB = die;
      currentBB.priority = 1000000000;
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new Imm64(pos.lineNum), new Imm64(pos.colNum))
      .add(Op.NO_RETURN);
      funcExits.add(currentBB);

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

      currentBB.add(Op.CMP, index, new Imm64(0))
      .addJmp(Op.JL, die, check1);

      currentBB = check1;
      currentBB.add(Op.CMP, index, new Imm64(length))
      .addJmp(Op.JGE, die, ok);

      currentBB = die;
      currentBB.priority = 1000000000;
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new Imm64(pos.lineNum), new Imm64(pos.colNum))
      .add(Op.NO_RETURN);
      funcExits.add(currentBB);

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
    currentBB.addJmp(breakBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Continue node) {
    currentBB.addJmp(continueBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Die node) {
    if (node.exitCode == Die.CONTROL_REACHES_END_OF_NON_VOID_FUNCTION) {
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      currentBB.add(currentFunctionName, Op.CONTROL_REACHES_END, new Imm64(pos.lineNum), new Imm64(pos.colNum))
      .add(Op.NO_RETURN);
    } else {
      currentBB.add(new Instruction.CallInstruction(Value.dummy, new Symbol("exit"),
          new ArrayList<Operand>(){{add(new Imm64(node.exitCode));}}, false, 0));
      currentBB.add(Op.NO_RETURN);
    }
    funcExits.add(currentBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Call node) {
    if (node.func.returnType != Type.NONE) {
      if (currentAssignDest != null) {
        returnValue = currentAssignDest;
        currentAssignDest = null;
      } else {
        returnValue = node.func.returnType == Type.BOOLEAN ? new Value(false) : new Value();
      }
    }

    BasicBlock pushTrueTarget = trueTarget;
    BasicBlock pushFalseTarget = falseTarget;

    currentBB.add(Op.ALLOCATE, new Imm64(Math.max(6, node.args.size()) - 6));
    ArrayList<Operand> args = new ArrayList<>();
    for (ExpressionNode e : node.args) {
      args.add(compile(e));
    }

    // final int offset = (Math.max(6, node.args.size()) - 6) * 8;
    /*BasicBlock next = new BasicBlock();
    currentBB.add(Op.JMP);
    currentBB.taken = next;

    currentBB = next;*/


    boolean variadic = node.func.isCallout && (node.func.id.contains("printf") || node.func.id.contains("scanf"));
    currentBB.add(new Instruction.CallInstruction(returnValue, new Symbol(node.func.getMangledName()), args, variadic, 0));

    trueTarget = pushTrueTarget;
    falseTarget = pushFalseTarget;

    if (trueTarget != null) {
      currentBB.add(new Instruction(Op.TEST, Register.rax, Register.rax));
      currentBB.addJmp(Op.JNE, trueTarget, falseTarget);
    }

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
    currentAssignDest = Value.dummy;
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
    .addJmp(cond);

    currentBB = cond;
    currentBB.add(Op.CMP, i, end)
    .addJmp(Op.JGE, exit, body);

    currentBB = body;
    body.deferPriority();
    node.body.accept(this);
    currentBB.addJmp(incr);

    currentBB = incr;
    incr.deferPriority();
    if (node.increment == 1) {
      currentBB.add(i, Op.INC, i);
    } else {
      currentBB.add(i, Op.ADD, new Imm64(node.increment), i);
    }
    currentBB.addJmp(cond);

    currentBB = exit;
    exit.deferPriority();
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

    currentBB.addJmp(cond);

    currentBB = cond;
    trueTarget = body;
    falseTarget = exit;
    compile(node.cond);
    trueTarget = null;
    falseTarget = null;

    body.deferPriority();
    exit.deferPriority();

    currentBB = body;
    node.body.accept(this);
    currentBB.addJmp(cond);

    currentBB = exit;

    breakBB = pushBreak;
    continueBB = pushContinue;
  }

  @Override
  protected void visit(Length node) {
    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
      currentBB.add(returnValue, Op.MOV, new Imm64(node.array.length));
    } else {
      returnValue = new Imm64(node.array.length);
    }
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
    .add(new Instruction(Op.RET));
    funcExits.add(currentBB);

    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(StringLiteral node) {
    returnValue = strtab.get(node.toEscapedString());
    if (returnValue == null) {
      returnValue = new StringObject(node.toEscapedString());
      strtab.put(node.value, returnValue);
    }
  }

  @Override
  protected void visit(UnparsedIntLiteral node) {
    throw new RuntimeException("UnparsedIntLiteral is not allowed in backend");
  }
}
