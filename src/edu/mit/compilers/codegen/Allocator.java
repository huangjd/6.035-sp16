package edu.mit.compilers.codegen;

import java.util.ArrayList;

public interface Allocator {

  /**
   *
   * @param basicblocks
   *          both input and output.
   *
   * @return amount of stack space used in this function.
   */
  public long transform(ArrayList<BasicBlock> basicblocks, long offset);
}
