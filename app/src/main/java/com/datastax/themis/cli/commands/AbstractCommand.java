package com.datastax.themis.cli.commands;

import picocli.CommandLine;

public abstract class AbstractCommand {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help and exit")
    boolean help;

    @CommandLine.Option(names = {"-o", "--origin"}, description = "Execute the operation against the origin")
    boolean origin;

    @CommandLine.Option(names = {"-t", "--target"}, description = "Execute the operation against the target")
    boolean target;

    @CommandLine.Option(names = {"-k", "--keyspace"}, description = "The keyspace the operation should use", defaultValue = "themis")
    String keyspace;

    @CommandLine.Option(names = {"-a", "--table"}, description = "The table the operation should use", defaultValue = "keyvalue")
    String table;
}
