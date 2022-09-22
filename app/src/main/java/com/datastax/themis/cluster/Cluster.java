package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;

public interface Cluster {

    public CqlSession getSession();

    public boolean createSchema();
}
