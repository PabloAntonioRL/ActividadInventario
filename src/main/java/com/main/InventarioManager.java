package com.main;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Request;

public class InventarioManager {
	
	
	
	public static ApiResponse insertItemsBatch(JSONArray datos) throws SQLException {
	    if (datos == null || datos.isEmpty()) {
	        return new ApiResponse(2, "Error: No se recibieron artículos para insertar.");
	    }
	    Util.logger.fine("Agregando " + datos.size() + " filas en DB inventario");
	    // Validar SKU repetidos dentro del lote
	    Set<Integer> skus = new HashSet<>();
	    for (int i = 0; i < datos.size(); i++) {
	        JSONObject item = (JSONObject) datos.get(i);
	        int sku = Util.safeToInteger(item.get("sku"));

	        if (!skus.add(sku)) {
	            return new ApiResponse(4, "Error: El SKU " + sku + " está repetido dentro del lote.");
	        }
	    }

	    String sql = "CALL insertar_articulos_batch(?::jsonb)";
	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setString(1, datos.toJSONString());
	            ps.execute();
	            conn.commit();
	            Util.logger.fine("Se insertaron " + datos.size() + " artículos.");
	            return new ApiResponse(0, "Ok", datos);
	        } catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	}


	public static ApiResponse updateItem(JSONObject item) throws SQLException {
		if(item==null) {
			return new ApiResponse(2, "Error: Falta la informacion del articulo");
		}
		if(item.get("sku")==null) {
			return new ApiResponse(4, "Error: Falta el SKU del articulo");
		}
	    String sql = "CALL actualizar_articulo(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            setItemParams(ps, item, true);
	            ps.execute();
	            conn.commit();
	            Util.logger.fine("Artículo actualizado. SKU: " + item.get("sku"));
	            return new ApiResponse(0, "Ok", item);
	        } catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            return new ApiResponse(7, DBManager.getDatabaseError(e));
	        }
	    }
	}
	
	public static ApiResponse getItemsBySku(Request req) throws SQLException {
		int sku = Util.safeToInteger(req.queryParamOrDefault("sku", "-1"));
		if(sku<100000) {
			return new ApiResponse(2, "Falta SKU del ariculo");
		}
	    String sql = "CALL obtener_articulo(?, ?)";
	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, sku);
	            cs.registerOutParameter(2, Types.OTHER);
	            cs.execute();
	            try (ResultSet rs = (ResultSet) cs.getObject(2)) {
	                JSONArray resultado = ResultSetToJsonConverter.toArray(rs);
	                conn.commit();
	                return new ApiResponse(0, "Ok", resultado);
	            }
	        } catch (SQLException | RuntimeException e) {
	            conn.rollback();
	            Util.printError(e);
	            throw e;
	        }
	    }catch (SQLException | RuntimeException e) {
            return new ApiResponse(7, DBManager.getDatabaseError(e));
        }
	}
	

	private static void setItemParams(PreparedStatement ps, JSONObject item, boolean isUpdate) throws SQLException {
	    int idx = 1;
	    ps.setString(idx++, (String) item.get("articulo"));
	    ps.setString(idx++, (String) item.get("marca"));
	    ps.setString(idx++, (String) item.get("modelo"));
	    ps.setInt(idx++, Util.safeToInteger(item.get("stock")));
	    ps.setInt(idx++, Util.safeToInteger(item.get("cantidad")));
	    ps.setInt(idx++, Util.safeToInteger(item.get("descontinuado")));

	    ps.setInt(idx++, Util.safeToInteger(item.get("numero_categoria")));
	    ps.setInt(idx++, Util.safeToInteger(item.get("numero_departamento")));
	    ps.setInt(idx++, Util.safeToInteger(item.get("numero_clase")));
	    ps.setInt(idx++, Util.safeToInteger(item.get("numero_familia")));
	
	    ps.setInt(idx, Util.safeToInteger(item.get("sku")));
	}

	public static ApiResponse eliminarArticulo(Request req) {
		int sku = Util.safeToInteger(req.queryParamOrDefault("sku", "-1"));
		if(sku<100000) {
			return new ApiResponse(2, "Falta SKU del ariculo");
		}
		String sql = "CALL eliminar_articulo(?)";
	    try (Connection conn = DBManager.getConnection()) {
	        conn.setAutoCommit(false);
	        try (CallableStatement cs = conn.prepareCall(sql)) {
	            cs.setInt(1, sku);
	            cs.execute();
                conn.commit();
                return new ApiResponse(0, "Ok, registro "+sku+" eliminado correctamente.");
	        }  catch (SQLException e) {
	            conn.rollback();
	            throw e;
	        }
   	    } catch (SQLException e) {
			Util.printError(e);
			return new ApiResponse(7, DBManager.getDatabaseError(e));
		}
	}
}
