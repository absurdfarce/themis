package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.session.SessionFactory;

import java.net.InetAddress;
import java.util.Objects;

/**
 * A {@link Cluster} object representing a local Cassandra instance.
 */
public class LocalCluster extends AbstractCluster {

    private final InetAddress address;
    private final int port;
    private final String localDc;

    public LocalCluster(InetAddress address, int port, String localDc) {

        this.localDc = localDc;
        this.address = address;
        this.port = port;
    }

    @Override
    public CqlSession buildSession() {
        return SessionFactory.build(address, port, localDc);
    }

    @Override
    public String toString() {
        return "LocalCluster{" +
                "localDc='" + localDc + '\'' +
                ", address=" + address +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalCluster that = (LocalCluster) o;
        return port == that.port && Objects.equals(localDc, that.localDc) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localDc, address, port);
    }
}
