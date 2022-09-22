package com.datastax.themis.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class ThemisCli {

    public static final String THEMIS_CONFIG = ".themis.yaml";

    private static Map<?,?> loadConfig() {
        File configFile = new File(System.getProperty("user.home"), THEMIS_CONFIG);
        if (! configFile.exists()) {
            throw new RuntimeException(String.format("Expected Themis config %s doesn't exist", configFile.getAbsolutePath()));
        }
        if (! configFile.isFile()) {
            throw new RuntimeException(String.format("Expected Themis config %s exists but isn't a file", configFile.getAbsolutePath()));
        }
        if (! configFile.canRead()) {
            throw new RuntimeException(String.format("Expected Themis config %s exists but isn't readable", configFile.getAbsolutePath()));
        }
        try {
            return ConfigLoader.load(new FileInputStream(configFile));
        }
        // We already checked for this above but... you know... type system...
        catch (FileNotFoundException fnfe) {
            throw new RuntimeException("Wow, that was weird", fnfe);
        }
    }

    public static void main(String[] args) {

        Map<?,?> config = loadConfig();
        System.out.println(String.format("Loaded config file: %s", config));
    }
}
