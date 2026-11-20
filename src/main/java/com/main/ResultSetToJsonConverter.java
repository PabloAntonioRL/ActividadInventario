package com.main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.*;

public class ResultSetToJsonConverter {


    @SuppressWarnings("unchecked")
	public static JSONObject toGeoJson(ResultSet rs) throws SQLException {
        JSONArray features = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject properties = new JSONObject();
            JSONObject geometry = null;

            for (int i = 1; i <= columnCount; i++) {
            	try {
	                String columnName = metaData.getColumnLabel(i);
	                int columnType = metaData.getColumnType(i);
	
	                // La columna "geom" debe contener el GeoJSON (desde ST_AsGeoJSON)
	                if ("geom".equalsIgnoreCase(columnName)) {
	                    String geomJson = rs.getString(i);
	                    if (geomJson != null) {
	                        geometry = org.json.simple.JSONValue.parse(geomJson) instanceof JSONObject
	                                ? (JSONObject) org.json.simple.JSONValue.parse(geomJson)
	                                : null;
	                    } else {
	                    	geometry = new JSONObject();
	                    	geometry.put("type", "Point");
	                    	float[] coord = {0,0};
	                    	geometry.put("coordinates", coord);
	                    }
	                } else {
	                    Object value;
	
	                    switch (columnType) {
	                        case Types.TIMESTAMP:
	                        case Types.DATE:
	                        case Types.TIME:
	                            Timestamp ts = rs.getTimestamp(i);
	                            value = ts != null ? ts.toString() : null;
	                            break;
	                        default:
	                            value = rs.getObject(i);
	                    }
	
	                    properties.put(columnName, value);
	                }
            	} catch(Exception e) {
            		Util.printError(e);
            	}
            }

            feature.put("geometry", geometry);
            feature.put("properties", properties);

            features.add(feature);
        }

        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("features", features);

        return featureCollection;
    }
    
    @SuppressWarnings("unchecked")
	public static JSONArray toGeoJsonArray(ResultSet rs) throws SQLException {
        JSONArray features = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            JSONObject feature = new JSONObject();
            feature.put("type", "Feature");

            JSONObject properties = new JSONObject();
            JSONObject geometry = null;

            for (int i = 1; i <= columnCount; i++) {
            	try {
	                String columnName = metaData.getColumnLabel(i);
	                int columnType = metaData.getColumnType(i);
	
	                // La columna "geom" debe contener el GeoJSON (desde ST_AsGeoJSON)
	                if ("geom".equalsIgnoreCase(columnName)||"geom_geojson".equalsIgnoreCase(columnName)) {
	                    String geomJson = rs.getString(i);
	                    if (geomJson != null && geometry==null) {
	                        geometry = org.json.simple.JSONValue.parse(geomJson) instanceof JSONObject
	                                ? (JSONObject) org.json.simple.JSONValue.parse(geomJson)
	                                : null;
	                    } 
	                } else {
	                    Object value;
	
	                    switch (columnType) {
	                        case Types.TIMESTAMP:
	                        case Types.DATE:
	                        case Types.TIME:
	                            Timestamp ts = rs.getTimestamp(i);
	                            value = ts != null ? ts.toString() : null;
	                            break;
	                        default:
	                            value = rs.getObject(i);
	                    }
	
	                    properties.put(columnName, value);
	                }
            	} catch(Exception e) {
            		Util.printError(e);
            	}
            }
            if(geometry==null) {
            	geometry = new JSONObject();
            	geometry.put("type", "Point");
            	float[] coord = {0,0};
            	geometry.put("coordinates", coord);
            }
            feature.put("geometry", geometry);
            feature.put("properties", properties);

            features.add(feature);
        }

        return features;
    }
	
	@SuppressWarnings("unchecked")
	public static JSONArray toArray(ResultSet rs) throws SQLException {
        JSONArray jsonArray = new JSONArray();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rs.next()) {
            JSONObject obj = new JSONObject();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                int columnType = metaData.getColumnType(i);

                Object value;

                switch (columnType) {
                    case Types.VARCHAR:
                    case Types.CHAR:
                    case Types.LONGVARCHAR:
                    case Types.NVARCHAR:
                    case Types.LONGNVARCHAR:
                    case Types.CLOB:
                        value = rs.getString(i);
                        break;

                    case Types.INTEGER:
                    case Types.TINYINT:
                    case Types.SMALLINT:
                    case Types.BIGINT:
                        value = rs.getObject(i);
                        break;

                    case Types.FLOAT:
                    case Types.REAL:
                    case Types.DOUBLE:
                    case Types.NUMERIC:
                    case Types.DECIMAL:
                        value = rs.getObject(i);
                        break;

                    case Types.BOOLEAN:
                    case Types.BIT:
                        value = rs.getBoolean(i);
                        break;

                    case Types.TIMESTAMP:
                    case Types.TIMESTAMP_WITH_TIMEZONE:
                    case Types.DATE:
                    case Types.TIME:
                        Timestamp ts = rs.getTimestamp(i);
                        value = (ts != null) ? ts.toString() : null;
                        break;

                    default:
                        value = rs.getObject(i); // fallback
                }

                obj.put(columnName, value);
            }

            jsonArray.add(obj);
        }

        return jsonArray;
    }
	
    @SuppressWarnings("unchecked")
	public static JSONObject toJson(ResultSet rs) throws SQLException {
        JSONObject json = new JSONObject();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i); 
            int columnType = metaData.getColumnType(i);

            Object value;

            switch (columnType) {
                case Types.VARCHAR:
                case Types.CHAR:
                case Types.LONGVARCHAR:
                case Types.NVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.CLOB:
                    value = rs.getString(i);
                    break;

                case Types.INTEGER:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.BIGINT:
                    value = rs.getObject(i); // número como Integer o Long
                    break;

                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                case Types.NUMERIC:
                case Types.DECIMAL:
                    value = rs.getObject(i); // número decimal
                    break;

                case Types.BOOLEAN:
                case Types.BIT:
                    value = rs.getBoolean(i);
                    break;

                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                case Types.DATE:
                case Types.TIME:
                    Timestamp ts = rs.getTimestamp(i);
                    value = (ts != null) ? ts.toString() : null;
                    break;

                case Types.NULL:
                    value = null;
                    break;

                default:
                    value = rs.getObject(i); // fallback para otros tipos (por ejemplo GEOMETRY como texto)
            }

            json.put(columnName, value);
        }

        return json;
    }
}
