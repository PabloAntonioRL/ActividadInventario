package com.main;


import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONUtil {
	

	/**
	 * Convierte un objeto JSON en un formato de texto
	 * @param json JSONObject
	 * @return String
	 */
	public static String JSONToString(JSONObject json) {
		try {
			Gson gson = new GsonBuilder()
	                .serializeNulls() // incluir valores null
	                .setPrettyPrinting()
	                .create();
	        String pretty = gson.toJson(json);
	        return pretty;
		} catch(Exception e) {
			Util.printError(e);
		}
		return "{}";
	}
	/**
	 * Convierte un texto con formato de json, en un objeto JSON
	 * @param string String
	 * @return JSONObject
	 */
	public static JSONObject StringToJSON(String string) {
		if(string==null || string.isBlank()) return null;
		JSONParser parser = new JSONParser();
		try {
			Object jsonParser;
			jsonParser = parser.parse(string);
			JSONObject obj = (JSONObject)jsonParser;
			return obj;
		} catch (ParseException e) {
			Util.printError(e);
		}
        return null;
	}
	/**
	 * Devuelve un objeto JSON desde dentro de otro objeto JSON teniendo el nombre de la propiedad
	 * 
	 * JSON = { OtroJSON: { ... } }
	 * Devuelve "OtroJSON"
	 * @param obj
	 * @param key
	 * @return JSONObject
	 */
	public static JSONObject getJSON (JSONObject obj, String key) {
		if(obj==null || key==null || key.isBlank()) return null;
		if(obj.get(key)!=null && obj.get(key) instanceof JSONObject) {
			JSONObject json = (JSONObject) obj.get(key);
			return json;
		}
		return null;
	}
	/**
	 * Devuelve un arreglo de JSON desde dentro de obteo objeto JSON
	 * @param obj
	 * @param key
	 * @return JSONArray
	 */
	public static JSONArray getArray (JSONObject obj, String key) {
		if(obj==null || key==null || key.isBlank()) return null;
		if(obj.get(key) != null && obj.get(key) instanceof JSONArray) {
			JSONArray json = (JSONArray) obj.get(key);
			return json;
		}
		return null;
	}
	/**
	 * Devuelve un arreglo de textos desde dentro de otro Objeto JSON
	 * @param obj
	 * @param key
	 * @return String[]
	 */
	public static String[] getArrayString (JSONObject obj, String key) {
		JSONArray arr = (JSONArray) obj.get(key);
		if(arr == null) {
			return new String[0];
		}
		String[] list = new String[getArrayLength(arr)];
		int i=0;
		@SuppressWarnings("unchecked")
		Iterator<String> it = arr.iterator();
		while (it.hasNext()) {
			list[i] = it.next();
			i++;
		}
		return list;
	}
	/**
	 * Devuelve el numero de elementos dentro de un arreglo JSON
	 * @param arr
	 * @return int
	 */
	public static int getArrayLength(JSONArray arr) {
		int i=0;
		@SuppressWarnings("unchecked")
		Iterator<JSONObject> it = arr.iterator();
		while (it.hasNext()) {
			it.next();
			i++;
		}
		return i;
	}
	/**
	 * Convierte un JSONArray en un arreglo de textos
	 * @param arr
	 * @return String[]
	 */
	public static String[] JsonArrayToArray(JSONArray arr) {
		String[] list = new String[getArrayLength(arr)];
		int i=0;
		@SuppressWarnings("unchecked")
		Iterator<String> it = arr.iterator();
		while (it.hasNext()) {
			list[i] = it.next();
			i++;
		}
		return list;
	}
}
