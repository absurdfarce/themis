package com.datastax.themis;

import com.datastax.themis.cluster.Cluster;
import com.google.common.base.Preconditions;

public class ThemisApp {

    private final Cluster origin;
    private final Cluster target;
    private final Cluster proxy;

    private ThemisApp(Cluster origin, Cluster target, Cluster proxy) {
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

        public ThemisApp build() {
            validate();
            return new ThemisApp(this.origin, this.target, this.proxy);
        }
    }
}
