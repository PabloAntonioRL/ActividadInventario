package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Request;

public class FamiliasManager {
	
	public static ApiResponse insertFamilia(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de familia", null);
	    }
	    String nombre = (String) item.get("nombre_familia");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de familia", null);
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
	    
	    String sql = "CALL insertar_familia(?, ?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numCat);
	        	cs.setInt(2, numDep);
	        	cs.setInt(3, numCla);
	            cs.setString(4, nombre);
	            cs.registerOutParameter(5, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(5)) {
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
	public static ApiResponse updateFamilia(JSONObject item) throws SQLException {
	    if (item == null) {
	        return new ApiResponse(2, "Faltan datos de familia", null);
	    }
	    String nombre = (String) item.get("nombre_familia");
	    if (nombre == null || nombre.isBlank()) {
	        return new ApiResponse(2, "Falta nombre de familia", null);
	    }
	    int numCat = Util.safeToInteger(item.get("numero_categoria"));
	    if (numCat<=0) {
	        return new ApiResponse(2, "Falta nombre de categoria");
	    }
	    int numDep = Util.safeToInteger(item.get("numero_departamento"));
	    if (numDep == -1) {
	        return new ApiResponse(2, "Falta el numero de departamento", null);
	    }
	    int numCla = Util.safeToInteger(item.get("numero_clase"));
	    if (numCla == -1) {
	        return new ApiResponse(2, "Falta el numero de la clase", null);
	    }
	    int numFam = Util.safeToInteger(item.get("numero_familia"));
	    if (numFam == -1) {
	        return new ApiResponse(2, "Falta el numero de la clase", null);
	    }
	    if(item.get("activo")==null) {
	    	return new ApiResponse(2, "Falta parametro 'Activo'", null);
	    }
	    boolean activo = Util.safeToBoolean(item.get("activo"));
	    
	    String sql = "CALL actualizar_familia(?, ?, ?, ?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	        	cs.setInt(1, numCat);
	        	cs.setInt(2, numDep);
	        	cs.setInt(3, numCla);
	        	cs.setInt(4, numFam);
	            cs.setString(5, nombre);
	            cs.setBoolean(6, activo);
	            cs.registerOutParameter(7, Types.OTHER);
	            cs.execute();
	            
	            JSONObject resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(7)) {
	            	if (rs != null && rs.next())
	            		resultado = ResultSetToJsonConverter.toJson(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0,
                        "Ok. Familia actualizada correctamente",
                        resultado);
	        } catch (SQLException e) {
	            conn.rollback();
	            Util.printError(e);
	            Util.logger.warning(
	                    "Error al insertar familia: " + e.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	}
	

	public static ApiResponse getDataFromFamilias(Request req) {
		int numFam = Util.safeToInteger(req.queryParamOrDefault("numero_familia", "-1"));
		
		int numCla = Util.safeToInteger(req.queryParamOrDefault("numero_clase", "-1"));
		if(numCla<0) {
			return new ApiResponse(2, "Falta el numero de clase");
		}
		int numDep = Util.safeToInteger(req.queryParamOrDefault("numero_departamento", "-1"));
		if(numDep<0) {
			return new ApiResponse(2, "Falta el numero de departamento");
		}
		int numCat = Util.safeToInteger(req.queryParamOrDefault("numero_categoria", "-1"));
		if(numCat<=0) {
			return new ApiResponse(2, "Falta el numero de categoria");
		}
		String sql = "CALL obtener_familias(?, ?, ?, ?, ?)";

	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numCat);
	            cs.setInt(2, numDep);
	            cs.setInt(3, numCla);
	            cs.setInt(4, numFam);
	            cs.registerOutParameter(5, Types.OTHER);
	            cs.execute();
	            
	            JSONArray resultado = null;
	            try (ResultSet rs = (ResultSet) cs.getObject(5)) {
	            	if (rs != null)
	            		resultado = ResultSetToJsonConverter.toArray(rs);
	            }

	            conn.commit();
	            return new ApiResponse(0, "Ok, se encontraron "+resultado.size()+" familias", resultado);
	        } 
	    } catch (SQLException e1) {
            Util.printError(e1);
            Util.logger.warning(
                    "Error al obtener familias: " + e1.getMessage());
            return new ApiResponse(7, DBManager.getDatabaseError(e1));
		}
	}
	
	public static ApiResponse eliminarFamilia(Request req) {
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
	    int numFam = Util.safeToInteger(req.queryParamOrDefault("numero_familia", "-1"));
	    if (numFam <= -1) {
	        return new ApiResponse(2, "Falta el numero de la familia", null);
	    }
		if(hardDelete==false) {
			Util.logger.warning("Se realizara un softDelete para la familia "+numFam);
			String sql = "CALL desactivar_familia(?, ?, ?, ?)";
			try (Connection conn = DBManager.getConnection();
			     CallableStatement cs = conn.prepareCall(sql)) {
			    cs.setInt(1, numCat);
			    cs.setInt(2, numDep);
			    cs.setInt(3, numCla);
			    cs.setInt(4, numFam);
			    cs.execute();

			    return new ApiResponse(0, "Familia desactivada correctamente");
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		} else {
			Util.logger.warning("Se eliminara la familia "+numDep);
			String sql = "CALL eliminar_familia(?, ?, ?)";
		    try (Connection conn = DBManager.getConnection();
		        CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, numDep);
			    cs.setInt(2, numCla);
			    cs.setInt(3, numFam);
	            cs.execute();

	            return new ApiResponse(0, "Se elimino la familia "+numFam);
		    } catch (SQLException e1) {
	            Util.printError(e1);
	            Util.logger.warning(
	                    "Error: " + e1.getMessage());
	            return new ApiResponse(7, DBManager.getDatabaseError(e1));
			}
		}
	}
	
}
