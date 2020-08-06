package extractocol.backend.request.basics;

import java.util.ArrayList;
import java.util.HashMap;

public class ContextTable {
	public static HashMap<String, ArrayList<ContextEntry>> Map = new HashMap<String, ArrayList<ContextEntry>>();

	public static boolean contains(Object o) {
		for (ArrayList<ContextEntry> a : Map.values()) {
			for (ContextEntry te : a) {
				if (te.equals(o))
					return true;
			}
		}
		return false;
	}

	public static String findkey(ContextEntry ce) {
		for (String key : Map.keySet()) {
			for (ContextEntry te : Map.get(key)) {
				if (te.equals(ce))
					return key;
			}
		}
		return null;
	}

	public static void addKey(String key) {
		Map.put(key, new ArrayList<ContextEntry>());
	}

	public static boolean add(ContextEntry ce) {
		String key = findkey(ce);

		if (key != null) {
			Map.get(key).add(ce);
			return true;
		} else
			return false;
	}

	public static void add(String key, ContextEntry ce) {
		// JM Auto-generated method stub
		Map.get(key).add(ce);
	}

	public static void remove(String lTaintKey, ContextEntry ce) {
		// JM Auto-generated method stub
		Map.get(lTaintKey).remove(ce);
	}
}
