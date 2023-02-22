package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.config.ClusterConfig;

public class AstraCluster {

    private final String name;
    private final ClusterConfig config;

    public AstraCluster(String name, ClusterConfig config) {
        this.name = name;
        this.config = config;
    }

    CqlSession buildSession() {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(this.config.getScb().get())
                .withAuthCredentials(this.config.getUsername().get(), this.config.getPassword().get())
                .build();
    }
}
