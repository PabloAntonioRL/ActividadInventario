package com.main;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//import com.main.system.SystemHistoryScheduler;


public class ConfigListener implements ServletContextListener {
	private ScheduledExecutorService scheduler;
	
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	ServletContext ctx = sce.getServletContext();

        // 1) Obtén la carpeta raíz de la webapp ya expandida
        String webappRoot = ctx.getRealPath("/");  
        // ej: C:\…\Tomcat9\webapps\ApiRest\

        // 2) Define ahí tu app-data
        File externalDir = new File(webappRoot, "app-data");
        if (!externalDir.exists()) {
            externalDir.mkdirs();
            // Copia aquí los datos por defecto
            copyRecursively(ctx, "/WEB-INF/classes/data/", externalDir);
        }

        // guarda esta carpeta en un holder si la necesitas en otras clases
        AppConfig.storageDir = externalDir;
        
        Util.startLog();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // calcula el tiempo hasta la próxima medianoche UTC
        long initialDelay = Duration.between(
                Instant.now(),
                LocalDate.now(ZoneOffset.UTC).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
        ).toMillis();
        
        Util.logger.info("Logger programado: "+Util.formatMillis(initialDelay));
        // programa la rotación diaria exacta
        scheduler.scheduleAtFixedRate(Util::checkLogDate, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    
    }

    @Override public void contextDestroyed(ServletContextEvent sce) {
    	try {
    		Util.logger.warning("Deteniendo Contexto");
    		//SystemHistoryScheduler.stop();
            if (scheduler != null) {
                scheduler.shutdownNow();
            }
            Util.terminateLog();
        } catch (Exception ignored) {}
    }

    private void copyRecursively(ServletContext ctx, String pathInWar, File destDir) {
        Set<String> paths = ctx.getResourcePaths(pathInWar);
        if (paths == null) return;
        for (String entry : paths) {
            if (entry.endsWith("/")) {
                // es carpeta: crea la subcarpeta y desciende
                String folderName = entry.substring(pathInWar.length(), entry.length()-1);
                File sub = new File(destDir, folderName);
                if (!sub.exists()) sub.mkdirs();
                copyRecursively(ctx, entry, sub);
            } else {
                // es archivo: cópialo
                String fileName = entry.substring(pathInWar.length());
                File out = new File(destDir, fileName);
                try (InputStream is = ctx.getResourceAsStream(entry)) {
                    if (is == null) {
                        ctx.log("No se encontró recurso: " + entry);
                        continue;
                    }
                    Files.copy(is, out.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    ctx.log("Copiado recurso: " + entry + " → " + out.getAbsolutePath());
                } catch (IOException e) {
                    ctx.log("Error copiando " + entry, e);
                }
            }
        }
        
        
    }
}
