package com.datastax.themis.cli.commands;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.themis.Constants;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

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

        /* Note that (unlike most of the other commands) this Statement relies on values passed in via picocli... so we
        * can't declare it as a constant field of this class */
        Statement insertStmt = QueryBuilder.selectFrom(Constants.DEFAULT_KEYSPACE, Constants.DEFAULT_TABLE)
                .all()
                .where(Relation.column("app").isEqualTo(QueryBuilder.literal("themis")))
                .orderBy("key", ClusteringOrder.DESC)
                .limit(this.limit)
                .build();

        boolean success = true;
        if (origin)
            success = success & queryCluster(ClusterName.ORIGIN, insertStmt);
        if (target)
            success = success & queryCluster(ClusterName.TARGET, insertStmt);
        if (proxy)
            success = success & queryCluster(ClusterName.PROXY, insertStmt);
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
