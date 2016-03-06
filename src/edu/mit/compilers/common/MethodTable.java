package edu.mit.compilers.common;

import java.util.HashMap;

import edu.mit.compilers.nodes.Function;

public class MethodTable {

  private HashMap<String, Function> map;

  public MethodTable() {
    map = new HashMap<>();
  }

  public Function lookup(String id) {
    return map.get(id);
  }

  public boolean insert(Function f) {
    if (map.containsKey(f.id)) {
      return false;
    } else {
      map.put(f.id, f);
      return true;
    }
  }

  public void forceInsert(Function f) {
    map.put(f.id, f);
  }
}
