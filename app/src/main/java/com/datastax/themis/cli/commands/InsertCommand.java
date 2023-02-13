package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Function;

@CommandLine.Command()
public class InsertCommand extends AbstractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(InsertCommand.class);

    /* TODO: Could be made configurable if desired */
    private static final int RANDOM_STRING_SIZE = 12;

    private Random random = new Random(System.currentTimeMillis());

    @CommandLine.Option(names = {"-p", "--proxy"}, description = "Execute the insertion against the proxy")
    boolean proxy;

    @CommandLine.Option(names = {"-c", "--count"}, description = "Number of records to insert", defaultValue = "10")
    int count;

    private final ImmutableMap<ClusterName, Cluster> clusters;

    public InsertCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        /* Safe to re-use only because we're resetting values on the same columns each time and not altering anything more substantive */
        InsertInto insertBuilder =
                QueryBuilder.insertInto(this.keyspace, this.table);

        Statement maxQueryStmt =
                QueryBuilder.selectFrom(this.keyspace, this.table)
                        .function("max", Selector.column("key"))
                        .where(Relation.column("app").isEqualTo(QueryBuilder.literal("themis")))
                        .build();

        /* My kingdom for real partial application */
        Function<ClusterName, Boolean> insertFn = (ClusterName clusterName) -> {
            return insertIntoCluster(clusterName, insertBuilder, maxQueryStmt);
        };

        boolean success = true;
        if (origin)
            success = success & insertFn.apply(ClusterName.ORIGIN);
        if (target)
            success = success & insertFn.apply(ClusterName.TARGET);
        if (proxy)
            success = success & insertFn.apply(ClusterName.PROXY);
        return success ? 0 : 1;
    }

    private boolean insertIntoCluster(ClusterName name, InsertInto insertBuilder, Statement maxQueryStmt) {

        System.out.println(String.format("Inserting %d new rows into cluster %s", this.count, name));

        try {

            ResultSet maxRs = this.clusters.get(name).getSession().execute(maxQueryStmt);
            int currentMax = maxRs.iterator().next().getInt(0);

            for (int i = currentMax + 1; i <= currentMax + this.count; ++i) {
                this.clusters.get(name).getSession().execute(
                        insertBuilder
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

    private String buildRandomString(int length) {

        /* Printable ASCII chars run from 33 through 126 (decimal) */
        return this.random.ints(length, 33, 127)
                .collect(
                        StringBuilder::new,
                        (sb,cp) -> sb.appendCodePoint(cp),
                        StringBuilder::append)
                .toString();
    }
}
