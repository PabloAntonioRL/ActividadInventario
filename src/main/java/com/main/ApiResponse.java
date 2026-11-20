package com.main;


import org.json.simple.JSONObject;
/**
 * 
 * Codigos de respuesta
	0: Consulta completada sin ningún error
	1: Sin autorización o sesión expirada
	2: Datos incorrectos en la consulta
	3: Datos incompletos o faltantes
	4: No se encontró información para esta consulta
	5: Error inesperado
	6: Configuracion incompleta
	7: Error al conectar con base de datos

 * @author anto_
 *
 */
public class ApiResponse {

	public int code;
	public String message;
	private Object data;
	
	public ApiResponse() {
        this.code = 0;
        this.message = "Ok";
        this.data = null;
    }
    public ApiResponse(Exception e) {
        this.code = 5;
        this.message = e.getMessage();
        this.data = null;
    }
    public ApiResponse(int code) {
        this.code = code;
        switch(this.code) {
        case 0: this.message = "Ok"; break;
        case 1: this.message = "Sin autorización, permisos o sesión expirada"; break;
        case 2: this.message = "Datos incorrectos en la consulta"; break;
        case 3: this.message = "Datos incompletos o faltantes"; break;
        case 4: this.message = "No se encontró información para esta consulta"; break;
        case 5: this.message = "Error inesperado"; break;
        case 6: this.message = "Configuracion incompleta"; break;
        case 7: this.message = "Error al conectar con base de datos"; break;
        }
        this.data = null;
    }
    public ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
        this.data = null;
    }
    public ApiResponse(int code, Exception e) {
        this.code = code;
        this.message = e.getMessage();
        this.data = null;
    }
    public ApiResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
    
    public int getCode() { return this.code; }
    
    @SuppressWarnings("unchecked")
	public JSONObject getDataJSON() {
    	if(this.data instanceof JSONObject) {
    		return (JSONObject) this.data;
    	} else if(this.data instanceof String) {
    		JSONObject x = JSONUtil.StringToJSON((String) this.data);
    		if(x!=null) return x;
    		x = new JSONObject();
    		x.put("value", this.data);
    		return x;
    	}
    	return null;
    }

    @SuppressWarnings("unchecked")
	public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", message);
        json.put("data", this.getDataJSON()==null? data: this.getDataJSON());
        return json;
    }
    
    public String toString() {
    	return JSONUtil.JSONToString(this.toJson());
    }
	
}
