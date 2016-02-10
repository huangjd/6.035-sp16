header {
package edu.mit.compilers.grammar;
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
}

program: (callout_decl)* (field_decl)* (method_decl)* EOF;

callout_decl: TK_callout ID SEMICOLON;

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

assign_stmt: location (cop)? EQUAL expr SEMICOLON;
cop: PLUS | MINUS;

call: ID LPAREN (argument_list)? RPAREN;
argument_list: argument (COMMA argument)*;
argument: expr | STRINGLITERAL;

if_stmt: TK_if LPAREN expr RPAREN block (TK_else block)?;
for_stmt: TK_for LPAREN ID EQUAL expr COMMA expr (COMMA INTLITERAL)? RPAREN block;
while_stmt: TK_while LPAREN expr RPAREN block;
return_stmt: TK_return (expr)? SEMICOLON; 

expr: ternary_expr;
ternary_expr:  or_expr (QUESTION ternary_expr COLON ternary_expr)?;

or_expr: and_expr (OR and_expr)*;
and_expr: eq_expr (AND eq_expr)*;
eq_expr: rel_expr (EQ_OP rel_expr)*;
rel_expr: add_expr (REL_OP add_expr)*;
add_expr: mul_expr (add_op mul_expr)*;
mul_expr: unary_expr (MUL_OP unary_expr)*;
unary_expr: (unary_op)* primary_expr;

primary_expr: location
            | call
            | CHARLITERAL
            | INTLITERAL
            | boolean_literal
            | length
            | LPAREN expr RPAREN;

length: AT_SIGN ID;
unary_op: MINUS | EXCLAMATION;
add_op: PLUS | MINUS;

type: TK_int | TK_boolean;
location: ID | ID LSQUARE expr RSQUARE;
boolean_literal : TK_true | TK_false;
