package com.datastax.themis.cluster;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.themis.ThemisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public class AstraCluster extends Cluster {

    private static Logger logger = LoggerFactory.getLogger(AstraCluster.class);

    private final Path scb;
    private final String username;
    private final String password;

    private AstraCluster(Path scb, String username, String password) {
        this.scb = scb;
        this.username = username;
        this.password = password;
    }

    public static AstraCluster.Builder builder(String name) { return new AstraCluster.Builder(name); }

    CqlSession buildSession() {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(this.scb)
                .withAuthCredentials(this.username, this.password)
                .build();
    }

    public static class Builder {

        private final String name;

        private Optional<Path> scb = Optional.empty();
        private Optional<String> username = Optional.empty();
        private Optional<String> password = Optional.empty();

        public Builder(String name) {
            this.name = name;
        }

        public Builder scb(Path scb) {
            this.scb = Optional.of(scb);
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

            if (this.scb.isEmpty()) {
                throw new ThemisException("Secure connect bundle (SCB) is required for DefaultCluster %s", this.name);
            }
            if (this.username.isEmpty()) {
                throw new ThemisException("Username (Astra client ID) is required for AstraCluster %s", this.name);
            }
            if (this.password.isEmpty()) {
                throw new ThemisException("Password (Astra secret) is required for AstraCluster %s", this.name);
            }
        }

        public AstraCluster build()
        throws ThemisException {

            validate();
            return new AstraCluster(this.scb.get(),this.username.get(), this.password.get());
        }
    }
}
