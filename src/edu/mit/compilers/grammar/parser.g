header {
package edu.mit.compilers.grammar;
import edu.mit.compilers.common.*;
import edu.mit.compilers.nodes.*;
import sun.util.locale.LanguageTag;

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

  // Selectively turns on debug mode.

  /** Whether to display debug information. */
  private boolean trace = false;

  public void setTrace(boolean shouldTrace) {
    trace = shouldTrace;
  }
  @Override
  public void traceIn(String rname) throws TokenStreamException {
    if (trace) {
      super.traceIn(rname);
    }
  }
  @Override
  public void traceOut(String rname) throws TokenStreamException {
    if (trace) {
      super.traceOut(rname);
    }
  }
  
  SourcePosition currentTokenPos = new SourcePosition(getFileName());
  @Override
  public void match(int arg0) throws MismatchedTokenException, TokenStreamException  {
  	currentTokenPos = new SourcePosition(getFilename(), LT(1).getLine(), LT(1).getColumn());
  	super(match);
  }
  public SourcePosition getSourcePosition() {
    return currentTokenPos;
  }
  
  
  SymbolTable currentSymtab;
  MethodTable methodTable;
  Program program;
  ArrayList<Expression> exprStack = new ArrayList<>();
  
  public Program getProgram() {
  	return program;
  }
}

program: {
		currentSymtab = new SymbolTable();
		methodTable = new MethodTable();
		program = new Program(methodTable, currentSymtab);
	} (callout_decl)* (field_decl)* (method_decl)* EOF!;

callout_decl: TK_callout! ID SEMICOLON!;

field_decl: type (var_decl | array_decl) (COMMA (var_decl | array_decl))* SEMICOLON;

var_decl: ID;

array_decl: ID LSQUARE INTLITERAL RSQUARE;

method_decl: (type|TK_void) ID LPAREN (parameter_list)? RPAREN block;

parameter_list : type ID (COMMA type ID)*; 

block: LCURLY (field_decl)* (statement)* RCURLY;

statement: assign_stmt
         | call SEMICOLON
         | if_stmt
         | for_stmt
         | while_stmt
         | return_stmt
         | TK_break SEMICOLON
         | TK_continue SEMICOLON;

assign_stmt: location assign_op expr SEMICOLON;

assign_op: EQUAL | PLUS_EQ | MINUS_EQ;

call: ID LPAREN (argument_list)? RPAREN;

argument_list: argument (COMMA argument)*;

argument: expr | STRINGLITERAL;

if_stmt: TK_if LPAREN expr RPAREN block (TK_else block)?;

for_stmt: TK_for LPAREN ID EQUAL expr COMMA expr (COMMA INTLITERAL)? RPAREN block;

while_stmt: TK_while LPAREN expr RPAREN block;

return_stmt: TK_return (expr)? SEMICOLON; 

expr: ternary_expr;

ternary_expr returns [ExpressionNode e = null] :  or_expr (QUESTION ternary_expr COLON ternary_expr)?;

or_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expr;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
}: expr = and_expr {
	exprStack.push(expr);
} ({String or;} or = or_op {
	opStack.push(new OpDesc(or, getSourcePosition()));
} expr = and_expr {
	exprStack.push(expr);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();
			switch (op.op) {
			case "||":
				exprStack.addFirst(new Eq(left, right, getSourcePosition()));
				break;
			default:
				throw new RuntimeException("blah");
			}
		}
		e = expr;
	} catch (TypeCheckException e2) {
		// TODO: handle exception
	}
};

and_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expr;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
	
} : expr = eq_expr {
	exprStack.push(expr);
} ({String and;} and = and_op {
	opStack.push(new OpDesc(and, getSourcePosition()));
} expr = eq_expr {
	exprStack.push(expr);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();
			switch (op.op) {
			case "&&":
				exprStack.addFirst(new Eq(left, right, getSourcePosition()));
				break;
			default:
				throw new RuntimeException("blah");
			}
		}
		e = expr;
	} catch (TypeCheckException e2) {
		// TODO: handle exception
	}
};

eq_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expr;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
	
} : expr = rel_expr {
	exprStack.push(expr);
} ({String eq;} eq = eq_op {
	opStack.push(new OpDesc(eq, getSourcePosition()));
} expr = rel_expr {
	exprStack.push(expr);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();
			switch (op.op) {
			case '==':
				exprStack.addFirst(new Eq(left, right, getSourcePosition()));
				break;
			case '!=':
				exprStack.addFirst(new Ne(left, right, getSourcePosition()));
				break;
			default:
				throw new RuntimeException("blah");
			}
		}
		e = expr;
	} catch (TypeCheckException e2) {
		// TODO: handle exception
	}
};

