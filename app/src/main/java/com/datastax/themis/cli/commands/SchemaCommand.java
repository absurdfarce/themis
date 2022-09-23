package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.themis.Constants;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class SchemaCommand implements Callable<Integer> {

    private static Logger logger = LoggerFactory.getLogger(SchemaCommand.class);

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Create the schema on the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Create the schema on the target")
    boolean target;

    private final ImmutableMap<ClusterName, Cluster> clusters;

    private final Statement keyspaceCreateStmt =
            SchemaBuilder.createKeyspace(Constants.DEFAULT_KEYSPACE)
                    .ifNotExists()
                    .withSimpleStrategy(1)
                    .build();

    //private final Statement tableCreateStmt =
    //        SchemaBuilder.createTable(Constants.DEFAULT_KEYSPACE, Constants.DEFAULT_TABLE)
    //                .ifNotExists()

    public SchemaCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        if (origin)
            createSchemaOnCluster(ClusterName.ORIGIN);
        if (target)
            createSchemaOnCluster(ClusterName.TARGET);
        return 0;
    }

    private void createSchemaOnCluster(ClusterName name) {

        System.out.println(String.format("Creating schema on cluster %s", name));

        try {

            Cluster cluster = this.clusters.get(name);
            if (cluster.isAstra())
                System.out.print(String.format("Cluster %s is an Astra cluster, skipping keyspace creation (Astra keyspaces must be created through the Astra UI)"));
            else
                cluster.getSession().execute(keyspaceCreateStmt);

        }
        catch (Exception e) {
            logger.error(String.format("Exception creating schema on cluster %s", name), e);
            System.out.println("Error creating schema, consult the log for details");
            System.exit(1);
        }
    }
}
