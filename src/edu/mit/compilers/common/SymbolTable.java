package edu.mit.compilers.common;

import java.util.*;

import edu.mit.compilers.codegen.Register;

public class SymbolTable {

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

  public SymbolTable scope() {
    return new SymbolTable(this);
  }

  public SymbolTable unscope() {
    return parentScope;
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
        if (i == 0) {
          i = Integer.compare(Register.regToArg(arg1.registerIndex),
              Register.regToArg(arg0.registerIndex));
        }
        return i;
      }
    });
    return list;
  }
}