rel_expr returns [ExpressionNode e = null] {
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(String op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expr;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
		
} : expr = add_expr {
	exprStack.push(expr);
} ({String rel;} rel = rel_op {
	opStack.push(new OpDesc(rel, getSourcePosition()));
} expr = add_expr {
	exprStack.push(expr);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();
			switch (op.op) {
			case ">":
				exprStack.addFirst(new Gt(left, right, getSourcePosition()));
				break;
			case "<":
				exprStack.addFirst(new Lt(left, right, getSourcePosition()));
				break;
			case ">=":
				exprStack.addFirst(new Ge(left, right, getSourcePosition()));
				break;
			case "<=":
				exprStack.addFirst(new Le(left, right, getSourcePosition()));
				break;
			default:
				throw new RuntimeException("blah");
			}
		}
		e = expr;
	} catch (TypeCheckException e2) {
		// TODO: handle exception
	}
	
};

add_expr returns [ExpressionNode e = null] { //   (add_op mul_expr)*;
	class OpDesc { // Operator descriptor
		char op;
		SourcePosition pos;
		OpDesc(char op, SourcePosition position) {
			this.op = op;
			this.pos = position;
		}
	};
	
	ExpressionNode expr;
	ArrayList<OpDesc> opStack = new ArrayList<>();
	LinkedList<ExpressionNode> exprStack = new LinkedList<>();
}: expr = mul_expr {
	exprStack.push(expr);
} ({char add;} add = add_op {
	opStack.push(new OpDesc(add, getSourcePosition()));
} expr = mul_expr {
	exprStack.push(expr);
})* {
	try {
		for (OpDesc op : opStack) {
			ExpressionNode left = exprStack.pollFirst();
			ExpressionNode right = exprStack.pollFirst();
			switch (op.op) {
			case '+':
				exprStack.addFirst(new Add(left, right, getSourcePosition()));
				break;
			case '-':
				exprStack.addFirst(new Sub(left, right, getSourcePosition()));
				break;
			default:
				throw new RuntimeException("blah");
		
			}
		}
		e = expr;
	} catch (TypeCheckException e2) {
		// TODO: handle exception
	}
};


mul_expr: unary_expr (MUL_OP unary_expr)*;

unary_expr returns [ExpressionNode e = null] {
	ExpressionNode expr;
	class OpDesc {
		char op;
		SourcePosition pos;
		OpDesc(char op, int line, int col) {
			this.op = op;
			this.pos = new SourcePosition(line, col);
		}
	};
	ArrayList<OpDesc> opStack = new ArrayList<>();
}: ({char op;} op = unary_op { 
		opStack.push(new OpDesc(op, ));
	})*
	expr = primary_expr {
		if (expr == null) {
			// TODO: error 
		} else {
		  try {
		  	Collections.reverse(opStack);
				for (Character c : opStack) {
					if (c == '!') {
						expr = new Not(expr.box(), getSourcePosition());
					} else if (c == '-') {
						expr = new Minus(expr.box(), getSourcePosition());
					} else {
						throw new RuntimeException("WTF"); 
					}
				}
			} catch (TypeException ex) {
				// TODO: error
			}
		  e = expr;
		}
		
	}
	;

primary_expr: location
            | call
            | CHARLITERAL
            | INTLITERAL
            | boolean_literal
            | length
            | LPAREN expr RPAREN;

length: AT_SIGN ID;

unary_op returns [char c = 0]: MINUS { c = '-';} | EXCLAMATION { c = '!';};

add_op returns [char add = 0]: PLUS { add = '+';}| MINUS {add = '-';};

rel_op returns [String rel]: "<" { rel = "<";} | ">" { rel = ">";} | "<=" { rel = "<=";} | ">=" {rel = ">=";};

eq_op returns [String eq]: "==" { eq = "==";} | "!=" { eq = "!="; };

and_op returns [String and]: AND {and = "&&";};

or_op returns [String or]: OR {or = "||";};

type: TK_int | TK_boolean;

location: ID | ID LSQUARE expr RSQUARE;

boolean_literal : TK_true | TK_false;
