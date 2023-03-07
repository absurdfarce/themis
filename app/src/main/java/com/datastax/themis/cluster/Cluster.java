package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Cluster {

    private final AtomicReference<CqlSession> sessionRef = new AtomicReference<>(null);

    abstract CqlSession buildSession();

    public CqlSession getSession() {

        sessionRef.compareAndSet(null, buildSession());
        return sessionRef.get();
    }
}
