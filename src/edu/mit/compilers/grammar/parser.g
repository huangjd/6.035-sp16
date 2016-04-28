header {
package edu.mit.compilers.grammar;
import edu.mit.compilers.common.*;
import edu.mit.compilers.visitors.*;
import edu.mit.compilers.nodes.*;
import edu.mit.compilers.codegen.*;

import java.util.*;
import java.lang.*;
}

options
{
  mangleLiteralPrefix = "TK_";
  language = "Java";
}

class DecafParser extends Parser;
options
{
  importVocab = DecafScanner;
  k = 3;
  buildAST = true; 
}

// Java glue code that makes error reporting easier.
// You can insert arbitrary Java code into your parser/lexer this way.
{
  // Do our own reporting of errors so the parser can return a non-zero status
  // if any errors are detected.
  /** Reports if any errors were reported during parse. */
  private boolean error;

  @Override
  public void reportError (RecognitionException ex) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
    System.err.println(ex);
  } 
  @Override
  public void reportError (String s) {
    // Print the error via some kind of error reporting mechanism.
    error = true;
    System.err.println(s);
  }
  public boolean getError () {
    return error; 
  }
  
  MethodTable methodTable;
  Program program;
  SymbolTable currentSymtab;
  FunctionNode currentFunc = null;

  // Selectively turns on debug mode.

  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws TokenStreamException {
    if (rname.equals("block")) {
      currentSymtab = currentSymtab.scope(); 
    }
    if (rname.equals("method_decl")) {
      currentSymtab = currentSymtab.scope();
    }
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws TokenStreamException {
    if (rname.equals("block")) {
      currentSymtab = currentSymtab.unscope(); 
    }
    if (rname.equals("method_decl")) {
      currentSymtab = currentSymtab.unscope();
    }
    if (trace) {
      super.traceOut(rname);
    }
  }
    
  public SourcePosition getPos() throws TokenStreamException {
    SourcePosition usp = new SourcePosition(getFilename(), LT(1).getLine(), LT(1).getColumn() + LT(1).getText().length());
    return usp;
  }
}

program returns [ProgramNode p = null]: {
	currentSymtab = new SymbolTable();
	methodTable = new MethodTable();
	program = new Program(methodTable, currentSymtab);
	FunctionNode f;
	boolean ok = true;
	ArrayList<StatementNode> declarations;
	SourcePosition pos;
} (f = callout_decl {
  if (f != null) { 
    program.functions.add(f);
  } else {
    ok = false;
  }
})* ({pos = getPos();} declarations = field_decl {
  if (declarations != null) {
    try {
      for (StatementNode s : declarations) {
        Function f1 = methodTable.lookup(((VarDecl)s.getNode()).var.id);  
        if (f1 != null) {
          ErrorLogger.logError(new RedeclaredSymbolException(f1, ((VarDecl)s.getNode()).var, pos));
          ok = false;
        }
      }
      program.varDecls.addAll(declarations);
    } catch (Exception e) {
      ok = false;
    }
  } else {
    ok = false;
  }
})* ({pos = getPos();} f = method_decl {
  if (f != null) {
    Function func = (Function) f.getNode();
    if (currentSymtab.lookup(func.id) != null) {
      ErrorLogger.logError(new RedeclaredSymbolException((Function)f.getNode(), currentSymtab.lookup(func.id), pos));
      ok = false;
    }
    program.functions.add(f);
  } else {
    ok = false; 
  }
})* EOF {
  if (ok) {
    p = program.box(); 
  }
  if (p != null && !error) {
    SemanticsPostProcess a = new SemanticsPostProcess();
    p = (ProgramNode) a.enter(p);
    if (!a.ok) {
      p = null;
    }
  }
};

callout_decl returns [FunctionNode f = null] {
  String name;
  SourcePosition pos;
} : TK_callout {pos = getPos();} 
    name = id SEMICOLON {
  Function oldf = methodTable.lookup(name);
  if (oldf == null) {
    oldf = new Function(name, pos);
    methodTable.insert(oldf);
    f = oldf.box();
  } else {
    ErrorLogger.logError(new RedeclaredSymbolException(oldf, pos));
    f = null;
  }
};

