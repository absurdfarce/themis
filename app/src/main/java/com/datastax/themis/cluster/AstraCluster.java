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

    public static AstraCluster.Builder builder() { return new AstraCluster.Builder(); }

    CqlSession buildSession() {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(this.scb)
                .withAuthCredentials(this.username, this.password)
                .build();
    }

    public static class Builder {

        private Optional<Path> scb = Optional.empty();
        private Optional<String> username = Optional.empty();
        private Optional<String> password = Optional.empty();

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

        private boolean validate() {

            if (this.scb.isEmpty()) {
                logger.error("Secure connect bundle (SCB) is required for DefaultCluster");
                return false;
            }
            if (this.username.isEmpty()) {
                logger.error("Username (Astra client ID) is required for AstraCluster");
                return false;
            }
            if (this.password.isEmpty()) {
                logger.error("Password (Astra secret) is required for AstraCluster");
                return false;
            }
            return true;
        }

        public AstraCluster build()
                throws ThemisException {

            if (! validate()) {
                throw new ThemisException("Could not validate configs for Astra cluster, consult the log for details");
            }
            return new AstraCluster(this.scb.get(),this.username.get(), this.password.get());
        }
    }
}
