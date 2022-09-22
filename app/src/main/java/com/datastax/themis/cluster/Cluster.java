package com.datastax.themis.cluster;

public interface Cluster {

    public boolean createSchema();

    public default boolean isAstra() { return false; }

    public default boolean isLocal() { return false; }
}
