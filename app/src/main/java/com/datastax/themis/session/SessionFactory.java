package com.datastax.themis.session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.auth.ProgrammaticPlainTextAuthProvider;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

public class SessionFactory {

    public static CqlSession build(InetAddress address, int port, String localDc) {
        return CqlSession
                .builder()
                .addContactPoint(new InetSocketAddress(address, port))
                .withLocalDatacenter(localDc)
                .build();
    }

    public static CqlSession build(Path scb, String clientID, String secret) {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(scb)
                .withAuthCredentials(clientID, secret)
                .build();
    }

    public static CqlSession build(InetAddress address, int port, String localDc, String clientID, String secret) {
        return CqlSession
                .builder()
                .addContactPoint(new InetSocketAddress(address, port))
                .withAuthCredentials(clientID, secret)
                .withLocalDatacenter(localDc)
                .build();
    }
}
