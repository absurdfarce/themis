package com.datastax.themis.cli.commands;

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

    private final ImmutableMap<ClusterName, Cluster> clusters;

    public InsertCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
