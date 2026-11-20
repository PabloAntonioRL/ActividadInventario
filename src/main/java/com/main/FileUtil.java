package com.main;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {
	
	public static String WebAppPath = "src/main/webapp";
	//public static String PATH = "src/main/resources";
	
	
	@SuppressWarnings("null")
	public static void createFolder(String name) {
		try {

	        File path = AppConfig.storageDir;
	        if(path == null) path = new File(Util.PATH);
			if(name!=null || !name.equals(""))
				Files.createDirectories(Paths.get(path+"/"+name));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Util.printError(e);
		}
	}
	
	public static void saveData(String value, String name) {
        File path = AppConfig.storageDir;
        if(path == null) path = new File(Util.PATH);
		File file = new File(path, name);
	    Util.print("Guardando datos en " + file.getAbsolutePath());
	    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
	        bw.write(value);
	    } catch (IOException e) {
	        Util.printError(e);
	    }
	}
	
	public static void saveData(String value, String name, String path) {
		String fileName = name;
		FileWriter fw;
		Util.print("Guardando datos "+name +" "+path);
		try {
			//Files.createDirectories(Paths.get(PATH));
			File file  = new File(String.valueOf(path +"/"+ fileName));
			
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(value);
			bw.close();
			//System.out.println(Util.getCurrentTime()+"- Guardado con exito "+path +"/"+ fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Util.printError(e);
		}
	}


	public static void saveJSON(String JsonString, String id) {
		FileWriter fw;
		Util.print("Guardando JSON "+id);
		try {
	        File path = AppConfig.storageDir;
	        if(path == null) path = new File(Util.PATH);
	        Util.logger.info("Ubicacion: "+path +"/"+ id+".json");
			//Files.createDirectories(Paths.get(AppConfig.storageDir));
			File file  = new File(String.valueOf(path +"/"+ id+".json"));
			
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(JsonString);
			bw.close();
			//System.out.println(Util.getCurrentTime()+"- Guardado con exito "+id);
		} catch (IOException e) {
			Util.printError(e);
		}
	}
	public static String loadJSON(String id) {
        File path = AppConfig.storageDir;
        if(path == null) path = new File(Util.PATH);
	    File file = new File(path, id + ".json");
	    Util.print("Leyendo JSON " + file.getAbsolutePath());
	    if (!file.exists()) {
	        return "{}";  // o lanza excepción
	    }
	    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
	        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
	    } catch (IOException e) {
	        Util.printError(e);
	        return "{}";
	    }
	}

	public static String loadJSONOld (String id) {
        File path = AppConfig.storageDir;
        if(path == null) path = new File(Util.PATH);
		String fileName = path+"/"+id+".json";
		String content = "{}";
		Util.print("Leyendo JSON "+id);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			reader.close();
	
			content = stringBuilder.toString();
		} catch (IOException e) {
			Util.printError(e);
		}
		Util.print("Terminado de leer "+id);
		
		return content;
	}
	
	public static String loadFile (String id) {
        File path = AppConfig.storageDir;
        if(path == null) path = new File(Util.PATH);
		String fileName = path+"/"+id;
		String content = "{}";
		Util.print("Leyendo archivo "+id+"...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			reader.close();
	
			content = stringBuilder.toString();
		} catch (IOException e) {
			Util.printError(e);
		}
		Util.print("Terminado de leer "+id);
		
		return content;
	}
	public static String loadFile (String id, String path) {
		String fileName = path+"/"+id;
		String content = "{}";
		Util.print("Leyendo archivo "+id+"...");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			StringBuilder stringBuilder = new StringBuilder();
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
			// delete the last new line separator
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			reader.close();
	
			content = stringBuilder.toString();
		} catch (IOException e) {
			Util.printError(e);
		}
		Util.print("Terminado de leer "+id);
		
		return content;
	}

	
	public static boolean checkIfFolderExist(String url) {
		Path path = Paths.get(url);
		return Files.exists(path);
	}
	public static boolean checkIfFolderExistAndCreate(String url, String type) {
		Path path = Paths.get(url);
		return Files.exists(path);
	}
	
	
}
