package edu.mit.compilers.nodes;

import edu.mit.compilers.common.*;

public class Length extends Expression {
  private Var array;
  public Length (Var array, SourcePosition pos) {
    super(pos);
    hashCache = array.hashCode() + 0b10001011101010101;
    
    if (!array.isArray()) {
    	ErrorLogger.logError(ErrorLogger.ErrorMask.SEMANTICS, pos, this.toString(), ErrorType.TYPEERROR);
    	throw new TypeException(array,false);
    }
  } 

  @Override
  public void dispatch(Visitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return "@" + array.toString();
  }

  @Override
  public Type getType() {
    return Type.INT;
  }
}