method_decl returns [FunctionNode f = null] {
  Type returnType;
  String name;
  StatementNode s;
  
  ArrayList<StatementNode> body = new ArrayList<>(), temps;
  SourcePosition pos = getPos(), pos2 = null;
  int ok = 0;
} : (returnType = type | TK_void {returnType = Type.NONE;}) 
    name = id  
    LPAREN (ok = parameter_list)? RPAREN {
  Function oldf = methodTable.lookup(name);
  if (oldf != null) {
    ErrorLogger.logError(new RedeclaredSymbolException(oldf, pos));
  } else {
    oldf = new Function(name, returnType, ok, currentSymtab, new Pass(null).box(), pos);
    methodTable.insert(oldf);
    f = oldf.box();
    currentFunc = f;
  }
  currentSymtab.setOffset(8);
} LCURLY
  (temps = field_decl {
    if (temps != null) {
      try {
        body.addAll(temps);
      } catch (Exception ex) {
        ok = -1;
      }
    } else {
      ok = -1;
    }
  })*            
  (s = statement {
    if (s != null) {
      body.add(s);
    } else {
      ok = -1;
    }
  })*
  {pos2 = getPos();} RCURLY {
  
  try {
    if (returnType == Type.NONE) {
      body.add(new Return(f, pos2).box());
    } else {
      body.add(new Die(Die.CONTROL_REACHES_END_OF_NON_VOID_FUNCTION, pos2).box());
    }
    Block b = new Block(currentSymtab, body, pos);    
    
    oldf = new Function(name, returnType, ok, currentSymtab, b.box(), pos);
    f.setNode(oldf);
    methodTable.forceInsert(oldf);
    currentFunc = null;
    if (ok < 0) {
      f = null;
    }
  } catch (NullPointerException ex) {
    f = null;
  }
};

parameter_list returns [int ok = 0] {
  Type t;
  String name;
  SourcePosition pos = getPos(), pos2;
  int count = 0;
  int offset = 16;
} : t = type {pos2 = getPos();} name = id {
   if (t != null) {
     Var newVar = new Var(name, t); 
     Var oldVar = currentSymtab.lookupCurrentScope(name);
     if (oldVar == null) {
       currentSymtab.insert(newVar);
       newVar.stackOffset = 1;
       count = 1;
     } else {
       ErrorLogger.logError(new RedeclaredSymbolException(oldVar, newVar, pos2));
       ok = -1;
     }
   } else {
     ErrorLogger.logError(new TypeException(t, false, pos));
     ok = -1;
   }
} (COMMA {pos = getPos();} t = type {pos2 = getPos();} name = id {
  if (t != null) {
    Var newVar = new Var(name, t); 
    Var oldVar = currentSymtab.lookupCurrentScope(name);
    if (oldVar == null) {
      currentSymtab.insert(newVar);
      newVar.stackOffset = 0;
      switch (count) {
      case 1:
      case 2:
      case 3:
      case 4:
      case 5:
        newVar.stackOffset = count + 1;
        break;
      default:
        newVar.stackOffset = (count - 4) * 8;
        break; 
      }
      count++;
    } else {
      ErrorLogger.logError(new RedeclaredSymbolException(oldVar, newVar, pos2));
      ok = -1;
    }
  } else {
    ErrorLogger.logError(new TypeException(t, false, pos));
    ok = -1;
  }
})* {
  if (ok != -1) {
    ok = count;
  }
};

	
field_decl returns [ArrayList<StatementNode> declarations = null] {
   Type t = Type.NONE;
   ArrayList<StatementNode> decls = new ArrayList<>();
   ArrayList<SourcePosition> pos = new ArrayList<>();
   StatementNode stmt;
} : t = type  {pos.add(getPos());} 
    stmt = var_decl[t] {decls.add(stmt);} 
   (COMMA {pos.add(getPos());} stmt = var_decl[t] {decls.add(stmt);})* SEMICOLON {
   declarations = decls;
   for (StatementNode s : decls) {
     if (s == null) {
       declarations = null;
     }
   }  
}; 

