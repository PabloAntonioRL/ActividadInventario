package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Request;

public class CategoriaManager {
	public static ApiResponse insertCategoria(JSONObject item) throws SQLException {
		Util.logger.info("Insertando Categoria...");
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de Categoria");
	    }
	    String nombre = (String) item.get("nombre_categoria");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de Categoria");
	    }
	    
	    String sql = "CALL insertar_categoria(?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setString(1, nombre);
	            cs.registerOutParameter(2, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Categoria creado correctamente",
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al insertar Categoria: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }

	}

	public static ApiResponse updateCategoria(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de Categoria", null);
	    }
	    String nombre = (String) item.get("nombre_categoria");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de Categoria", null);
	    }
	    int numDep = Util.safeToInteger(item.get("numero_categoria"));
	    if (numDep == -1) {
	        return new ApiResponse(2, "Falta el numero de categoria", null);
	    }
	    if(item.get("activo")==null) {
	    	return new ApiResponse(2, "Falta parametro 'Activo'", null);
	    }
	    boolean activo = Util.safeToBoolean(item.get("activo"));
	    
	    String sql = "CALL actualizar_categoria(?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numDep);
	            cs.setString(2, nombre);
	            cs.setBoolean(3, activo);
	            cs.registerOutParameter(4, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. categoria actualizada correctamente",
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al insertar categoria: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	}
	    
	public static ApiResponse getDataFromCategoria(Request req) {
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		String sql = "CALL obtener_categorias(?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numDep);
	            cs.registerOutParameter(2, Types.OTHER);
	            cs.execute();
	            
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
	            	if (rs != null)
	            		resultado = ResultSetToJsonConverter.toArray(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0, "Ok, se encontraron "+resultado.size()+" departamentos", resultado);
	        }
	    } catch (SQLException e1) {
            Util.printError(e1);
            Util.logger.warning(
                    "Error al obtener categoria: " + e1.getMessage());
            return new ApiResponse(7, DBManager.getDatabaseError(e1));
		}
	}
	
	public static ApiResponse eliminarCategoria(Request req) {
		boolean hardDelete = Util.safeToBoolean(req.queryParamOrDefault("trueDelete", "false"));
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		if(hardDelete==false) {
			Util.logger.warning("Se realizara un softDelete para el categoria "+numDep);
			String sql = "CALL desactivar_categoria(?)";
			try (Connection conn = DBManager.getConnection();
			     CallableStatement cs = conn.prepareCall(sql)) {
			    cs.setInt(1, numDep);
			    cs.execute();

			    return new ApiResponse(0, "Categoria desactivada correctamente");
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		} else {
			Util.logger.warning("Se eliminara el categoria "+numDep);
			String sql = "CALL eliminar_categoria(?)";
		    try (Connection conn = DBManager.getConnection();
		        CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numDep);
	            cs.execute();

	            return new ApiResponse(0, "Se elimino el categoria "+numDep);
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		}
	}
}
