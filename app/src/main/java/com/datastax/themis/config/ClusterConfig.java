package com.datastax.themis.config;

import com.datastax.themis.ThemisException;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ClusterConfig {

    private final Optional<InetAddress> address;
    private final Optional<Integer> port;
    private final Optional<String> localDc;
    private final Optional<String> username;
    private final Optional<String> password;
    private final Optional<Path> scb;

    private ClusterConfig(Optional<InetAddress> address,
                          Optional<Integer> port,
                          Optional<String> localDc,
                          Optional<String> username,
                          Optional<String> password,
                          Optional<Path> scb) {

        this.address = address;
        this.port = port;
        this.localDc = localDc;
        this.username = username;
        this.password = password;
        this.scb = scb;
    }

    public Optional<InetAddress> getAddress() {
        return address;
    }

    public Optional<Integer> getPort() {
        return port;
    }

    public Optional<String> getLocalDc() {
        return localDc;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getPassword() {
        return password;
    }

    public Optional<Path> getScb() {
        return scb;
    }

    public static ClusterConfig buildFromConfig(ClusterName name, ImmutableMap<ClusterConfigKey, ?> config)
    throws ThemisException {

        return new ClusterConfig(
                getValidatedAddress(config, name),
                getValidatedPort(config, name),
                getValidatedString(ClusterConfigKey.LOCALDC, config, name),
                getValidatedString(ClusterConfigKey.USERNAME, config, name),
                getValidatedString(ClusterConfigKey.PASSWORD, config, name),
                getValidatedScb(config, name));
    }


    private static Optional<Object> getValidatedNonNullObject(ClusterConfigKey key, ImmutableMap<ClusterConfigKey, ?> config, ClusterName name)
    throws ThemisException {

        if (! config.containsKey(key))
            return Optional.empty();
        Object obj = config.get(key);
        if (obj == null)
            throw new ThemisException(String.format("Key %s for cluster %s is null", key, name));
        return Optional.of(obj);
    }

    private static Optional<String> getValidatedString(ClusterConfigKey key, ImmutableMap<ClusterConfigKey, ?> config, ClusterName name)
            throws ThemisException {

        Optional<?> option = getValidatedNonNullObject(key, config, name);
        if (option.isEmpty())
            return Optional.empty();
        Object obj = option.get();
        if (! (obj instanceof String))
            throw new ThemisException(String.format("Cluster %s expected key %s to be a String but it was of type %s", name, key, obj.getClass().getName()));
        String rv = obj.toString();
        if (rv.isBlank())
            throw new ThemisException(String.format("Key %s for cluster %s is empty or contains only whitespace", key, name));
        return Optional.of(rv);
    }

    private static Optional<InetAddress > getValidatedAddress(ImmutableMap<ClusterConfigKey, ?> config, ClusterName name)
    throws ThemisException {

        Optional<String> addressOption = getValidatedString(ClusterConfigKey.ADDRESS, config, name);
        if (addressOption.isEmpty())
            return Optional.empty();
        try { return Optional.of(InetAddress.getByName(addressOption.get())); }
        catch (UnknownHostException uhe) {
            throw new ThemisException(String.format("Exception while resolving address %s", addressOption.get()), uhe);
        }
    }

    private static Optional<Integer> getValidatedPort(ImmutableMap<ClusterConfigKey, ?> config, ClusterName name)
        throws ThemisException {

        Optional<?> option = getValidatedNonNullObject(ClusterConfigKey.PORT, config, name);
        if (option.isEmpty())
            return Optional.empty();
        Object obj = option.get();
        if (! (obj instanceof Integer))
            throw new ThemisException(String.format("Cluster %s expected port (%s) to be an Integer but it was of type %s", name, obj, obj.getClass().getName()));
        return Optional.of((Integer)obj);
    }

    private static Optional<Path> getValidatedScb(ImmutableMap<ClusterConfigKey, ?> config, ClusterName name)
            throws ThemisException {

        Optional<String> scbOption = getValidatedString(ClusterConfigKey.SCB, config, name);
        /* Optional.map() would be preferred here but we're leveraging side effects (i.e. exceptions) to notify the top-level
        * CLI... and that doesn't play nice with map() + closures */
        if (scbOption.isEmpty())
            return Optional.empty();
        Path rv = Paths.get(scbOption.get());
        File rvFile = rv.toFile();
        if (! rvFile.exists())
            throw new ThemisException(String.format("Cluster %s contains an SCB config %s but that file does not exist", name, rvFile.getAbsolutePath()));
        if (! rvFile.isFile())
            throw new ThemisException(String.format("Cluster %s contains an SCB config %s but that is not a file", name, rvFile.getAbsolutePath()));
        if (! rvFile.canRead())
            throw new ThemisException(String.format("Cluster %s contains an SCB config %s but that file can't be read", name, rvFile.getAbsolutePath()));
        return Optional.of(rv);
    }
}