var_decl [Type type] returns [StatementNode node = null] {
  String identifier;
  SourcePosition pos = getPos(), pos2 = null;
  String intliteral = null;
  boolean bad = false;
} : identifier = id (LSQUARE { 
  if (LA(2) != DecafScannerTokenTypes.RSQUARE) {
    ErrorLogger.logError(new ArrayDeclSizeException(identifier, pos));
    bad = true;
  }
  intliteral = LT(1).getText(); 
  pos2 = getPos();
} INTLITERAL RSQUARE)? {
  try {
    if (intliteral == null) {
      Var newVar = new Var(identifier, type);
      Var oldVar = currentSymtab.lookupCurrentScope(identifier);
      if (oldVar != null) {
        ErrorLogger.logError(new RedeclaredSymbolException(oldVar, newVar, pos));
        bad = true;
      } else {
        node = new VarDecl(currentSymtab, newVar, pos).box();
      }
    } else {
      long length = Util.parseInt(intliteral, new Long(1), null);
      Var newVar = new Var(identifier, type.getArrayType(), length);
      Var oldVar = currentSymtab.lookupCurrentScope(identifier);
      if (oldVar != null) {
        ErrorLogger.logError(new RedeclaredSymbolException(oldVar, newVar, pos));
        bad = true;
      } else {
        node = new VarDecl(currentSymtab, newVar, pos).box();
      }
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (RedeclaredSymbolException ex) {
    ErrorLogger.logError(ex);
  } catch (BoundsException ex) {
    ErrorLogger.logError(new BoundsException(ex, pos2));
  }
  if (bad) {
    node = null;
  }
};

block returns [StatementNode s = null] {
  SourcePosition pos = getPos();
  ArrayList<StatementNode> statements = new ArrayList<>();
  ArrayList<StatementNode> decls;
  StatementNode stmt = null;
  boolean ok = true;
} : LCURLY (decls = field_decl {
  try {
    statements.addAll(decls); 
  } catch (Exception ex) {
    ok = false;
  }
})*
    (stmt = statement {statements.add(stmt);} )* RCURLY {  
  s = new Block(currentSymtab, statements, pos).box();
  for (StatementNode node : statements) {
    if (node == null) {
      s = null;
    }
  }
  if (!ok) {
    s = null;
  }
};

statement returns [StatementNode s = null] {
  SourcePosition pos = getPos();
}
  : s = assign_stmt
  | s = store_stmt
  | {Call e;} e = call SEMICOLON {
    if (e != null) {
      s = new CallStmt(e, e.getSourcePosition()).box();
    }
  } 
  | s = if_stmt
  | s = for_stmt
  | s = while_stmt
  | s = return_stmt
  | TK_break SEMICOLON {
      s = new Break(pos).box();
  }
  | TK_continue SEMICOLON {
      s = new Continue(pos).box();
  };

assign_stmt returns [StatementNode s = null] {
  SourcePosition pos;
  Var var;
  ExpressionNode value;
  char op = 0;
} : var = location 
    {pos = getPos();} op=assign_op 
    value = expr SEMICOLON {
  try {
    switch (op) {
    case '=':
      s = new Assign(var, value, pos).box();
      break;
    case '+':
      s = new Assign(var, new Add(new VarExpr(var, pos).box(), value, pos).box(), pos).box();
      break;
    case '-':
      s = new Assign(var, new Sub(new VarExpr(var, pos).box(), value, pos).box(), pos).box();
      break;
    default:
      throw new NullPointerException();
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};
	
store_stmt returns [StatementNode s = null] {
  SourcePosition pos;
  AbstractMap.SimpleEntry<Var, ExpressionNode> kvp;
  char op = 0;
  ExpressionNode value;
} : kvp = arraylocation 
    {pos = getPos();} op = assign_op 
    value = expr SEMICOLON {
  try {
    Var var = kvp.getKey();
    ExpressionNode index = kvp.getValue();
    switch (op) {
    case '=':
      s = new Store(var, index, value, pos, Store.CoOperand.NONE).box();
      break;
    case '+':
      s = new Store(var, index, value, pos, Store.CoOperand.PLUS).box();
      break;
    case '-':
      s = new Store(var, index, value, pos, Store.CoOperand.MINUS).box();
      break;
    default:
      throw new NullPointerException();  
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};
	
assign_op returns [char c = 0]: EQUAL { c = '=';} | PLUS_EQ { c = '+';} | MINUS_EQ { c = '-';};

call returns [Call e = null] {
  SourcePosition pos = getPos(); 
  String name;
  ArrayList<ExpressionNode> args = new ArrayList<>();
  ExpressionNode temp;
} : name = id 
    LPAREN (temp = argument {args.add(temp);} 
   (COMMA temp = argument {args.add(temp);})*)? RPAREN {
  Var v = currentSymtab.lookupCurrentScope(name);
  if (v != null && v.type != Type.CALL && v.type != Type.CALLOUT) {
    ErrorLogger.logError(new TypeException(v, Type.CALL, pos));
    e = null;
  } else {
    Function f = methodTable.lookup(name);
    try {
      if (f == null) {
        ErrorLogger.logError(new UndeclaredSymbolException(name, methodTable, pos));
        e = null;
      } else {
        e = new Call(f, args, pos);
        for (ExpressionNode arg: args) {
          if (arg == null) {
            e = null;
          }
        } 
      }
    } catch (NullPointerException ex) {
    }catch (TypeException ex) {
      ErrorLogger.logError(ex);
    } catch (ArgumentsException ex) {
      ErrorLogger.logError(ex);
    } catch (Exception ex) { 
      ErrorLogger.logError(ex);
    }
  }
};

argument returns [ExpressionNode e = null] 
  : e = expr
  | { e = new StringLiteral(LT(1).getText(), getPos()).box(); } STRINGLITERAL;

if_stmt returns [StatementNode s = null] {
  SourcePosition pos = getPos();
  ExpressionNode cond;
  StatementNode body, elsebody = null;
} : TK_if LPAREN cond = expr 
    RPAREN body = block 
    (TK_else elsebody = block)? {
  try {
    if (elsebody == null) {
      s = new If(cond, body, pos).box();
    } else {
      s = new If(cond, body, elsebody, pos).box();
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};

for_stmt returns [StatementNode s = null] {
  SourcePosition pos = getPos(), pos1, pos2, pos3 = null;
  String identifier;
  ExpressionNode init, end;
  String intToken = null;
  StatementNode body;
  Var var = null;
} : TK_for LPAREN identifier = id 
    EQUAL {pos1 = getPos();} init = expr 
    COMMA {pos2 = getPos();} end = expr 
    (COMMA {pos3 = getPos(); intToken = LT(1).getText();} INTLITERAL)? 
    RPAREN body = block {
  try {
    var = currentSymtab.lookup(identifier);
    if (var == null) {
      ErrorLogger.logError(new UndeclaredSymbolException(identifier, currentSymtab, pos));
    }
    
    if (intToken == null) {
      s = new For(var, init, end, body, pos).box();
    } else {
      long increment = Util.parseInt(intToken, new Long(1), null);
      s = new For(var, init, end, increment, body, pos).box();
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  } catch (BoundsException ex) {
    ErrorLogger.logError(new BoundsException(ex, pos3));
  } 
};

while_stmt returns [StatementNode s = null] {
  SourcePosition pos = getPos(), pos2;
  ExpressionNode cond;
  StatementNode body;
} : TK_while LPAREN {pos2 = getPos();} cond = expr RPAREN body = block {
  try {
    s = new While(cond, body, pos).box();
  } catch (TypeException ex) {
    ErrorLogger.logError(new TypeException(ex, pos2));
  }  catch (NullPointerException ex) {
    return null;
  }
};


return_stmt returns [StatementNode s = null] { 
  ExpressionNode e = new IntLiteral(0, null).box(); // dummy node to test null
  ExpressionNode temp = e; 
  SourcePosition pos = getPos();
} : TK_return (e = expr)? SEMICOLON {
  try {
    if (currentFunc == null) {
      ErrorLogger.logError(new GeneralException("return statement outside of a function body", pos));
    }
    if (e == temp) {
      s = new Return(currentFunc, pos).box();
    } else if (e != null) {
      s = new Return(currentFunc, e, pos).box();
    }
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
    s = null;
  }
}; 

expr returns [ExpressionNode e = null]: e = ternary_expr;

ternary_expr returns [ExpressionNode e = null] {
  ExpressionNode t, f;
  SourcePosition pos;
}  : e = or_expr  
   ( {pos = getPos();} QUESTION t = ternary_expr 
     COLON f = ternary_expr {
     try {
       e = new Ternary(e, t, f, pos).box();
     } catch (TypeException ex) {
       ErrorLogger.logError(ex);
     } catch (NullPointerException ex) {
       return null;
     }
   })? ;

or_expr returns [ExpressionNode e = null] {
  ArrayList<ExpressionNode> exprStack = new ArrayList<>();
  ArrayList<SourcePosition> posStack = new ArrayList<>();
  ExpressionNode expression, start;
  SourcePosition pos;
} : start = and_expr 
    ( {posStack.add(getPos());} OR expression = and_expr { exprStack.add(expression);} )* {
  try {
    int i = 0;
    for (ExpressionNode expr : exprStack) {
      start = new Or(start, expr, posStack.get(i)).box();
      i++;
    }
    e = start;
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
    return null;
  }
}; 

and_expr returns [ExpressionNode e = null] {
  ArrayList<ExpressionNode> exprStack = new ArrayList<>();
  ArrayList<SourcePosition> posStack = new ArrayList<>();
  ExpressionNode expression, start;
  SourcePosition pos;
} : start = eq_expr 
    ( {posStack.add(getPos());} AND expression = eq_expr { exprStack.add(expression);} )* {
  try {
    int i = 0;
    for (ExpressionNode expr : exprStack) {
      start = new And(start, expr, posStack.get(i)).box();
      i++;
    }
    e = start;
  } catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
}; 

eq_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		String op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expression;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
  SourcePosition pos = getPos(); 
} : expression = rel_expr {
	exprStack.add(expression);
} ({ pos = getPos(); String eq;} eq = eq_op {
	opStack.add(new OpDesc(eq, pos));
} expression = rel_expr {
	exprStack.add(expression);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();

			if (op.op.equals("==")) {
				exprStack.addFirst(new Eq(left, right, op.pos).box());
			} else if (op.op.equals("!=")) {
				exprStack.addFirst(new Ne(left, right, op.pos).box());
			} else {
			  throw new NullPointerException();
			}
		}
    e = exprStack.pollFirst();
	} catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};

rel_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		String op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expression;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
  SourcePosition pos = getPos();
  String rel;
} : expression = add_expr {
	exprStack.add(expression);
} ( {pos = getPos();} rel = rel_op {
	opStack.add(new OpDesc(rel, pos));
} expression = add_expr {
	exprStack.add(expression);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();

			if (op.op.equals(">")) {
				exprStack.addFirst(new Gt(left, right, op.pos).box());
			} else if (op.op.equals("<")) {
				exprStack.addFirst(new Lt(left, right, op.pos).box());
			} else if (op.op.equals(">=")) {
				exprStack.addFirst(new Ge(left, right, op.pos).box());
			} else if (op.op.equals("<=")) {
				exprStack.addFirst(new Le(left, right, op.pos).box());
			} else {
			  throw new NullPointerException();
			}
		}
		e = exprStack.pollFirst();
	} catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};

add_expr returns [ExpressionNode e = null] { 
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(char op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expression;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
  SourcePosition pos = getPos();
  char add;
}: expression = mul_expr {
	exprStack.add(expression);
} ( {pos = getPos();} add = add_op {
	opStack.add(new OpDesc(add, pos));
} expression = mul_expr {
	exprStack.add(expression);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();

			switch (op.op) {
			case '+':
				exprStack.addFirst(new Add(left, right, op.pos).box());
				break;
			case '-':
				exprStack.addFirst(new Sub(left, right, op.pos).box());
				break;
			default:
			  throw new NullPointerException();
			}
		}
    e = exprStack.pollFirst();
	} catch (TypeException ex) {
    ErrorLogger.logError(ex);
  } catch (NullPointerException ex) {
  }
};

mul_expr returns [ExpressionNode e = null] {
  class OpDesc {
    char op;
    SourcePosition pos;
    OpDesc(char op, SourcePosition position) {
      this.op = op;
      this.pos = position;
    }
  };
  ExpressionNode expression;
  ArrayList<OpDesc> opStack = new ArrayList<>();
  LinkedList<ExpressionNode> exprStack = new LinkedList<>();
  SourcePosition pos = getPos();
  char c;
} : expression = unary_expr {
  exprStack.add(expression);
} ( {pos = getPos(); } c = mul_op { 
  opStack.add(new OpDesc(c, pos)); 
} expression = unary_expr { 
  exprStack.add(expression); 
})* {
  try {
    for (OpDesc op : opStack) {
      ExpressionNode left = exprStack.pollFirst();      
      ExpressionNode right = exprStack.pollFirst();
      
      switch (op.op) {
      case '*':
        exprStack.addFirst(new Mul(left, right, op.pos).box());
        break;
      case '/':
        exprStack.addFirst(new Div(left, right, op.pos).box());
        break;
      case '%':
        exprStack.addFirst(new Mod(left, right, op.pos).box());
        break;
      default:
        throw new NullPointerException();
      }
    }
    e = exprStack.pollFirst();
  } catch (TypeException ex) {
    ErrorLogger.logError(ex); 
  } catch (NullPointerException ex) {
  }
};
 
unary_expr returns [ExpressionNode e = null] {
	ExpressionNode expression;
	class OpDesc {
		char op;
		SourcePosition pos;
		OpDesc(char op, SourcePosition position) {
      this.op = op;
      this.pos = position;
    }
	};
	ArrayList<OpDesc> opStack = new ArrayList<>();
	SourcePosition pos; 
}: ({char op; pos = getPos(); } op = unary_op { 
		opStack.add(new OpDesc(op, pos));
	})*
  expression = primary_expr {
    if (expression != null) {
  	  try { 
  	  	Collections.reverse(opStack);
  			for (OpDesc desc : opStack) {
  			  char c = desc.op;
  			  pos = desc.pos;
  				if (c == '!') {
  				  expression = new Not(expression, pos).box();
  				} else if (c == '-') {
  				  expression = new Minus(expression, pos).box();
  				} else {
  					throw new IllegalStateException("Internal error in parser: unary_expr"); 
  				}
  			}
  			e = expression;
  		} catch (TypeException ex) {
  			ErrorLogger.logError(ex);
  		}
    }	
	}
	;

primary_expr returns [ExpressionNode e = null] {
  SourcePosition pos = getPos(); 
} : { Var var; } var = location {
    if (var != null) {
      e = new VarExpr(var, pos).box();
    }
  }
  | {HashMap.Entry<Var, ExpressionNode> kvp; } kvp = arraylocation {
    if (kvp.getKey() != null && kvp.getValue() != null) {
      e = new Load(kvp.getKey(), kvp.getValue(), pos).box();
    }
  }
  | {Expression e_tmp;} e_tmp = call { 
    if (e_tmp != null) { 
      e = e_tmp.box();
    }
  }
  | {Token charliteral = LT(1); } CHARLITERAL {
    char c0 = charliteral.getText().charAt(1);
    e = new IntLiteral(c0, pos).box();
  }
  | { Token intliteral = LT(1); } INTLITERAL {
    e = new UnparsedIntLiteral(intliteral.getText(), pos).box();
  }
  | e = boolean_literal
  | e = length 
  | LPAREN {pos = getPos();} e = expr RPAREN; 
exception catch [TypeException ex] {
  ErrorLogger.logError(ex);
}

length returns [ExpressionNode e = null] {
  String s;
  SourcePosition pos; 
}: AT_SIGN { pos = getPos(); } s = id {
  Var v = currentSymtab.lookup(s);
  if (v == null) {
    ErrorLogger.logError(new UndeclaredSymbolException(s, currentSymtab, pos));
  }
  try {
    e = new Length(v, pos).box();
  } catch (TypeException ex) { 
    ErrorLogger.logError(ex); 
  } catch (NullPointerException ex) {
  }
};

unary_op returns [char c = 0]: MINUS { c = '-';} | EXCLAMATION { c = '!';};

add_op returns [char add = 0]: PLUS { add = '+';}| MINUS {add = '-';};

mul_op returns [char c = 0]: { String s = LT(1).getText(); } MUL_OP { c = s.charAt(0);};

rel_op returns [String rel = null]: { rel = LT(1).getText();} REL_OP;

eq_op returns [String eq = null]: {eq = LT(1).getText();} EQ_OP;

and_op returns [String and = null]: AND {and = "&&";};

or_op returns [String or = null]: OR {or = "||";};

type returns [Type t = null]: TK_int {t = Type.INT;} | TK_boolean {t = Type.BOOLEAN;};

arraylocation returns [AbstractMap.SimpleEntry<Var, ExpressionNode> kvp = null] {
  Var var = null; 
  ExpressionNode e; 
} : var=location LSQUARE e=expr RSQUARE {
  kvp = new AbstractMap.SimpleEntry<Var, ExpressionNode>(var, e);
};

location returns [Var var = null] {
  String s;
  SourcePosition pos = getPos(); 
}: s=id {
  var = currentSymtab.lookup(s);
  if (var == null) {
    ErrorLogger.logError(new UndeclaredSymbolException(s, currentSymtab, pos));
  } 
};
 
id returns [String s = null]: {s = LT(1).getText();} ID;

boolean_literal returns [ExpressionNode e = null] {
  SourcePosition pos = getPos(); 
} : ( TK_true { e = new BooleanLiteral(true, pos).box(); } | 
      TK_false { e = new BooleanLiteral(false, pos).box(); }
  ) ;
