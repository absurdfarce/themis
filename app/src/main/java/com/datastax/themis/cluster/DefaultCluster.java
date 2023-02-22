package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.themis.ThemisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class DefaultCluster extends Cluster {

    private static Logger logger = LoggerFactory.getLogger(DefaultCluster.class);

    private final InetAddress address;
    private final int port;
    private final String localDc;
    private final Optional<String> username;
    private final Optional<String> password;

    private DefaultCluster(InetAddress address, int port, String localDc, Optional<String> username, Optional<String> password) {
        this.address = address;
        this.port = port;
        this.localDc = localDc;
        this.username = username;
        this.password = password;
    }

    public static DefaultCluster.Builder builder(String name) { return new DefaultCluster.Builder(name); }

    CqlSession buildSession() {

        CqlSessionBuilder builder = CqlSession
                .builder()
                .addContactPoint(new InetSocketAddress(this.address, this.port))
                .withLocalDatacenter(this.localDc);
        this.username.ifPresent(u -> {
            this.password.ifPresent(p -> {
                builder.withAuthCredentials(u,p);
            });
        });
        return builder.build();
    }

    public static class Builder {

        private final String name;

        private Optional<InetAddress> address = Optional.empty();
        private Optional<Integer> port = Optional.empty();
        private Optional<String> localDc = Optional.empty();
        private Optional<String> username = Optional.empty();
        private Optional<String> password = Optional.empty();

        public Builder(String name) {
            this.name = name;
        }

        public Builder address(InetAddress address) {
            this.address = Optional.of(address);
            return this;
        }

        public Builder port(int port) {
            this.port = Optional.of(port);
            return this;
        }

        public Builder localDc(String localDc) {
            this.localDc = Optional.of(localDc);
            return this;
        }

        public Builder username(String username) {
            this.username = Optional.of(username);
            return this;
        }

        public Builder password(String password) {
            this.password = Optional.of(password);
            return this;
        }

        private void validate()
        throws ThemisException {

            if (this.address.isEmpty()) {
                throw new ThemisException("Address is required for DefaultCluster %s", this.name);
            }
            if (this.port.isEmpty()) {
                throw new ThemisException("Port is required for DefaultCluster", this.name);
            }
            if (this.localDc.isEmpty()) {
                throw new ThemisException("Local DC is required for DefaultCluster", this.name);
            }
        }

        public DefaultCluster build()
        throws ThemisException {

            validate();
            return new DefaultCluster(this.address.get(),this.port.get(), this.localDc.get(), this.username, this.password);
        }
    }
}