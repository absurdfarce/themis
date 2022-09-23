package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.session.SessionFactory;

import java.net.InetAddress;
import java.util.Objects;

public class ProxyCluster extends AbstractCluster {

    private final InetAddress address;
    private final int port;
    private final String localDc;
    private final String clientID;
    private final String secret;

    public ProxyCluster(InetAddress address, int port, String localDc, String clientID, String secret) {

        this.address = address;
        this.port = port;
        this.localDc = localDc;
        this.clientID = clientID;
        this.secret = secret;
    }

    @Override
    public CqlSession buildSession() {
        return SessionFactory.build(this.address, this.port, this.localDc, this.clientID, this.secret);
    }

    @Override
    public String toString() {
        return "ProxyCluster{" +
                "address=" + address +
                ", port=" + port +
                ", localDc='" + localDc + '\'' +
                ", clientID='" + clientID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyCluster that = (ProxyCluster) o;
        return port == that.port && Objects.equals(address, that.address) && Objects.equals(localDc, that.localDc) && Objects.equals(clientID, that.clientID) && Objects.equals(secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, port, localDc, clientID, secret);
    }
}
