package edu.mit.compilers.visitors;

import edu.mit.compilers.codegen.CallingConvention;
import edu.mit.compilers.nodes.*;

public class CallExprStackReserveEstimator extends Visitor {
  int reserveCount;

  final CallingConvention cc;

  public CallExprStackReserveEstimator(CallingConvention callingConvention) {
    cc = callingConvention;
  }

  @Override
  protected void visit(Function node) {
    reserveCount = 0;
    super.visit(node);
    node.stackFrameReserve = reserveCount;
  }

  @Override
  protected void visit(Call node) {
    reserveCount = Math.max(reserveCount, node.args.size() - cc.getNumArgsPassedByReg());
  }
}

