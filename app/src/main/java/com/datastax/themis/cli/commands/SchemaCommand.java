package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.function.Function;

@CommandLine.Command()
public class SchemaCommand extends AbstractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(SchemaCommand.class);

    private final ImmutableMap<ClusterName, Cluster> clusters;

    public SchemaCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        Statement keyspaceCreateStmt =
                SchemaBuilder.createKeyspace(this.keyspace)
                        .ifNotExists()
                        .withSimpleStrategy(1)
                        .build();

        Statement tableCreateStmt =
                SchemaBuilder.createTable(this.keyspace, this.table)
                        .ifNotExists()
                        .withPartitionKey("app", DataTypes.TEXT)
                        .withClusteringColumn("key", DataTypes.INT)
                        .withColumn("value", DataTypes.TEXT)
                        .build();

        Function<ClusterName, Boolean> schemaFn = (ClusterName clusterName) -> {
            return createSchemaOnCluster(clusterName, keyspaceCreateStmt, tableCreateStmt);
        };

        boolean success = true;
        if (origin)
            success = success & schemaFn.apply(ClusterName.ORIGIN);
        if (target)
            success = success & schemaFn.apply(ClusterName.TARGET);
        return success ? 0 : 1;
    }

    private boolean createSchemaOnCluster(ClusterName name, Statement keyspaceCreateStmt, Statement tableCreateStmt) {

        System.out.println(String.format("Creating schema on cluster %s", name));

        try {

            Cluster cluster = this.clusters.get(name);
            if (cluster.isAstra())
                System.out.println(String.format("Cluster %s is an Astra cluster, skipping keyspace creation (Astra keyspaces must be created through the Astra UI)", name));
            else
                cluster.getSession().execute(keyspaceCreateStmt);

            cluster.getSession().execute(tableCreateStmt);

            return true;
        }
        catch (Exception e) {
            logger.error(String.format("Exception creating schema on cluster %s", name), e);
            System.out.println("Error creating schema, consult the log for details");
            return false;
        }
    }
}
