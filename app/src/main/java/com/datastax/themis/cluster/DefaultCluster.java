package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.themis.config.ClusterConfig;

import java.net.InetSocketAddress;

public class DefaultCluster extends Cluster {

    private final String name;
    private final ClusterConfig config;

    public DefaultCluster(String name, ClusterConfig config) {
        this.name = name;
        this.config = config;
    }

    CqlSession buildSession() {

        CqlSessionBuilder builder = CqlSession
                .builder()
                .addContactPoint(new InetSocketAddress(this.config.getAddress().get(), this.config.getPort().get()))
                .withLocalDatacenter(this.config.getLocalDc().get());
        this.config.getUsername().ifPresent(u -> {
            this.config.getPassword().ifPresent(p -> {
                builder.withAuthCredentials(u,p);
            });
        });
        return builder.build();
    }
}
