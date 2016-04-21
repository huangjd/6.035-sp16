package edu.mit.compilers.codegen;

public interface Transformable<T> {
  public T transform(T t);
}
