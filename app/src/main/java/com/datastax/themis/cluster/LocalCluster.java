package com.datastax.zdm.validate.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.zdm.validate.session.SessionFactory;

import java.net.InetAddress;
import java.util.Objects;

public class LocalCluster implements Cluster {

    private final String localDc;
    private final InetAddress address;
    private final int port;

    private CqlSession cql;

    public LocalCluster(String localDc, InetAddress address, int port) {

        this.localDc = localDc;
        this.address = address;
        this.port = port;

        this.cql = SessionFactory.build(localDc, address, port);
    }

    @Override
    public boolean isLocal() { return true; }

    @Override
    public boolean createSchema() {
        return false;
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
