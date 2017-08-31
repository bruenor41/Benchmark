package magiclib.core;

import java.util.HashMap;

import magiclib.Global;

public class Theme {
    private static HashMap<String, Integer> resourceMap;

    public static void clear() {
        if (resourceMap !=null) {
            resourceMap.clear();
        }
        resourceMap = null;
    }

    public static void add(String code, int resourceID) {
        if (resourceMap == null) {
            resourceMap = new HashMap<>();
        }

        resourceMap.put(code, resourceID);
    }

    public static int get(String code) {
        return resourceMap.get(code);
    }
}
