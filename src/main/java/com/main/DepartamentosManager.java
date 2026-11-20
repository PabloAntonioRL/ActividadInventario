package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Request;

public class DepartamentosManager {
	
	
	public static ApiResponse insertDepartamento(JSONObject item) throws SQLException {
		Util.logger.info("Insertando Departamento...");
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de departamento");
	    }
	    String nombre = (String) item.get("nombre_departamento");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de departamento");
	    }
	    int numCat = Util.safeToInteger(item.get("numero_categoria"));
	    if (numCat<=0) {
	        return new ApiResponse(2, "Falta nombre de categoria");
	    }
	    
	    String sql = "CALL insertar_departamento(?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numCat);
	            cs.setString(2, nombre);
	            cs.registerOutParameter(3, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Departamento creado correctamente",
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al insertar departamento: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }

	}

	public static ApiResponse updateDepartamento(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de departamento", null);
	    }
	    String nombre = (String) item.get("nombre_departamento");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de departamento", null);
	    }
	    int numDep = Util.safeToInteger(item.get("numero_departamento"));
	    if (numDep == -1) {
	        return new ApiResponse(2, "Falta el numero de departamento", null);
	    }
	    int numCat = Util.safeToInteger(item.get("numero_categoria"));
	    if (numCat<=0) {
	        return new ApiResponse(2, "Falta nombre de categoria");
	    }
	    if(item.get("activo")==null) {
	    	return new ApiResponse(2, "Falta parametro 'Activo'", null);
	    }
	    boolean activo = Util.safeToBoolean(item.get("activo"));
	    
	    String sql = "CALL actualizar_departamento(?, ?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numCat);
	        	cs.setInt(2, numDep);
	            cs.setString(3, nombre);
	            cs.setBoolean(4, activo);
	            cs.registerOutParameter(5, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(5)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Clase actualizada correctamente",
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al insertar clase: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	}
	    
	public static ApiResponse getDataFromDepartamentos(Request req) {
		int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numCat<=0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
		/*if(numDep<=0) {
			return new ApiResponse(2, "Falta el numero de departamento");
		}*/
		String sql = "CALL obtener_departamentos(?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numCat);
	            cs.setInt(2, numDep);
	            cs.registerOutParameter(3, Types.OTHER);
	            cs.execute();
	            
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(3)) {
	            	if (rs != null)
	            		resultado = ResultSetToJsonConverter.toArray(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0, "Ok, se encontraron "+resultado.size()+" departamentos", resultado);
	        }
	    } catch (SQLException e1) {
            Util.printError(e1);
            Util.logger.warning(
                    "Error al obtener departamento: " + e1.getMessage());
            return new ApiResponse(7, DBManager.getDatabaseError(e1));
		}
	}
	
	public static ApiResponse eliminarDepartamento(Request req) {
		boolean hardDelete = Util.safeToBoolean(req.queryParamOrDefault("trueDelete", "false"));
		int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numCat<=0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de departamento");
		}
		if(hardDelete==false) {
			Util.logger.warning("Se realizara un softDelete para el departamento "+numDep);
			String sql = "CALL desactivar_departamento(?, ?)";
			try (Connection conn = DBManager.getConnection();
			     CallableStatement cs = conn.prepareCall(sql)) {
			    cs.setInt(1, numCat);
			    cs.setInt(2, numDep);
			    cs.execute();

			    return new ApiResponse(0, "Departamento desactivado correctamente");
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		} else {
			Util.logger.warning("Se eliminara el departamento "+numDep);
			String sql = "CALL eliminar_departamento(?)";
		    try (Connection conn = DBManager.getConnection();
		        CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numDep);
	            cs.execute();

	            return new ApiResponse(0, "Se elimino el departamento "+numDep);
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		}
	}
	
}
