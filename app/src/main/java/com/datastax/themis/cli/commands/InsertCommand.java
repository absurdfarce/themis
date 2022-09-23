package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.themis.Constants;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class InsertCommand implements Callable<Integer> {

    private static Logger logger = LoggerFactory.getLogger(InsertCommand.class);

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Execute the insertion against the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Execute the insertion against the target")
    boolean target;

    @CommandLine.Option(names = {"-p", "--proxy"}, description = "Execute the insertion against the proxy")
    boolean proxy;

    @CommandLine.Option(names = {"-c", "--count"}, description = "Number of records to insert", defaultValue = "10")
    int count;

    private final ImmutableMap<ClusterName, Cluster> clusters;

    /* Safe to re-use only because we're resetting values on the same columns each time and not alterting anything more substantive */
    private final InsertInto insertBuilder =
            QueryBuilder.insertInto(Constants.DEFAULT_KEYSPACE, Constants.DEFAULT_TABLE);

    private final Statement maxQueryStmt =
            QueryBuilder.selectFrom(Constants.DEFAULT_KEYSPACE, Constants.DEFAULT_TABLE)
            .function("max", Selector.column("key"))
            .build();

    public InsertCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        if (origin)
            insertIntoCluster(ClusterName.ORIGIN);
        if (target)
            insertIntoCluster(ClusterName.TARGET);
        if (proxy)
            insertIntoCluster(ClusterName.PROXY);
        return 0;
    }

    private void insertIntoCluster(ClusterName name) {

        System.out.println(String.format("Inserting %d new rows into cluster %s", this.count, name));

        try {

            ResultSet maxRs = this.clusters.get(name).getSession().execute(this.maxQueryStmt);
            int currentMax = maxRs.iterator().next().getInt(0);

            for (int i = currentMax + 1; i <= currentMax + this.count; ++i) {
                this.clusters.get(name).getSession().execute(
                        this.insertBuilder
                                .value("key", QueryBuilder.literal(i))
                                .value("value",QueryBuilder.literal("some text"))
                                .value("app", QueryBuilder.literal("themis"))
                                .build());
            }
        }
        catch (Exception e) {
            logger.error(String.format("Exception running insert command for cluster %s", name), e);
            System.out.println("Error inserting records, consult the log for details");
            System.exit(1);
        }
    }
}
