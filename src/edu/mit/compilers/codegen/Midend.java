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
  boolean isMain = false;

  // Const

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

  CFG.CFGDesc getExitBlock() {
    Operand s0 = compile(new StringLiteral("\"Array index out of bounds\\n\"", null).box());
    Operand s1 = compile(new StringLiteral("\"Control reaches end of non-void function\\n\"", null).box());
    Operand s2 = compile(new StringLiteral("\"Array index out of bounds\\n\"", null).box());
    Operand s3 = compile(new StringLiteral("\"Control reaches end of non-void function\\n\"", null).box());
    final BasicBlock dummy0 = new BasicBlock();
    BasicBlock dummy1 = new BasicBlock();
    BasicBlock dummy2 = new BasicBlock();
    BasicBlock exit0 = new BasicBlock("_exit.0");
    BasicBlock exit1 = new BasicBlock("_exit.1");
    BasicBlock exit2 = new BasicBlock("_exit.2");
    BasicBlock exit3 = new BasicBlock("_exit.3");
    BasicBlock exit = new BasicBlock("_exit.4");
    exit0.add(new Instruction(Op.MOV, s0, Register.rdi));
    exit0.add(new Instruction(Op.MOV, new Imm64(-1), Register.rbx));
    exit0.add(new Instruction(Op.MOV, new Memory(Register.rsp, 0, Operand.Type.r64), Register.rsi));
    exit0.addJmp(exit);

    exit1.add(new Instruction(Op.MOV, s1, Register.rdi));
    exit1.add(new Instruction(Op.MOV, new Imm64(-2), Register.rbx));
    exit1.add(new Instruction(Op.MOV, new Memory(Register.rsp, 0, Operand.Type.r64), Register.rsi));
    exit1.addJmp(exit);

    exit2.add(new Instruction(Op.MOV, s2, Register.rdi));
    exit2.add(new Instruction(Op.MOV, new Imm64(-1), Register.rbx));
    exit2.addJmp(exit);

    exit3.add(new Instruction(Op.MOV, s3, Register.rdi));
    exit3.add(new Instruction(Op.MOV, new Imm64(-2), Register.rbx));
    exit3.addJmp(exit);

    exit.add(new Instruction(Op.XOR, Register.rax, Register.rax));
    exit.add(new Instruction(Op.CALL, new Symbol("printf")));
    exit.add(new Instruction(Op.MOV, Register.rbx, Register.rdi));
    exit.add(new Instruction(Op.CALL, new Symbol("exit")));
    exit.add(new Instruction(Op.NO_RETURN));

    dummy0.addComment("error handling code");
    dummy0.addJmp(Op.JE, dummy1, dummy2);
    dummy1.addJmp(Op.JE, exit0, exit1);
    dummy2.addJmp(Op.JE, exit2, exit3);
    return new CFG.CFGDesc(dummy0, new HashSet<BasicBlock>() {
      {
        add(dummy0);
      }
    });
  }

  @Override
  protected void visit(Program node) {
    BasicBlock.priorityCounter = 0;

    strtab = new HashMap<>();

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
    entries.add(getExitBlock());

    cfg = new CFG(entries, symtab, strtab);
    cfg.fileName = fileName;
  }

  @Override
  protected void visit(Function node) {
    if (!node.isCallout) {
      if (node.id.equals("main")) {
        isMain = true;
      } else {
        isMain = false;
      }
      currentBB = null;
      funcEntryBB = null;
      funcExits = new HashSet<>();
      BasicBlock.priorityCounter = 0;

      currentFunctionName = compile(new StringLiteral("\"" + node.getMangledName() + "\"", null).box());

      currentBB = new BasicBlock(node.getMangledName());
      currentBB.addComment(node.getSignature());
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
      /*
       * currentBB.add(new Value(), Op.MOV, Register.rbx);
       * currentBB.add(new Value(), Op.MOV, Register.r12);
       * currentBB.add(new Value(), Op.MOV, Register.r13);
       * currentBB.add(new Value(), Op.MOV, Register.r14);
       * currentBB.add(new Value(), Op.MOV, Register.r15);
       */
      node.body.accept(this);
      symtab = symtab.unscope();
      isMain = false;
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
    currentBB.add(returnValue, Op.ADD, b, a);
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
    currentBB.add(returnValue, Op.SUB, b, a);
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
    currentBB.add(returnValue, Op.IMUL, b, a);
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
      currentBB.add(Op.CMP, b, a)
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
    currentBB.addComment("if (" + node.cond.toString() + ") {");

    BasicBlock t = new BasicBlock();
    BasicBlock f = new BasicBlock();
    BasicBlock exit = new BasicBlock();

    trueTarget = t;
    falseTarget = f;
    compile(node.cond);
    trueTarget = null;
    falseTarget = null;

    currentBB = t;
    t.deferPriority();
    node.trueBlock.accept(this);
    currentBB.addJmp(exit);

    trueTarget = null;
    falseTarget = null;

    currentBB = f;
    currentBB.addComment("} else {");
    f.deferPriority();
    node.falseBlock.accept(this);
    currentBB.addJmp(exit);

    currentBB = exit;
    exit.deferPriority();
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

      currentBB = t;
      t.deferPriority();
      trueTarget = pushTrueTarget;
      falseTarget = pushFalseTarget;
      compile(node.trueExpr);

      currentBB = f;
      f.deferPriority();
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

      trueTarget = null;
      falseTarget = null;
      currentBB = t;
      t.deferPriority();
      currentAssignDest = returnValue;
      compile(node.trueExpr);
      currentBB.addJmp(exit);

      trueTarget = null;
      falseTarget = null;
      currentBB = f;
      f.deferPriority();
      currentAssignDest = returnValue;
      compile(node.falseExpr);
      currentBB.addJmp(exit);

      currentBB = exit;
      exit.deferPriority();
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
      currentBB.addComment(node.toString());

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

      Value index = new Value();
      currentBB.add(temp, Op.LOCAL_ARRAY_DECL, new Imm64(node.var.length));
      currentBB.add(index, Op.MOV, new Imm64(node.var.length - 1))
      .addJmp(loop);

      currentBB = loop;
      currentBB.add(zero, Op.STORE, temp, index)
      .add(index, Op.SUB, new Imm64(1), index);
      currentBB.addJmp(Op.JGE, loop, exit);

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
    currentBB.addComment(node.toString());

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
    /*
     * if (node.checkBounds) {
     * BasicBlock check1 = new BasicBlock();
     * BasicBlock ok = new BasicBlock();
     * BasicBlock die = new BasicBlock();
     *
     * currentBB.add(Op.CMP, new Imm64(0), index)
     * .addJmp(Op.JL, die, check1);
     *
     * currentBB = check1;
     * currentBB.add(Op.CMP, new Imm64(length), index)
     * .addJmp(Op.JGE, die, ok);
     *
     * currentBB = die;
     * currentBB.priority = 1000000000;
     * SourcePosition pos = node.getSourcePosition();
     * if (pos == null) {
     * pos = new SourcePosition();
     * }
     * currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new
     * Imm64(pos.lineNum), new Imm64(pos.colNum))
     * .add(Op.NO_RETURN);
     * funcExits.add(currentBB);
     *
     * currentBB = ok;
     * }
     */
    currentBB.add(Value.dummy, Op.RANGE, index, new Imm64(node.array.length));
    Operand base = symtab.lookup(node.array);
    currentBB.add(returnValue, Op.LOAD, base, index);
  }

  @Override
  protected void visit(Store node) {
    currentBB.addComment(node.toString());

    Operand index = compile(node.index);
    long length = node.array.length;
    /*
     * if (node.checkBounds) {
     * BasicBlock check1 = new BasicBlock();
     * BasicBlock ok = new BasicBlock();
     * BasicBlock die = new BasicBlock();
     *
     * currentBB.add(Op.CMP, new Imm64(0), index)
     * .addJmp(Op.JL, die, check1);
     *
     * currentBB = check1;
     * currentBB.add(Op.CMP, new Imm64(length), index)
     * .addJmp(Op.JGE, die, ok);
     *
     * currentBB = die;
     * currentBB.priority = 1000000000;
     * SourcePosition pos = node.getSourcePosition();
     * if (pos == null) {
     * pos = new SourcePosition();
     * }
     * currentBB.add(currentFunctionName, Op.OUT_OF_BOUNDS, new
     * Imm64(pos.lineNum), new Imm64(pos.colNum))
     * .add(Op.NO_RETURN);
     * funcExits.add(currentBB);
     *
     * currentBB = ok;
     * }
     */
    currentBB.add(Value.dummy, Op.RANGE, index, new Imm64(node.array.length));

    Operand value = compile(node.value);
    Operand base = symtab.lookup(node.array);
    Operand oldValue = node.array.type.getElementType() == Type.INT ? new Value() : new Value(false);
    Operand newValue = node.array.type.getElementType() == Type.INT ? new Value() : new Value(false);
    if (node.cop == CoOperand.PLUS) {
      currentBB.add(oldValue, Op.LOAD, base, index)
      .add(newValue, Op.ADD, value, oldValue);
    } else if (node.cop == CoOperand.MINUS) {
      currentBB.add(oldValue, Op.LOAD, base, index)
      .add(newValue, Op.SUB, value, oldValue);
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
    currentBB.addComment(node.toString());
    currentBB.addJmp(breakBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Continue node) {
    currentBB.addComment(node.toString());
    currentBB.addJmp(continueBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Die node) {
    currentBB.addComment("exit(" + String.valueOf(node.exitCode) + ");");
    if (node.exitCode == Die.CONTROL_REACHES_END_OF_NON_VOID_FUNCTION) {
      SourcePosition pos = node.getSourcePosition();
      if (pos == null) {
        pos = new SourcePosition();
      }
      currentBB.add(currentFunctionName, Op.CONTROL_REACHES_END, new Imm64(pos.lineNum), new Imm64(pos.colNum))
      .add(Op.NO_RETURN);
    } else {
      final Imm64 imm = new Imm64(node.exitCode);
      
      currentBB.add(new Instruction.CallInstruction(Value.dummy, new Symbol("exit"),
          new ArrayList<Operand>(){{add(imm);}}, false, 0));
      currentBB.add(Op.NO_RETURN);
    }
    funcExits.add(currentBB);
    currentBB = new BasicBlock("");
  }

  @Override
  protected void visit(Call node) {
    BasicBlock next = new BasicBlock();
    currentBB.addJmp(next);
    currentBB = next;

    if (currentAssignDest != null) {
      returnValue = currentAssignDest;
      currentAssignDest = null;
    } else {
      returnValue = node.func.returnType == Type.BOOLEAN ? new Value(false) : new Value();
    }

    Operand pushReturnValue = returnValue;

    BasicBlock pushTrueTarget = trueTarget;
    BasicBlock pushFalseTarget = falseTarget;

    ArrayList<Operand> args = new ArrayList<>();
    for (ExpressionNode e : node.args) {
      trueTarget = null;
      falseTarget = null;
      Operand value = compile(e);
      assert (value != null);
      args.add(value);
    }

    returnValue = pushReturnValue;

    currentBB.add(Op.ALLOCATE, new Imm64((Math.max(6, node.args.size()) - 6) * 8));
    boolean variadic = node.func.isCallout && (node.func.id.contains("printf") || node.func.id.contains("scanf"));
    currentBB.add(new Instruction.CallInstruction(returnValue, new Symbol(node.func.getMangledName()), args, variadic, 0));

    trueTarget = pushTrueTarget;
    falseTarget = pushFalseTarget;

    if (trueTarget != null) {
      if (node.func.returnType == Type.BOOLEAN) {
        currentBB.add(new Instruction(Op.TEST, Register.al, Register.al));
      } else {
        currentBB.add(new Instruction(Op.TEST, Register.rax, Register.rax));
      }
      currentBB.addJmp(Op.JNE, trueTarget, falseTarget);
    }
  }

  @Override
  protected void visit(CallStmt node) {
    currentBB.addComment(node.toString());
    currentAssignDest = node.call.getType() == Type.BOOLEAN ? new Value(false) : new Value();
    visit(node.call);
  }

  @Override
  protected void visit(For node) {
    currentBB.addComment("for (" + node.loopVar.toString() + " = " + node.init.toString() + ", " + node.end.toString()
    + ", " + String.valueOf(node.increment) + ") {");

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
    .add(i, Op.LOOP_START)
    .addJmp(cond);

    currentBB = cond;
    currentBB.add(Op.CMP, end, i)
    .addJmp(Op.JGE, exit, body);

    currentBB = body;
    body.deferPriority();
    node.body.accept(this);
    currentBB.addJmp(incr);

    currentBB = incr;
    incr.deferPriority();

    currentBB.add(i, Op.ADD, new Imm64(node.increment), i);

    currentBB.addJmp(cond);

    currentBB = exit;
    exit.deferPriority();
    breakBB = pushBreak;
    continueBB = pushContinue;
    currentBB.add(i, Op.LOOP_END);
  }

  @Override
  protected void visit(While node) {
    currentBB.addComment("while (" + node.cond.toString() + ") {");

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

    currentBB = body;
    body.deferPriority();
    node.body.accept(this);
    currentBB.addJmp(cond);

    currentBB = exit;
    exit.deferPriority();

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
    currentBB.add(Value.dummy, Op.EPILOGUE);

    if (isMain) {
      currentBB.add(Register.rax, Op.MOV, new Imm64(0));
    }

    currentBB.add(new Instruction(Op.RET));
    funcExits.add(currentBB);

    currentBB = new BasicBlock();
  }

  @Override
  protected void visit(StringLiteral node) {
    returnValue = strtab.get(node.value);
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
