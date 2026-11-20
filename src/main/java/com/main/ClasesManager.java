package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Request;

public class ClasesManager {
	
	public static ApiResponse insertClase(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de la clase", null);
	    }
	
	    String nombre = (String) item.get("nombre_clase");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de la clase", null);
	    }
	
	    int numDep = Util.safeToInteger(item.get("numero_departamento"));
	    if (numDep <=0) {
	        return new ApiResponse(2, "Falta el numero de departamento", null);
	    }
	    int numCat = Util.safeToInteger(item.get("numero_categoria"));
	    if (numCat<=0) {
	        return new ApiResponse(2, "Falta nombre de categoria");
	    }
	    
	    String sql = "CALL insertar_clase(?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numCat);
	        	cs.setInt(2, numDep);
	            cs.setString(3, nombre);
	            cs.registerOutParameter(4, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Clase creada correctamente",
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
	

	public static ApiResponse updateClase(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de clase", null);
	    }
	    String nombre = (String) item.get("nombre_clase");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de clase", null);
	    }
	    int numDep = Util.safeToInteger(item.get("numero_departamento"));
	    if (numDep == -1) {
	        return new ApiResponse(2, "Falta el numero de departamento", null);
	    }
	    int numCla = Util.safeToInteger(item.get("numero_clase"));
	    if (numCla == -1) {
	        return new ApiResponse(2, "Falta el numero de la clase", null);
	    }
	    int numCat = Util.safeToInteger(item.get("numero_categoria"));
	    if (numCat<=0) {
	        return new ApiResponse(2, "Falta nombre de categoria");
	    }
	    if(item.get("activo")==null) {
	    	return new ApiResponse(2, "Falta parametro 'Activo'", null);
	    }
	    boolean activo = Util.safeToBoolean(item.get("activo"));
	    
	    String sql = "CALL actualizar_clase(?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numCat);
	        	cs.setInt(2, numDep);
	        	cs.setInt(3, numCla);
	            cs.setString(4, nombre);
	            cs.setBoolean(5, activo);
	            cs.registerOutParameter(6, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(6)) {
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

	public static ApiResponse getDataFromClases(Request req) {
		int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numCat<=0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de departamento");
		}
		int numCla = Util.safeToInteger(req.queryParamOrDefault("numero_clase", "-1"));
		
		String sql = "CALL obtener_clases(?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numCat);
	            cs.setInt(2, numDep);
	            cs.setInt(3, numCla);
	            cs.registerOutParameter(4, Types.OTHER);
	            cs.execute();
	            
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(4)) {
	            	if (rs != null)
	            		resultado = ResultSetToJsonConverter.toArray(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0, "Ok, se encontraron "+resultado.size()+" clases", resultado);
	        } 
	    } catch (SQLException e1) {
            Util.printError(e1);
            Util.logger.warning(
                    "Error al obtener clases: " + e1.getMessage());
            return new ApiResponse(7, DBManager.getDatabaseError(e1));
		}
	}
	
	public static ApiResponse eliminarClase(Request req) {
		boolean hardDelete = Util.safeToBoolean(req.queryParamOrDefault("trueDelete", "false"));
		int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numCat<=0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de departamento");
		}
		int numCla = Util.safeToInteger(req.queryParamOrDefault("numero_clase", "-1"));
		if(numCla<=0) {
			return new ApiResponse(2, "Falta el numero de clase");
		}
		if(hardDelete==false) {
			Util.logger.warning("Se realizara un softDelete para la clase "+numCla);
			String sql = "CALL desactivar_clase(?, ?, ?)";
			try (Connection conn = DBManager.getConnection();
			     CallableStatement cs = conn.prepareCall(sql)) {
			    cs.setInt(1, numCat);
			    cs.setInt(2, numDep);
			    cs.setInt(3, numCla);
			    cs.execute();

			    return new ApiResponse(0, "Clase desactivada correctamente");
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		} else {
			Util.logger.warning("Se eliminara la clase "+numCla);
			String sql = "CALL eliminar_clase(?, ?)";
		    try (Connection conn = DBManager.getConnection();
		        CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numDep);
			    cs.setInt(2, numCla);
	            cs.execute();

	            return new ApiResponse(0, "Se elimino la Clase "+numCla);
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		}
	}
}
