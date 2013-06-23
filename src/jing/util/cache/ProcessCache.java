package jing.util.cache;

import java.util.HashMap;
import java.util.Map;

public class ProcessCache {
	private static Map<String, Integer> cache = new HashMap<String, Integer>();

	public synchronized static Integer getCacheValue(String key) {
		if (cache.containsKey(key))
			return cache.get(key);
		return null;
	}

	public synchronized static void setCacheValue(String key, Integer value) {
		cache.put(key, value);
	}
}
