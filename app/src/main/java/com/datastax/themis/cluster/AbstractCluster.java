package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractCluster implements Cluster {

    private final AtomicReference<CqlSession> sessionRef = new AtomicReference<>(null);

    public abstract CqlSession buildSession();

    @Override
    public CqlSession getSession() {

        sessionRef.compareAndSet(null, buildSession());
        return sessionRef.get();
    }
}
