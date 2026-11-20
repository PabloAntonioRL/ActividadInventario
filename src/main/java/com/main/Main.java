package com.main;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.delete;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import spark.Spark;
import spark.servlet.SparkApplication;
/*
 * Creacion de Endpoints
 * Codigos de respuesta
	0: Consulta completada sin ningún error
	1: Sin autorización o sesión expirada
	2: Datos incorrectos en la consulta
	3: Datos incompletos o faltantes
	4: No se encontró información para esta consulta
	5: Error inesperado
	6: Configuracion incompleta
	7: Error al conectar con base de datos

 * @param c
 */

public class Main implements SparkApplication{
	public final static String version = "0.0.2", serverName = "Coppel";
	
	public void init() {
		// TODO Auto-generated method stub
		Util.startLog(serverName);
		startServer();
	}

	public static void main(String[] args) {
		new Main().init();
	}
	
	@SuppressWarnings("unchecked")
	public static void startServer() {
		Util.logger.info("Iniciando server "+version+"...");
        
        Util.logger.warning("Habilitando servidor...");
        port((int) 8081);
        
        
        String externalUrl = Util.getWebappUrl();
        Util.logger.config("Cargando webapp...");
        if(FileUtil.checkIfFolderExist(externalUrl))
        	try {
        		Spark.externalStaticFileLocation(externalUrl);
        	} catch(Exception e) {
        		Util.printError(e);
        	}
        else {
        	Util.logger.warning("No existe la carpeta para cargar proyectos web");
        }
     // evitar problema con CORS
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });

        before((req, response) -> {
        	response.header("Access-Control-Allow-Origin", "*");
        });
        exception(Exception.class, (e, req, res) -> {

            req.attribute("exception", e);
            Util.printError(e);

        });
        
        get("/check", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		JSONObject ver = new JSONObject();
    		ver.put("version", version);
    		ApiResponse response = new ApiResponse(0, "Autorizacion correcta", ver);
    		return response.toString();
        	
        });
        get("/getData", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
    			ApiResponse response = DBManager.getDataFrom(req);
    			if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
    		} catch(Exception e) {
    			res.status(400);
        		Util.printError(e);
    			ApiResponse responseError = new ApiResponse(e);
        		return responseError.toString();
    		}
        });
        get("/reactivateElement", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
    			ApiResponse response = DBManager.reactivarElemento(req);
    			if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
    		} catch(Exception e) {
    			res.status(400);
        		Util.printError(e);
    			ApiResponse responseError = new ApiResponse(e);
        		return responseError.toString();
    		}
        });
        get("/getDataInactive", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
    			ApiResponse response = DBManager.getAllInactive(req);
    			if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
    		} catch(Exception e) {
    			res.status(400);
        		Util.printError(e);
    			ApiResponse responseError = new ApiResponse(e);
        		return responseError.toString();
    		}
        });
        
        get("/getItem", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		int sku = Util.safeToInteger(req.queryParamOrDefault("sku", "-1"));
    		if(sku==-1) {
    			ApiResponse response = new ApiResponse(3, "SKU invalido");
        		return response.toString();
    		}
    		try {
    			ApiResponse response = InventarioManager.getItemsBySku(req);
    			if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
    		} catch(Exception e) {
    			res.status(400);
        		Util.printError(e);
    			ApiResponse responseError = new ApiResponse(e);
        		return responseError.toString();
    		}
        });
        // ============================================================
        post("/newCategory", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = CategoriaManager.insertCategoria(item);
	    		//ApiResponse response = new ApiResponse(0, "Ok", item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        post("/updateCategory", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = CategoriaManager.updateCategoria(item);
	    		//ApiResponse response = new ApiResponse(0, "Ok", item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        delete("/deleteCategory", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
	    		ApiResponse response = CategoriaManager.eliminarCategoria(req);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        // ============================================================

        post("/newDepartment", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = DepartamentosManager.insertDepartamento(item);
	    		//ApiResponse response = new ApiResponse(0, "Ok", item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        post("/updateDepartment", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = DepartamentosManager.updateDepartamento(item);
	    		//ApiResponse response = new ApiResponse(0, "Ok", item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        delete("/deleteDepartment", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
	    		ApiResponse response = DepartamentosManager.eliminarDepartamento(req);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        
        // ===============================================
        post("/newClass", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = ClasesManager.insertClase(item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        post("/updateClass", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = ClasesManager.updateClase(item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        delete("/deleteClass", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
	    		ApiResponse response = ClasesManager.eliminarClase(req);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        // ==============================================================
        post("/newFamily", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = FamiliasManager.insertFamilia(item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        post("/updateFamily", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject item = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = FamiliasManager.updateFamilia(item);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        delete("/deleteFamily", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
	    		ApiResponse response = FamiliasManager.eliminarFamilia(req);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        
        /*
         * ===========================================================
         */

        post("/newItem", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONArray items = JSONUtil.getArray(body, "data");
	    		
	    		ApiResponse response = InventarioManager.insertItemsBatch(items);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        	
        });
        post("/updateItem", (req, res) -> {
        	res.type("application/json");
        	JSONObject body = JSONUtil.StringToJSON(req.body());
        	if(body==null) {
        		res.status(400);
        		ApiResponse response = new ApiResponse(2, "Consulta invalida o cuerpo vacio.");
        		return response.toString();
        	}
        	try {
	    		res.status(200);
	    		JSONObject items = JSONUtil.getJSON(body, "data");
	    		
	    		ApiResponse response = InventarioManager.updateItem(items);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        
        delete("/deleteItem", (req, res) -> {
        	res.type("application/json");
    		res.status(200);
    		try {
	    		ApiResponse response = InventarioManager.eliminarArticulo(req);
	    		if(response.code!=0) {
	    			res.status(400);
	    		}
	    		Util.logger.fine(response.message);
	    		return response.toString();
        	} catch(Exception e) {
        		res.status(400);
        		Util.printError(e);
        		return new ApiResponse(e).toString();
        	}
        });
        
	}
	
}
