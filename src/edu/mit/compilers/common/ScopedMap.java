package edu.mit.compilers.common;

import java.util.*;

public class ScopedMap<K, V> {

  protected HashMap<K, V> map;
  protected ScopedMap<K, V> parentScope;

  public ScopedMap() {
    map = new HashMap<K, V>();
    parentScope = null;
  }

  public ScopedMap(ScopedMap<K, V> parentscope) {
    map = new HashMap<K, V>();
    this.parentScope = parentscope;
  }

  public ScopedMap<K, V> scope() {
    return new ScopedMap<K, V>(this);
  }

  public ScopedMap<K, V> unscope() {
    return parentScope;
  }

  public V lookup(K key) {
    V result = map.get(key);
    if (result == null && parentScope != null) {
      return parentScope.lookup(key);
    }
    return result;
  }

  public V lookupCurrentScope(K key) {
    return map.get(key);
  }

  public V insert(K key, V val) {
    V result = map.get(key);
    map.put(key, val);
    return result;
  }

  public Set<K> keySet() {
    return map.keySet();
  }
}
