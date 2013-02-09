package com.w0rp.androidutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;


/*
 * This class provides various utility methods for constructing and
 * deconstructing JSON sequences.
 */
public abstract class JSON {
	public static class JSONArrayIterator implements Iterator<Object> {
		private JSONArray arr;
		private int current = 0;
		
		public JSONArrayIterator(JSONArray arr) {
			this.arr = arr;
		}

		@Override
		public boolean hasNext() {
			return arr != null && current < this.arr.length();
		}

		@Override
		public Object next() {
			return hasNext() ? this.arr.opt(current++) : null;
		}

		@Override
		public void remove() {}
	}
	
	public static JSONObject obj(String jsonString) {
		try {
			return new JSONObject(jsonString);
		} catch (Exception e) {
			return new JSONObject();
		}
	}
	
	public static JSONArray arr(String jsonString) {
		try {
		    return new JSONArray(jsonString);
		} catch (Exception e) {
			return new JSONArray();
		}
	    
	}
	
	public static List<String> keys(JSONObject obj) {
	    List<String> keyList = new ArrayList<String>();
	    
	    if (obj == null) {
            return keyList;
        }
	    
        Iterator<?> keyIter = obj.keys();
        
        while(keyIter.hasNext()){
            keyList.add((String) keyIter.next());
        }
	    
	    return keyList;
	}
	
	public static Iterable<Object> iter(JSONArray arr) {
		return Util.iter(new JSONArrayIterator(arr));
	}
	
	public static Iterable<Object> iter(JSONObject obj, String key) {
		return iter(obj != null ? obj.optJSONArray(key) : null);
	}
	
	public static Set<String> stringSet(JSONArray arr) {
		return Util.stringSet(iter(arr));
	}
	
	public static Set<String> stringSet(JSONObject obj, String key) {
		return Util.stringSet(iter(obj, key)); 
	}
	
	public static List<String> stringList(JSONArray arr) {
		return Util.stringList(iter(arr));
	}
	
	public static List<String> stringList(JSONObject obj, String key) {
		return Util.stringList(iter(obj, key)); 
	}
	
	public static List<JSONArray> arrList(Iterable<Object> iter) {
		List<JSONArray> list = new ArrayList<JSONArray>();
		
		for (Object obj : iter) {
			if (obj instanceof JSONArray) {
				list.add((JSONArray) obj);
			}
		}
		
		return list;
	}
	
	public static List<JSONArray> arrList(JSONArray arr) {
		return arrList(iter(arr));
	}
	
	public static List<JSONArray> arrList(JSONObject obj, String key) {
		return arrList(iter(obj, key)); 
	}
	
	public static List<JSONObject> objList(Iterable<Object> iter) {
		List<JSONObject> list = new ArrayList<JSONObject>();
		
		for (Object obj : iter) {
			if (obj instanceof JSONObject) {
				list.add((JSONObject) obj);
			}
		}
		
		return list;
	}
	
	public static List<JSONObject> objList(JSONArray arr) {
		return objList(iter(arr));
	}
	
	public static List<JSONObject> objList(JSONObject obj, String key) {
		return objList(iter(obj, key)); 
	}
}
