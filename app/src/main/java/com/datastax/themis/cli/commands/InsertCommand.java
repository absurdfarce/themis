package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.themis.Constants;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Random;
import java.util.concurrent.Callable;

@CommandLine.Command()
public class InsertCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(InsertCommand.class);

    /* TODO: Could be made configurable if desired */
    private static final int RANDOM_STRING_SIZE = 12;

    private Random random = new Random(System.currentTimeMillis());

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
                    .where(Relation.column("app").isEqualTo(QueryBuilder.literal("themis")))
            .build();

    public InsertCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        boolean success = true;
        if (origin)
            success = success & insertIntoCluster(ClusterName.ORIGIN);
        if (target)
            success = success & insertIntoCluster(ClusterName.TARGET);
        if (proxy)
            success = success & insertIntoCluster(ClusterName.PROXY);
        return success ? 0 : 1;
    }

    private String buildRandomString(int length) {

        /* Printable ASCII chars run from 33 through 126 (decimal) */
        return this.random.ints(length, 33, 127)
                .collect(
                        StringBuilder::new,
                        (sb,cp) -> sb.appendCodePoint(cp),
                        StringBuilder::append)
                .toString();
    }

    private boolean insertIntoCluster(ClusterName name) {

        System.out.println(String.format("Inserting %d new rows into cluster %s", this.count, name));

        try {

            ResultSet maxRs = this.clusters.get(name).getSession().execute(this.maxQueryStmt);
            int currentMax = maxRs.iterator().next().getInt(0);

            for (int i = currentMax + 1; i <= currentMax + this.count; ++i) {
                this.clusters.get(name).getSession().execute(
                        this.insertBuilder
                                .value("key", QueryBuilder.literal(i))
                                .value("value",QueryBuilder.literal(buildRandomString(RANDOM_STRING_SIZE)))
                                .value("app", QueryBuilder.literal("themis"))
                                .build());
            }

            return true;
        }
        catch (Exception e) {
            logger.error(String.format("Exception running insert command for cluster %s", name), e);
            System.out.println("Error inserting records, consult the log for details");
            return false;
        }
    }
}
