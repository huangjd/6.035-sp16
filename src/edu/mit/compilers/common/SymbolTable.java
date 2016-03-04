package edu.mit.compilers.common;

import java.util.*;

public class SymbolTable implements ScopedMap {

  private int offsetCounter;
  private HashMap<String, Var> map;
  private SymbolTable parentScope;

  public SymbolTable() {
    map = new HashMap<>();
    parentScope = null;
  }

  public SymbolTable(SymbolTable parentScope) {
    map = new HashMap<>();
    this.parentScope = parentScope;
    this.offsetCounter = parentScope.offsetCounter;
  }

  @Override
  public SymbolTable scope() {
    return new SymbolTable(this);
  }

  @Override
  public SymbolTable unscope() {
    return parentScope;
  }

  @Override
  public void Swap(ScopedMap other) {
    SymbolTable that = (SymbolTable) other;
    offsetCounter = that.offsetCounter;
    map = that.map;
  }

  public int getOffset() {
    return offsetCounter;
  }

  public void setOffset(int value) {
    offsetCounter = value;
  }

  public Var lookup(String id) {
    Var result = map.get(id);
    if (result == null && parentScope != null) {
      return parentScope.lookup(id);
    }
    return result;
  }

  public Var lookupCurrentScope(String id) {
    return map.get(id);
  }

  public boolean insert(Var var) {
    if (map.containsKey(var.id) || !var.isVariable()) {
      return false;
    } else {
      map.put(var.id, var);
      switch (var.type) {
      case INT:
        offsetCounter = MathUtil.roundUp(offsetCounter, 8);
        break;
      case BOOLEANARRAY:
      case INTARRAY:
        offsetCounter = MathUtil.roundUp(offsetCounter, 16);
      default:
        break;
      }
      var.stackOffset = offsetCounter;
      offsetCounter += var.size;
      return true;
    }
  }

  public ArrayList<Var> asList() {
    ArrayList<Var> list = new ArrayList<Var>(map.values());
    list.sort(new Comparator<Var>() {
      @Override
      public int compare(Var arg0, Var arg1) {
        return Integer.compare(arg0.stackOffset, arg1.stackOffset);
      }
    });
    return list;
  }
}
