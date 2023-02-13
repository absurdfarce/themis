package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.function.Function;

@CommandLine.Command()
public class QueryCommand extends AbstractCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(QueryCommand.class);

    @CommandLine.Option(names = {"-p", "--proxy"}, description = "Execute the query against the proxy")
    boolean proxy;

    @CommandLine.Option(names = {"-l", "--limit"}, description = "Limit the number of rows returned", defaultValue = "10")
    int limit;

    private final ImmutableMap<ClusterName, Cluster> clusters;

    public QueryCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {

        Statement insertStmt = QueryBuilder.selectFrom(this.keyspace, this.table)
                .all()
                .where(Relation.column("app").isEqualTo(QueryBuilder.literal("themis")))
                .orderBy("key", ClusteringOrder.DESC)
                .limit(this.limit)
                .build();

        Function<ClusterName, Boolean> queryFn = (ClusterName clusterName) -> {
            return queryCluster(clusterName, insertStmt);
        };

        boolean success = true;
        if (origin)
            success = success & queryFn.apply(ClusterName.ORIGIN);
        if (target)
            success = success & queryFn.apply(ClusterName.TARGET);
        if (proxy)
            success = success & queryFn.apply(ClusterName.PROXY);
        return success ? 0 : 1;
    }

    private boolean queryCluster(ClusterName name, Statement insertStmt) {

        System.out.println(String.format("Querying cluster %s", name));
        try {

            for (Row row : this.clusters.get(name).getSession().execute(insertStmt)) {
                System.out.println(String.format("%d => %s", row.getInt("key"), row.getString("value")));
            }

            return true;
        }
        catch (Exception e) {
            logger.error(String.format("Exception running query command for cluster %s", name), e);
            System.out.println("Error executing query, consult the log for details");
            return false;
        }
    }
}
