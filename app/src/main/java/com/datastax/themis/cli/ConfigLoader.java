package com.datastax.themis.cli;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * Load config information from a YAML file.
 * <br><br>
 * Config format requirements and validation operations should remain encapsulated within this class
 */
public class ConfigLoader {

    public static Map<?,?> load(InputStream in) {

        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(in);
        return data;
    }
}
