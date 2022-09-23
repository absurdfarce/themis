package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.InsertInto;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class InsertCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Execute the insertion against the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Execute the insertion against the target")
    boolean target;

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

        Statement maxQuery = QueryBuilder.selectFrom(CqlIdentifier.fromCql("themis"), CqlIdentifier.fromCql("keyvalue"))
                .function("max", Selector.column("key"))
                .build();
        InsertInto insertBuilder = QueryBuilder.insertInto(CqlIdentifier.fromCql("themis"), CqlIdentifier.fromCql("keyvalue"));

        if (origin) {
            System.out.println(String.format("Inserting %d new rows into origin", this.count));
            updateCluster(ClusterName.ORIGIN, maxQuery, insertBuilder);
        }
        if (target) {
            System.out.println(String.format("Inserting %d new rows into target", this.count));
            updateCluster(ClusterName.TARGET, maxQuery, insertBuilder);
        }
        if (proxy) {
            System.out.println(String.format("Inserting %d new rows into proxy", this.count));
            updateCluster(ClusterName.PROXY, maxQuery, insertBuilder);
        }
        return 0;
    }

    private void updateCluster(ClusterName name, Statement maxQuery, InsertInto insertBuilder) {

        ResultSet maxRs = this.clusters.get(name).getSession().execute(maxQuery);
        int currentMax = maxRs.iterator().next().getInt(0);

        for (int i = currentMax + 1; i <= currentMax + this.count; ++i) {
            this.clusters.get(name).getSession().execute(
                    insertBuilder
                            .value("key", QueryBuilder.literal(i))
                            .value("value",QueryBuilder.literal("some text"))
                            .value("app", QueryBuilder.literal("themis"))
                            .build());
        }
    }
}
