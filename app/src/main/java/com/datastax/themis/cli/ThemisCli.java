package com.datastax.themis.cli;

import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.cluster.ClusterName;
import com.datastax.themis.config.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class ThemisCli {

    private static Logger logger = LoggerFactory.getLogger(com.datastax.themis.cli.ThemisCli.class);

    public static final String THEMIS_CONFIG = ".themis.yaml";

    private static Map<ClusterName, Cluster> loadConfig() {
        File configFile = new File(System.getProperty("user.home"), THEMIS_CONFIG);
        if (! configFile.exists()) {
            System.out.println(String.format("Expected Themis config %s doesn't exist", configFile.getAbsolutePath()));
            System.exit(1);
        }
        if (! configFile.isFile()) {
            System.out.println(String.format("Expected Themis config %s exists but isn't a file", configFile.getAbsolutePath()));
            System.exit(1);
        }
        if (! configFile.canRead()) {
            System.out.println(String.format("Expected Themis config %s exists but isn't readable", configFile.getAbsolutePath()));
            System.exit(1);
        }

        try {
            return ConfigLoader.load(new FileInputStream(configFile));
        }
        catch (ConfigException ce) {
            logger.error("Exception processing YAML config", ce);
            System.out.println("YAML config could not be parsed, consult the log for details");
            System.exit(1);
        }
        // We already checked for this above but... you know... type system...
        catch (FileNotFoundException fnfe) {
            System.out.println(String.format("VERY unexpected error", fnfe));
            System.exit(2);
        }

        /* Here to make IDEs happy */
        return null;
    }

    public static void main(String[] args) {

        Map<?,?> config = loadConfig();
        System.out.println(String.format("Loaded config file: %s", config));
    }
}
