package com.main;


import com.main.LogFormatter;
import com.main.AppConfig;

import java.io.*;
//import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import spark.Request;

public class Util {
	
	public static Logger logger;
	public static String PATH = "src/main/resources/data";
    private static FileHandler fileHandler;
	private static String currentDateUTC, proyectName;
	
	public static void startLog() {
		String serverName = Main.serverName;
		startLog(serverName);
	}
	public static void startLog(String project) {
		if(logger==null) {
	        logger = Logger.getLogger(Main.class.getName());
	        logger.setUseParentHandlers(false); // evita duplicar logs en consola
	        proyectName = project;
	        // Crear log inicial
	        currentDateUTC = getCurrentTimeUtc("yyyy-MM-dd");
	        setupFileHandler();
		} else {
			logger.info("El Logger ya fue iniciado");
		}
		/*try {
			scheduler = Executors.newSingleThreadScheduledExecutor();
			// calcula el tiempo hasta la próxima medianoche UTC
	        long initialDelay = Duration.between(
	                Instant.now(),
	                LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
	        ).toMillis();
	        initialDelay = 30000;
	        Util.logger.info("Logger programado: "+formatMillis(initialDelay));
	        // programa la rotación diaria exacta
	        scheduler.scheduleAtFixedRate(Util::checkLogDate, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

		} catch (Exception e) {
			printError(e);
		}*/
    }
	public static String getWebappUrl() {
		File path = AppConfig.storageDir;
		String url = "src/main/webapp";
		if(path == null) path = new File(url);
        else url = path.getParentFile().getPath();
        logger.fine("Path: "+url);
        
        return url;
	}

