package com.datastax.zdm.validate.cluster;

import com.google.common.base.Preconditions;

import java.net.InetAddress;
import java.nio.file.Path;

public class ClusterFactory {

    public static AstraCluster buildAstraCluster(Path scb, String clientID, String secret) {
        validateAstraArgs(scb, clientID, secret);
        return new AstraCluster(scb, clientID, secret);
    }

    public static LocalCluster buildLocalCluster(String localDc, InetAddress address, int port) {
        validateLocalArgs(localDc, address, port);
        return new LocalCluster(localDc, address, port);
    }

    private static void validateAstraArgs(Path scb, String clientID, String secret) {
        Preconditions.checkNotNull(scb);
        Preconditions.checkNotNull(clientID);
        Preconditions.checkNotNull(secret);
        Preconditions.checkArgument(!clientID.isEmpty());
        Preconditions.checkArgument(!secret.isEmpty());
    }

    private static void validateLocalArgs(String localDc, InetAddress address, int port) {
        Preconditions.checkNotNull(localDc);
        Preconditions.checkNotNull(address);
        Preconditions.checkNotNull(port);
        Preconditions.checkArgument(!localDc.isEmpty());
        Preconditions.checkArgument(port > 0);
    }

}
