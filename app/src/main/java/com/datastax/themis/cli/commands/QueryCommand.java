package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class QueryCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Execute the query against the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Execute the query against the target")
    boolean target;

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

        Statement insertStmt = QueryBuilder.selectFrom(CqlIdentifier.fromCql("themis"), CqlIdentifier.fromCql("keyvalue"))
                .all()
                .where(Relation.column("app").isEqualTo(QueryBuilder.literal("themis")))
                .orderBy("key", ClusteringOrder.DESC)
                .limit(this.limit)
                .build();
        if (origin) {
            System.out.println("Querying origin");
            queryCluster(ClusterName.ORIGIN, insertStmt);
        }
        if (target) {
            System.out.println("Querying target");
            queryCluster(ClusterName.TARGET, insertStmt);
        }
        if (proxy) {
            System.out.println("Querying proxy");
            queryCluster(ClusterName.PROXY, insertStmt);
        }
        return 0;
    }

    private void queryCluster(ClusterName name, Statement stmt) {

        for (Row row : this.clusters.get(name).getSession().execute(stmt)) {
            System.out.println(String.format("%d => %s", row.getInt("key"), row.getString("value")));
        }
    }
}
