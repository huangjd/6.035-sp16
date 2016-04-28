package edu.mit.compilers.common;

import java.util.Iterator;

public class Util {
  public static boolean implies(boolean condition, boolean consequence) {
    return (!condition || consequence);
  }

  public static int roundUp(int value, int alignment) {
    return (value + alignment - 1) / alignment * alignment;
  }

  public static int roundDown(int value, int alignment) {
    int remainder = value % alignment;
    if (remainder < 0) {
      remainder += alignment;
    }
    return value - remainder;
  }

  public static long roundUp(long value, long alignment) {
    return (value + alignment - 1) / alignment * alignment;
  }

  public static int extractNBit(int value, int startBit, int n) {
    return value >>> startBit & ((1 << n) - 1);
  }

  public static boolean isImm32(long value) {
    return value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE;
  }

  public static long parseInt(String value, Long lowerBound, Long upperBound) throws IllegalArgumentException {
    long v;
    if (value.length() > 2 && value.charAt(0) == '0' && value.charAt(1) == 'x') {
      v = Long.parseLong(value.substring(2), 16);
    } else {
      v = Long.parseLong(value);
    }
    if (lowerBound != null) {
      if (v < lowerBound) {
        throw new BoundsException(value, lowerBound, upperBound, null);
      }
    }
    if (upperBound != null) {
      if (v >= upperBound) {
        throw new BoundsException(value, lowerBound, upperBound, null);
      }
    }
    return v;
  }

  public static long strToBytes(String s) {
    long val = 0;
    for (int i = 0; i < Math.min(s.length(), 8); i++) {
      val += ((long) s.charAt(i)) << i * 8l;
    }
    val &= 0xfffffffffffffffl;
    return val;
  }

  public interface Reductor<S, T> {
    public S apply(S x, T y);

    public S apply(T y);
  }

  public static <S, T extends Object> T Reduce(Reductor<T, S> reductor, Iterable<S> values, T defaultValue) {
    Iterator<S> it = values.iterator();

    if (!it.hasNext()) {
      return defaultValue;
    }

    S first = it.next();
    T result = reductor.apply(first);
    while (it.hasNext()) {
      result = reductor.apply(result, it.next());
    }
    return result;
  }

  public static <S, T extends Object> T Reduce(Reductor<T, S> reductor, Iterable<S> values) {
    return Reduce(reductor, values, null);
  }

  public static <S> String toCommaSeparatedString(Iterable<S> values) {
    return Reduce(new Reductor<StringBuilder, S>() {
      @Override
      public StringBuilder apply(StringBuilder x, S y) {
        return x.append(", ").append(y.toString());
      }

      @Override
      public StringBuilder apply(S y) {
        return new StringBuilder(y.toString());
      }
    }, values, new StringBuilder("")).toString();
  }

}
