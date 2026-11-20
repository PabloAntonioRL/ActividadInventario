package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.json.simple.JSONArray;
import spark.Request;

public class DBManager {
	
	private static final String defaultUrl = "jdbc:postgresql://localhost:5432/actividad_inventario";
	private static final String USER = "postgres"; 
	private static final String PASS = "136479"; 
	public static Connection getConnection() {
		try {
			 return DriverManager.getConnection(defaultUrl, USER, PASS);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			Util.printError(e);
			return null;
		}
	}
	
	

	public static ApiResponse getDataFrom(Request req) throws SQLException {
		String table = req.queryParamOrDefault("from", "");
		if(table==null || table.isBlank()) {
			ApiResponse response = new ApiResponse(3, "Nombre de tabla invalida");
    		return response;
		}
		
	    // Validamos las tablas permitidas
	    if (!Arrays.asList("inventario", "departamentos", "clases", "familias", "categorias")
	            .contains(table)) {
	        throw new IllegalArgumentException("Tabla no permitida: " + table);
	    }
	    
	    Util.logger.info("Buscando informacion de "+table);
	    int cantidad = req.queryParams().size();
	    if (cantidad == 1) {
	    	String sql = "CALL obtener_todos_desde(?, ?)";

		    try (Connection conn = DBManager.getConnection()) {
		        conn.setAutoCommit(false);
		        try (CallableStatement cs = conn.prepareCall(sql)) {
		            cs.setString(1, table);
		            cs.registerOutParameter(2, Types.OTHER);
		            cs.execute();
		            
		            JSONArray resultado = null;
		            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
		            	if (rs != null)
		            		resultado = ResultSetToJsonConverter.toArray(rs);
		            }

		            conn.commit();
		            return new ApiResponse(0,
	                        "Ok. Obteniendo datos de "+table+" "+resultado.size(),
	                        resultado);
		        } catch (SQLException e) {
		            conn.rollback();
		            Util.printError(e);
		            Util.logger.warning(
		                    "Error al obtener datos: " + e.getMessage());
		            return new ApiResponse(7, DBManager.getDatabaseError(e));
		        }
		    }
	    } else {
	    	switch(table) {
	    	default: throw new IllegalArgumentException("Tabla no permitida: " + table);
	    	case "inventario": return InventarioManager.getItemsBySku(req);
	    	case "departamentos": return DepartamentosManager.getDataFromDepartamentos(req);
	    	case "clases": return ClasesManager.getDataFromClases(req);
	    	case "familias": return FamiliasManager.getDataFromFamilias(req);
	    	case "categorias": return CategoriaManager.getDataFromCategoria(req);
	    	}
	    	
	    }
	}
	
	public static ApiResponse getAllInactive(Request req) throws SQLException {
		String table = req.queryParamOrDefault("from", "");
		if(table==null || table.isBlank()) {
			ApiResponse response = new ApiResponse(3, "Nombre de tabla invalida");
    		return response;
		}
		
	    // Validamos las tablas permitidas
	    if (!Arrays.asList("inventario", "departamentos", "clases", "familias", "categorias")
	            .contains(table)) {
	        throw new IllegalArgumentException("Tabla no permitida: " + table);
	    }
	    
	    Util.logger.info("Buscando informacion de "+table);
    	String sql = "CALL obtener_todos_inactivos(?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setString(1, table);
	            cs.registerOutParameter(2, Types.OTHER);
	            cs.execute();
	            
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
	            	if (rs != null)
	            		resultado = ResultSetToJsonConverter.toArray(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Obteniendo datos de "+table+" "+resultado.size(),
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al obtener datos: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	    
	}


	public static ApiResponse reactivarElemento(Request req) throws SQLException {
	    String table = req.queryParamOrDefault("from", "");
	    if (table == null || table.isBlank()) {
	        return new ApiResponse(3, "Nombre de tabla inválido");
	    }
	    // Validar tablas permitidas
	    if (!Arrays.asList("departamentos", "clases", "familias", "categorias")
	            .contains(table.toLowerCase())) {
	        return new ApiResponse(3, "Tabla no permitida: " + table);
	    }

	    int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
	    int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
	    int numCla = Util.safeToInteger(req.queryParamOrDefault("numero_clase", "-1"));
	    int numFam = Util.safeToInteger(req.queryParamOrDefault("numero_familia", "-1"));
	    // Validar clave primaria según el elemento
	    if (numCat<=0) {
	        return new ApiResponse(3, "Falta el número de categoria");
	    }
	    if (table.equalsIgnoreCase("departamentos") && numDep<=0) {
	        return new ApiResponse(3, "Falta el número de departamento");
	    }
	    if (table.equalsIgnoreCase("clases") ||
	        table.equalsIgnoreCase("familias")) {
	        if (numCla<=0) {
	            return new ApiResponse(3, "Falta el número de clase");
	        }
	    }
	    if (table.equalsIgnoreCase("familias")) {
	        if (numFam<=0) {
	            return new ApiResponse(3, "Falta el número de familia");
	        }
	    }
	    Util.logger.info("Reactivando elemento de " + table);
	    String sql = "CALL reactivar_elemento(?, ?, ?, ?, ?, ?)";
	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setString(1, table);
	            cs.setInt(2, numCat);
	            if (numDep>0) cs.setInt(3, numDep);
	            else cs.setNull(3, Types.NUMERIC);
	
	            if (numCla>0) cs.setInt(4, numCla);
	            else cs.setNull(4, Types.NUMERIC);
	
	            if (numFam>0) cs.setInt(5, numFam);
	            else cs.setNull(5, Types.NUMERIC);
	            
	            cs.registerOutParameter(6, Types.OTHER);
	            cs.execute();
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(6)) {
	                if (rs != null) {
	                    resultado = ResultSetToJsonConverter.toArray(rs);
	                }
	            }
	            conn.commit();
	            return new ApiResponse(
	                    0,
	                    "Elemento reactivado correctamente",
	                    resultado
	            );
	        } catch (SQLException | NumberFormatException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al reactivar elemento: " + e.getMessage());
	            if (e instanceof SQLException) {
	                return new ApiResponse(
	                        7,
	                        DBManager.getDatabaseError((SQLException) e)
	                );
	            }
	            return new ApiResponse(3, "Los valores de la clave primaria deben ser numéricos");
	        }
	    }
	}

	
	/*
	 * ========================================================================
	 */


	public static String getDatabaseError(Exception e) {
	
	    if (e instanceof SQLException) {
	
	        SQLException sqlEx = (SQLException) e;
	        String state = sqlEx.getSQLState();
	
	        if (state != null) {
	
	            switch (state) {
	
	                case "23505":
	                    return "Error: Ya existe un registro con esa llave primaria o valor único.";
	
	                case "23503": {
	
	                    String message = sqlEx.getMessage().toLowerCase();
	
	                    if (message.contains("still referenced")
	                            || message.contains("violates foreign key constraint")
	                            || message.contains("viola la llave foránea")) {
	
	                        return "Error: No se puede eliminar el registro porque "
	                                + "existen elementos que dependen de él.";
	                    }
	
	                    return "Error: No se puede realizar la operación porque "
	                            + "la clasificación indicada no existe o no tiene "
	                            + "una relación válida. Verifica departamento, clase y familia.";
	                }
	
	                case "23502":
	                    return "Error: Falta un campo obligatorio.";
	
	                case "22003":
	                    return "Error: Uno de los valores numéricos excede el límite permitido.";
	
	                case "22001":
	                    return "Error: Uno de los textos supera la longitud permitida.";
	
	                case "22P02":
	                    return "Error: Uno de los valores tiene un formato incorrecto.";
	
	                case "P0001":
	                    return "Error: " + sqlEx.getMessage();
	
	                case "40001":
	                    return "Error: La operación entró en conflicto con otra transacción. Intenta nuevamente.";
	
	                case "40P01":
	                    return "Error: Se detectó un bloqueo entre operaciones. Intenta nuevamente.";
	
	                default:
	                    return "Error SQL [" + state + "]: " + sqlEx.getMessage();
	            }
	        }
	    }
	
	    return "Error: " + e.getMessage();
	}



}
