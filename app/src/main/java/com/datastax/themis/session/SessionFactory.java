package com.datastax.themis.session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.google.common.collect.Lists;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

public class SessionFactory {

    public static CqlSession build(String localDc, InetAddress contactAddr, int contactPort) {
        return build(localDc, new InetSocketAddress(contactAddr, contactPort));
    }

    public static CqlSession build(String localDc, InetSocketAddress contactPoint) {
        return build(localDc, Lists.newArrayList(contactPoint));
    }

    public static CqlSession build(String localDc, Collection<InetSocketAddress> contactPoints) {
        return CqlSession
                .builder()
                .addContactPoints(contactPoints)
                .withLocalDatacenter(localDc)
                .build();
    }

    public static CqlSession build(URL scb, String clientID, String secret) {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(scb)
                .withAuthCredentials(clientID, secret)
                .build();
    }

    public static CqlSession build(Path scb, String clientID, String secret) {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(scb)
                .withAuthCredentials(clientID, secret)
                .build();
    }

    public static CqlSession build(InputStream scb, String clientID, String secret) {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(scb)
                .withAuthCredentials(clientID, secret)
                .build();
    }
}