    private static void setupFileHandler() {
        try {
            File path = AppConfig.storageDir;
            if(path == null) path = new File(PATH);
            else PATH = path.getPath();
            System.out.println("Path: "+PATH);
            
            String fileName = path + "/PlatformData/log-" + currentDateUTC + ".log";
            //new File(path + "/logs").mkdirs();

            fileHandler = new FileHandler(fileName, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            ConsoleHandler consoleH = new ConsoleHandler();
            consoleH.setLevel(Level.FINE);
            logger.addHandler(consoleH);
            
            LogFormatter formatterColor = new LogFormatter();  
            LogFormatter.PROYECT = proyectName;
            consoleH.setFormatter(formatterColor);
            
            logger.setLevel(Level.FINE);
            logger.info("Iniciando log para el proyecto " + proyectName + " (" + currentDateUTC + ")");
        } catch (IOException e) {
            e.printStackTrace();
            printError(e);
        }
    }

    public static void rotateLog() {
        logger.info("Rotando log diario (UTC)...");
        if (fileHandler != null) {
            fileHandler.close();
            logger.removeHandler(fileHandler);
        }
        File path = AppConfig.storageDir;
        if(path == null) path = new File(PATH);
        else PATH = path.getPath();
        System.out.println("Path: "+PATH);
        String fileName = path + "/PlatformData/log-" + currentDateUTC + ".log";
        //new File(path + "/logs").mkdirs();

        try {
			fileHandler = new FileHandler(fileName, true);
	        fileHandler.setFormatter(new SimpleFormatter());
	        logger.addHandler(fileHandler);
	        logger.info("Se actualizo el archivo log "+ proyectName + " (" + currentDateUTC + ")");
		} catch (SecurityException e) {
			e.printStackTrace();
			printError(e);
		} catch (IOException e) {
			e.printStackTrace();
			printError(e);
		}
        //setupFileHandler();
    }
    /** Llama este método antes de escribir cualquier log */
    public static void checkLogDate() {
        String todayUTC = getCurrentTimeUtc("yyyy-MM-dd");
        Util.logger.fine("Revisando si se necesita un nuevo Archivo Log");
        if (!todayUTC.equals(currentDateUTC)) {
            currentDateUTC = todayUTC;
            rotateLog();
        }
    }
    
	/**
	 * Inicializa el metodo de Logger para documentar todas las actividades del servicio
	 */
	public static void startLogOld(String serverName) {
		logger = Logger.getLogger(Main.class.getName());  
        FileHandler fh;  
        File path = AppConfig.storageDir;
        if(path == null) path = new File(PATH);
        else PATH = path.getPath();
        System.out.println("Path: "+PATH);
        try { 
        	String date = getCurrentTime("YYYY-MM-dd");
            // This block configure the logger with handler and formatter  
            fh = new FileHandler(PATH+"/PlatformData/log-"+date+".log");  
            logger.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();  
            fh.setFormatter(formatter);  
            
            ConsoleHandler consoleH = new ConsoleHandler();
            consoleH.setLevel(Level.FINE);
            logger.addHandler(consoleH);
            
            LogFormatter formatterColor = new LogFormatter();  
            LogFormatter.PROYECT = serverName;
            consoleH.setFormatter(formatterColor);
            
            logger.setLevel(Level.FINE);
            // the following statement is used to log any messages  
            logger.info("Iniciando");  

        } catch (SecurityException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
	}
	
	public static void terminateLog() {
		if (fileHandler != null) {
            fileHandler.close();
        }
		logger.setLevel(Level.OFF);
	}
	public static void stopLog() {
		logger.setLevel(Level.OFF);
	}
	public static void activateLog() {
		logger.setLevel(Level.FINE);
		
	}
	
	/**
     * Convierte milisegundos a un String usando el formato indicado.
     * Ejemplo de formatos:
     * - "hh:mm:ss"
     * - "dd días hh horas mm minutos"
     * - "hh horas mm min ss seg"
     * 
     * Si el formato es null o vacío, usa "hh:mm:ss" por defecto.
     */
    public static String formatMillis(long millis) {
    	return formatMillis(millis, null);
    }
    public static String formatMillis(long millis, String format) {
        if (millis < 0) return "0";
        if (format == null || format.isEmpty()) format = "hh:mm:ss";

        long totalSeconds = millis / 1000;
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60;
        long minutes = totalMinutes % 60;
        long totalHours = totalMinutes / 60;
        long hours = totalHours % 24;
        long days = totalHours / 24;

        // Reemplazar los tokens del formato
        String result = format;
        result = result.replaceAll("dd", String.format("%02d", days));
        result = result.replaceAll("hh", String.format("%02d", (days * 24) + hours)); // hh totales si se desea
        result = result.replaceAll("HH", String.format("%02d", hours)); // HH = solo horas del día
        result = result.replaceAll("mm", String.format("%02d", minutes));
        result = result.replaceAll("ss", String.format("%02d", seconds));

        return result.trim();
    }
	/**
	 * Devuelve el valor String de un Objeto.
	 * En caso de no encontrar un valor, se devuelve el texto "null"
	 * @param ob
	 * @return
	 */
	public static String getString(Object ob) {
		try {
			//System.out.println(ob);
			if(ob == null) return "null";
			String s = (String) ob;
			if(s.equals("")) return "null";
			return s;
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "null";
	}
	/**
	 * Se devuelve el Valor numerico de un objeto en forma de texto.
	 * En caso de no encontrar un valor, se devuelve el texto "null"
	 * @param ob
	 * @return
	 */
	public static String getInt(Object ob) {
		try {
			//System.out.println(ob);
			if(ob == null) return "null";
			if(ob instanceof Integer) {
				int s = (Integer) ob;
				String t = s+"";
				//System.out.println(s);
				if(t == null || t.equals("")) return "null";
				return t;
			}
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "null";
	}
	/**
	 * Se devuelve el valor flotante de un objeto, en formato de texto.
	 * En caso de no encontrar un valor, se devuelve el texto "null"
	 * @param ob
	 * @return
	 */
	public static String getFloat(Object ob) {
		try {
			//System.out.println(ob);
			if(ob == null) return "null";
			if(ob instanceof Float) {
				float s = (Float) ob;
			//System.out.println(s);
				String t = s+"";
				if(t == null || t.equals("")) return "null";
				return t;
			}
		} catch(Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		try {
			//System.out.println(ob);
			if(ob == null) return "null";
			double s = (Double) ob;
			String t = s+"";
			if(t == null || t.equals("")) return "null";
			return t;
		} catch(Exception e) {
			//e.printStackTrace();
		}
		return "null";
	}
	/**
	 * Devuelve el valor numerico de un Objeto, en formato de Long.
	 * En caso de no encontrar un valor, se devuelve el "-1"
	 * @param ob
	 * @return
	 */
	public static long getLong(Object ob) {
		try {
			//System.out.println(ob);
			if(ob == null) return -1;
			long s = (Long) ob;
			return s;
		} catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Devuelve el valor boleano de un objeto.
	 * En caso de no encontrar un valor, se devuelve false
	 * @param ob
	 * @return
	 */
	public static boolean getBoolean(Object ob) {
		try {
			if(ob == null) return false;
			boolean s = (Boolean) ob;
			return s;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Devuelve el valor de fecha actual de forma local, en milisegundos
	 * @return
	 */
	public static long getCurrentMiliseconds() {
		Instant ins = Instant.now();
		Date date = Date.from(ins);
		return date.getTime();
	}
	/**
	 * Devuelve el valor de fecha actual de forma local, en formato "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String getCurrentTime() {
		Instant ins = Instant.now();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = Date.from(ins);
		return sdf.format(date);
	}
	/**
	 * Devuelve el valor de fecha actual de forma local, en el formato espesificado
	 * @param format
	 * @return
	 */
	public static String getCurrentTime(String format) {
		Instant ins = Instant.now();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = Date.from(ins);
		return sdf.format(date);
	}
	public static String getCurrentTimeUtc() {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return utcNow.format(formatter);
    }
	public static String getCurrentTimeUtc(String format) {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return utcNow.format(formatter);
    }
	public static Timestamp getCurrentTimestampUTC() {
		Timestamp utcNow = Timestamp.from(Instant.now());
		return utcNow;
	}
	public static Timestamp getCurrentTimestampLocal() {
		ZoneId mexicoZone = ZoneId.of("America/Mexico_City");
	    ZonedDateTime nowInMexico = ZonedDateTime.now(mexicoZone);

	    // Obtener el desplazamiento real en segundos
	    int offsetInSeconds = nowInMexico.getOffset().getTotalSeconds();
	    int offsetHours = offsetInSeconds / 3600;

	    // Corregir si se está aplicando erróneamente UTC-5 (horario de verano)
	    if (offsetHours == -5) {
	    	//Util.logger.fine("restando una hora");
	        nowInMexico = nowInMexico.minusHours(1);
	        //Util.logger.fine(nowInMexico.toString());
	    }

	    // Convertir a Timestamp
	    Timestamp mexicoTimestamp = Timestamp.valueOf(nowInMexico.toLocalDateTime());
	    return mexicoTimestamp;
	}
	public static Instant getCurrentTimeUtcInstant() {
        ZonedDateTime utcNow = ZonedDateTime.now(ZoneOffset.UTC);
        return utcNow.toInstant();
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //return utcNow.format(formatter);
    }
	

	public static String formatearTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
     // Convertir el Timestamp a un Instant (siempre en UTC)
        Instant instant = timestamp.toInstant();

        // Asignar zona UTC
        ZonedDateTime utcDateTime = instant.atZone(ZoneOffset.UTC);

        // Formateador sin zona, solo fecha y hora
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return formatter.format(utcDateTime);
    }
	/**
	 * Convierte una cadena de texto con formato de fecha, en su equivalente numerico
	 * @param dateText
	 * @return
	 */
	public static long dateToMil(String dateText) {
		//System.out.println("Convertir fecha "+dateText);
		if(dateText == null) return -1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date;
		try {
			date = sdf.parse(dateText);
			return date.getTime();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	/**
	 * Convierte un valor numerico a su equivalente en una fecha en formato "yyyy-MM-dd HH:mm:ss"
	 * @param time
	 * @return
	 */
	public static String milToDate(long time) {
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateText = sdf.format(date);
		return dateText;
	}
	/**
	 * Convierte un valor numerico a su equivalente en una fecha en el formato espesificado
	 * @param time
	 * @param format
	 * @return
	 */
	public static String formatDate(long time, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date = new Date(time);
		return sdf.format(date);
	}
	/**
	 * Convierte una cadena de texto con formato de fecha, a el formato de fecha espesificado
	 * @param dateText
	 * @param format
	 * @return
	 */
	public static String formatDate(String dateText, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Date date;
		try {
			date = sdf.parse(dateText);
			return sdf.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateText;
	}
	/**
	 * Convierte una variable Date a un formato espesificado y devuelve una cadena de texto
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * Elimina todos los espacios en blanco de una cadena de texto
	 * @param text
	 * @return
	 */
	public static String removeBlanks(String text) {
		if(text==null) return null;
		String newText = "";
		for(int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if(c==' ' && newText.equals("")) continue;
			else {
				newText+=c;
			}
		}
		return newText;
	}
	/**
	 * Convertir una cadena de texto a un valor numerico
	 * @param text
	 * @return
	 */
	public static int tryParseInt(String text) {
	  try {
		  if(text==null) return -1;
		  text = removeBlanks(text);
		  text = text.replaceAll("\\s+","");
	    return Integer.parseInt(text);
	  } catch (NumberFormatException e) {
		  //System.out.println("No se convirtio el string a entero "+text);
	    return -1;
	  }
	}
	/**
	 * Convertir una cadena de texto a un valor numerico
	 * @param text
	 * @return
	 */
	public static Float tryParseFloat(String text) {
	  try {
		  if(text==null) return null;
		  text = removeBlanks(text);
		  text = text.replaceAll("\\s+","");
	    return Float.parseFloat(text);
	  } catch (NumberFormatException e) {
		  //System.out.println("No se convirtio el string a float "+text);
	    return null;
	  }
	}
	/**
	 * Convertir un numero en una cadena de texto
	 * @param number
	 * @return
	 */
	public static String parseInt(int number) {
		try {
			return String.valueOf(number);
		} catch(Exception e) {
			e.printStackTrace();
			return "NaN";
		}
	}

	public static String parseFloat(float number) {
		try {
			return String.valueOf(number);
		} catch(Exception e) {
			e.printStackTrace();
			return "NaN";
		}
	}

	@SuppressWarnings("unused")
	private static String arrayToString (String[] array) {
		String result = "[";
		for(int i=0; i< array.length; i++) {
			if(i>0) result += ", ";
			result += "\"" + array[i] + "\"";
		}
		result += "]";
		return result;
	}
	
	/**
	 * Buscar si un elemento numerico se encuentra dentro de un arreglo de numeros
	 * @param arr
	 * @param toCheckValue
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean checkForElementInArray(int[] arr, int toCheckValue)
    {
        // sort given array
        Arrays.sort(arr);
        int res = Arrays.binarySearch(arr, toCheckValue);
        boolean test = res >= 0 ? true : false;
        return test;
    }
	/**
	 * Buscar un si un elemento numerico se encuentra dentro de un arreglo de textos
	 * @param arr
	 * @param y
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean checkForElementInArray(String[] arr, String y)
	{
		//System.out.println("buscando "+y+ " en arreglo:");
		//System.out.println(Arrays.toString(arr));
        for(int i=0;i<arr.length;i++) {
        	String x = arr[i];
        	if(x==null) continue;
        	//System.out.println("Check "+x+" with "+ y);
        	if(x.equals(y)) {
        		//System.out.println("repetido...");
        		return true;
        	}
        }
        //System.out.println("no repedito...");
        return false;
    }
	
	/**
	 * Revisa el tramaño de una cadena de texto
	 * @param text
	 */
	public static void checkSize(String text) {
		if(text == null) {
			print("String vacio");
			return;
		}
		if(text.equals("") || text.equals("null")) {
			print("String vacio");
			return;
		}
		
		int n = text.length();
		print("Largo del texto "+n);

		byte[] utf32Bytes;
		try {
			utf32Bytes = text.getBytes("UTF-32");
			print("Tamaño en bites 32: "+utf32Bytes.length); // prints "16"
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Decodificar una cadena de texto con formato de una URL
	 * @param url
	 * @return
	 */
	public static String decodeURL(String url) {
		try {
		    url = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return url;
	}
	/**
	 * Elimina el ultimo caracter de una cadena de texto
	 * @param s
	 * @return
	 */
	public static String removeLastChar(String s) {
	    return (s == null || s.length() == 0)
	      ? null 
	      : (s.substring(0, s.length() - 1));
	}
	/**
	 * Regresa una cadena de texto con el formato de una URL local con el nombre del proyecto seleccionado
	 * @param req
	 * @return
	 */
	public static String getLocalPath(Request req) {
		String sproyecto = req.queryParams("sproyecto");
		if(sproyecto == null) sproyecto = "default";
		else if(sproyecto.equals("")) sproyecto = "default";
		String path = PATH+"/"+sproyecto;
		return path;
	}
	/**
	 * Muestra un nuevo mensaje de texto en el Log
	 * @param text
	 */
	public static void print(String text) {
		//String date = getCurrentTime();
		//System.out.println(date+" - "+text);
		logger.fine(text);  
	}
	/*
	 * Muestra un mensaje de error en el Log
	 */
	public static void printError(Exception e) {
		if (logger != null) {
	        logger.log(Level.SEVERE, getStackTraceAsString(e), e);
	    } else {
	        e.printStackTrace();
	    } 
	}
	private static String getStackTraceAsString(Exception e) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    e.printStackTrace(pw);
	    return sw.toString();
	}
	
	public static void clearScreen() {  
	    System.out.print("\033[H\033[2J");  
	    System.out.flush();  
	} 
	/*
	 * Elimina las comas (,) de una cadena de texto y los reemplaza con (||)
	 */
	public static String removeComa(String text) {
		if(text == null) return "";
		if(text.indexOf(",")==-1) return text;
		else {
			String x = text.replaceAll(",", "||");
			return x;
		}
	}
	

    public static Integer safeToInteger(Object value) {
        if (value == null) return -1;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        if (value instanceof Number) return ((Number) value).intValue();
        return null;
    }
    public static Double safeToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value instanceof String) {
            try { return Double.parseDouble((String) value); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }
    public static Float safeToFloat(Object value) {
        if (value == null) return null;
        if (value instanceof Float) return (Float) value;
        if (value instanceof Number) return ((Number) value).floatValue();
        if (value instanceof String) {
            try { return Float.parseFloat((String) value); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }
    public static Long safeToLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try { return Long.parseLong((String) value); }
            catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    public static boolean safeToBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if(value instanceof String) {
        	String x = (String) value;
        	if("true".equals(x)) return true;
        	if("false".equals(x)) return false;
        	if(x.equals("1"))return true;
        	return false;
        }
        return false;
    }
    public static String safeToString(Object value) {
        return (value != null) ? value.toString() : null;
    }
    
    public static String normalize(String input) {
		if (input == null) return "";
		String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
		normalized = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); // quita acentos
		normalized = normalized.replaceAll("[^\\p{Alnum}]", ""); // quita caracteres no alfanuméricos
		return normalized.toLowerCase(); // todo a minúsculas
	}
	
	public static Timestamp parseTimestamp(String textoFecha) {
        // Formato esperado: "2025-06-20 00:00:00"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(textoFecha, formatter);
        return Timestamp.valueOf(localDateTime);
    }
	
	public static String maskToken(String value) {
	    if(value == null) return null;
	    value = value.trim();
	    if(value.isEmpty()) return null;
	    // si es muy corto no vale la pena ocultarlo
	    if(value.length() <= 10) {
	    	return value;
	    }
	    int startLength = 6;
	    int endLength = 4;
	    String start =
	        value.substring(0, startLength);
	    String end =
	        value.substring(
	            value.length() - endLength
	        );
	    return start + "..." + end;
	}
	
}
