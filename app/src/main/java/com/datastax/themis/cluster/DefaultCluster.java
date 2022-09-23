package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.ThemisException;
import com.datastax.themis.config.ClusterConfig;
import com.datastax.themis.session.SessionFactory;

import java.util.concurrent.atomic.AtomicReference;

public class DefaultCluster implements Cluster {

    private final AtomicReference<CqlSession> sessionRef = new AtomicReference<>(null);

    private final String name;
    private final ClusterConfig config;

    public DefaultCluster(String name, ClusterConfig config) {
        this.name = name;
        this.config = config;
    }

    @Override
    public String getName() { return this.name; }

    @Override
    public CqlSession getSession() {

        sessionRef.compareAndSet(null, buildSession());
        return sessionRef.get();
    }

    private CqlSession buildSession() {
        if (isAstra()) {
            return SessionFactory.build(
                    this.config.getScb().get(),
                    this.config.getUsername().get(),
                    this.config.getPassword().get());
        }
        else {
            return SessionFactory.build(
                    this.config.getAddress().get(),
                    this.config.getPort().get(),
                    this.config.getLocalDc().get(),
                    this.config.getUsername(),
                    this.config.getPassword());
        }
    }

    private boolean isAstra() { return !this.config.getScb().isEmpty(); }
}
