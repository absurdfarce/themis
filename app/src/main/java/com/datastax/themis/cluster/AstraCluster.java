package com.datastax.zdm.validate.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.zdm.validate.session.SessionFactory;

import java.nio.file.Path;
import java.util.Objects;

public class AstraCluster implements Cluster {

    private final Path scb;
    private final String clientID;
    private final String secret;

    private CqlSession cql;

    public AstraCluster(Path scb, String clientID, String secret) {

        this.scb = scb;
        this.clientID = clientID;
        this.secret = secret;

        this.cql = SessionFactory.build(scb, clientID, secret);
    }

    @Override
    public boolean isAstra() { return true; }

    @Override
    public boolean createSchema() {
        return false;
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
