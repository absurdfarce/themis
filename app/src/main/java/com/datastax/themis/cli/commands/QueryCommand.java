package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.cluster.ClusterName;
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

        Statement stmt = buildStatement();
        if (origin) {
            System.out.println("Querying origin");
            queryServer(ClusterName.ORIGIN, stmt);
        }
        if (target) {
            System.out.println("Querying target");
            queryServer(ClusterName.TARGET, stmt);
        }
        if (proxy) {
            System.out.println("Querying proxy");
            queryServer(ClusterName.PROXY, stmt);
        }
        return 0;
    }

    private Statement buildStatement() {

        return QueryBuilder.selectFrom(CqlIdentifier.fromCql("themis"), CqlIdentifier.fromCql("keyvalue")).all().build();
    }

    private void queryServer(ClusterName name, Statement stmt) {

        for (Row row : this.clusters.get(name).getSession().execute(stmt)) {
            System.out.println(String.format("%d => %s", row.getInt("key"), row.getString("value")));
        }
    }
}
