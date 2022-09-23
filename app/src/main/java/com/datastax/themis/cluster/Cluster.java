package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;

/**
 * An object representing a Cassandra database on which our zdm proxy operates.
 * <br><br>
 * Note that the zdm endpoint itself is also represented by an instance of this class.
 */
public interface Cluster {

    public CqlSession getSession();
}
