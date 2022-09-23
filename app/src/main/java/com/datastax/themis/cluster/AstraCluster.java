package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.session.SessionFactory;

import java.nio.file.Path;
import java.util.Objects;

/**
 * A {@link Cluster} object representing a Cassandra instance managed by Astra.
 */
public class AstraCluster extends AbstractCluster {

    private final Path scb;
    private final String clientID;
    private final String secret;

    public AstraCluster(Path scb, String clientID, String secret) {

        this.scb = scb;
        this.clientID = clientID;
        this.secret = secret;
    }

    @Override
    public CqlSession buildSession() {
        return SessionFactory.build(scb, clientID, secret);
    }

    @Override
    public String toString() {
        return "AstraCluster{" +
                "scb=" + scb +
                ", clientID='" + clientID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AstraCluster that = (AstraCluster) o;
        return Objects.equals(scb, that.scb) && Objects.equals(clientID, that.clientID) && Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scb, clientID, secret);
    }
}
