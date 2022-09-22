package com.datastax.zdm.validate;

import com.datastax.zdm.validate.cluster.Cluster;
import com.google.common.base.Preconditions;

public class ValidatorApp {

    private final Cluster origin;
    private final Cluster target;
    private final Cluster proxy;

    private ValidatorApp(Cluster origin, Cluster target, Cluster proxy) {
        this.origin = origin;
        this.target = target;
        this.proxy = proxy;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private Cluster origin;
        private Cluster target;
        private Cluster proxy;

        public Builder withOrigin(Cluster cluster) {
            this.origin = cluster;
            return this;
        }

        public Builder withTarget(Cluster cluster) {
            this.target = cluster;
            return this;
        }

        public Builder withProxy(Cluster cluster) {
            this.proxy = cluster;
            return this;
        }

        private void validate() {
            Preconditions.checkNotNull(this.origin);
            Preconditions.checkNotNull(this.target);
            Preconditions.checkNotNull(this.proxy);
        }

        public ValidatorApp build() {
            validate();
            return new ValidatorApp(this.origin, this.target, this.proxy);
        }
    }
}
