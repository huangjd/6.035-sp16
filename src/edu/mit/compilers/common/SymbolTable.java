package edu.mit.compilers.common;

import java.util.HashMap;

public class SymbolTable implements ScopedMap {
  public class SymbolInfo {
    Type type;
    int stackOffset;
    int length;

    public boolean isArray() {
      return type == Type.BOOLEANARRAY || type == Type.INTARRAY;
    }

    public boolean isVariable() {
      return type == Type.BOOLEAN || type == Type.INT || isArray();
    }
  }

  private HashMap<String, SymbolInfo> map;

  private SymbolTable parentScope;

  public SymbolTable() {
    map = new HashMap<>();
    parentScope = null;
  }

  public SymbolTable(SymbolTable parentScope) {
    map = new HashMap<>();
    this.parentScope = parentScope;
  }

  @Override
  public SymbolTable scope() {
    return new SymbolTable(this);
  }

  @Override
  public SymbolTable unscope() {
    return parentScope;
  }

  public SymbolInfo lookup(String id) {
    return map.get(id);
  }

  public boolean insert(String id, SymbolInfo symbolInfo) {
    if (map.containsKey(id) || !symbolInfo.isArray()) {
      return false;
    } else {
      map.put(id, symbolInfo);
      return true;
    }
  }
}
