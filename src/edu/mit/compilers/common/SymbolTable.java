package edu.mit.compilers.common;

import java.util.*;

public class SymbolTable extends ScopedMap<String, Var> {

  private int offsetCounter;

  public SymbolTable() {
    super();
  }

  public SymbolTable(SymbolTable parentScope) {
    super(parentScope);
    this.offsetCounter = parentScope.offsetCounter;
  }

  @Override
  public SymbolTable scope() {
    return new SymbolTable(this);
  }

  @Override
  public SymbolTable unscope() {
    return (SymbolTable) parentScope;
  }

  public void SwapIn(SymbolTable other) {
    SymbolTable that = other;
    HashMap<String, Var> tmp = map;
    int tmp2 = offsetCounter;

    offsetCounter = that.offsetCounter;
    map = that.map;
    parentScope = that.parentScope;

    that.map = tmp;
    that.offsetCounter = tmp2;
  }

  public int getOffset() {
    return offsetCounter;
  }

  public void setOffset(int value) {
    offsetCounter = value;
  }

  public void forcedInsert(Var var) {
    map.put(var.id, var);
  }

  public boolean insert(Var var) {
    if (map.containsKey(var.id) || !var.isVariable()) {
      return false;
    } else {
      map.put(var.id, var);
      switch (var.type) {
      case INT:
        offsetCounter = Util.roundUp(offsetCounter, 8);
        break;
      case BOOLEANARRAY:
      case INTARRAY:
        offsetCounter = Util.roundUp(offsetCounter, 16);
      default:
        break;
      }
      var.stackOffset = -offsetCounter;
      offsetCounter += var.size;
      return true;
    }
  }

  public ArrayList<Var> asList() {
    ArrayList<Var> list = new ArrayList<Var>(map.values());
    list.sort(new Comparator<Var>() {
      @Override
      public int compare(Var arg0, Var arg1) {
        int i = Integer.compare(arg1.stackOffset, arg0.stackOffset);
        return i;
      }
    });
    return list;
  }
}
