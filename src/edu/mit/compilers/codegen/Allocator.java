package edu.mit.compilers.codegen;

import java.util.ArrayList;

public interface Allocator {

  /**
   *
   * @param basicblocks
   *          both input and output.
   *
   * @return int: amount of stack space used in this function.
   */
  public int transform(ArrayList<BasicBlock> basicblocks);
}
