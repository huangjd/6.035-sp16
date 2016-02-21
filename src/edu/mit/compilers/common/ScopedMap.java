package edu.mit.compilers.common;

public interface ScopedMap {
  public ScopedMap scope();
  public ScopedMap unscope();
}
