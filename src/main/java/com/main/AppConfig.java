package com.main;


import java.io.File;

import javax.servlet.ServletContext;

public class AppConfig {
    public static File storageDir;

    public static void init(ServletContext ctx) {
        // lee el context-param o System.getProperty
        String base = ctx.getInitParameter("storage.location");
        storageDir = new File(base);
        if (!storageDir.exists()) storageDir.mkdirs();
    }
}
