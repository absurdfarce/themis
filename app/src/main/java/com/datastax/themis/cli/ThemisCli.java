package com.datastax.themis.cli;

import com.datastax.themis.ThemisException;
import com.datastax.themis.cli.commands.InsertCommand;
import com.datastax.themis.cli.commands.QueryCommand;
import com.datastax.themis.cli.commands.SchemaCommand;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.datastax.themis.config.ConfigLoader;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

public class ThemisCli {

    private static Logger logger = LoggerFactory.getLogger(ThemisCli.class);

    public static final String THEMIS_CONFIG = ".themis.yaml";

    private static ImmutableMap<ClusterName, Cluster> loadConfig() {
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
        catch (ThemisException te) {
            logger.error("Exception processing YAML config", te);
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

        ImmutableMap<ClusterName, Cluster> clusters = loadConfig();

        CommandLine cli = new CommandLine(new ThemisRoot())
                .addSubcommand("query", new QueryCommand(clusters))
                .addSubcommand("insert", new InsertCommand(clusters))
                .addSubcommand("schema", new SchemaCommand(clusters));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }

    /* Top-level Callable for pioccli.  Doesn't really do much by itself */
    @CommandLine.Command(name="themis")
    static class ThemisRoot implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            return 0;
        }
    }
}
