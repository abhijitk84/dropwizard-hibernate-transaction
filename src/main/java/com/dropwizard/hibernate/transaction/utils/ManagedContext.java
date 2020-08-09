package com.dropwizard.hibernate.transaction.utils;

import java.util.HashMap;
import java.util.Map;

public class ManagedContext {

  private static final ThreadLocal<Map<String, String>> CONTEXT = new ThreadLocal();


  private static Map<String, String> contextMap(boolean createMap) {
    Map<String, String> contextMap = CONTEXT.get();
    if (contextMap == null && createMap) {
      contextMap = new HashMap<>();
      CONTEXT.set(contextMap);
    }
    return contextMap;
  }

  private static void doCleanup() {
    Map<String, String> contextMap = contextMap(false);
    if (contextMap != null && contextMap.isEmpty()) {
      CONTEXT.set(null);
    }
  }

  public static void put(String key, String value) {
    Map<String, String> contextMap = contextMap(true);
    contextMap.put(key, value);
  }

  public static void remove(String key) {
    Map<String, String> contextMap = contextMap(false);
    if (contextMap != null) {
      contextMap.remove(key);
      doCleanup();
    }
  }

  public static boolean existKey(String key) {
    Map<String, String> contextMap = contextMap(false);
    return contextMap != null && contextMap.containsKey(key);
  }

}
