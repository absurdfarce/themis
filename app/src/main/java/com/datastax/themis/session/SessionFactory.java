package com.datastax.themis.session;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Optional;

public class SessionFactory {

    public static CqlSession build(Path scb, String clientID, String secret) {
        return CqlSession
                .builder()
                .withCloudSecureConnectBundle(scb)
                .withAuthCredentials(clientID, secret)
                .build();
    }

    public static CqlSession build(@NonNull InetAddress address, int port, @NonNull String localDc) {
        return build(address, port, localDc, Optional.empty(), Optional.empty());
    }

    public static CqlSession build(@NonNull InetAddress address, int port, @NonNull String localDc, @NonNull Optional<String> username, @NonNull Optional<String> password) {
        CqlSessionBuilder builder = CqlSession
                .builder()
                .addContactPoint(new InetSocketAddress(address, port))
                .withLocalDatacenter(localDc);
        username.ifPresent(u -> {
            password.ifPresent(p -> {
                builder.withAuthCredentials(u,p);
            });
        });
        return builder.build();
    }
}
