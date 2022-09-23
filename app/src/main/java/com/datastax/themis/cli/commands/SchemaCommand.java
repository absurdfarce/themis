package com.datastax.themis.cli.commands;

import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command()
public class SchemaCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Create the schema on the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Create the schema on the target")
    boolean target;

    private final ImmutableMap<ClusterName, Cluster> clusters;

    public SchemaCommand(ImmutableMap<ClusterName, Cluster> clusters) {
        this.clusters = clusters;
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
