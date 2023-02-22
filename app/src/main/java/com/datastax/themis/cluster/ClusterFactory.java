package com.datastax.themis.cluster;

import com.datastax.themis.ThemisException;
import com.datastax.themis.config.ClusterConfigKey;
import com.datastax.themis.config.ClusterName;
import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

/**
 * This class performs validation of the ability to retrieve relevant values from the config map that's provided.
 * Validation of whether all necessary values are supplied, of the right type/value for a given cluster etc. are
 * the responsibility of the relevant builder.
 */
public class ClusterFactory {

    public static Cluster buildCluster(ClusterName name, Map<String,?> inMap)
    throws ThemisException {

        ImmutableMap<ClusterConfigKey, ?> clusterConfig = mapToClusterConfig(inMap);
        return clusterConfig.containsKey(ClusterConfigKey.SCB) ?
                buildAstraCluster(name, clusterConfig) :
                buildDefaultCluster(name, clusterConfig);
    }

    private static DefaultCluster buildDefaultCluster(ClusterName name, ImmutableMap<ClusterConfigKey, ?> clusterConfig)
    throws ThemisException {

        DefaultCluster.Builder builder = DefaultCluster.builder(name.name());
        getValidatedAddress(name, clusterConfig).ifPresent(builder::address);
        getValidatedPort(name, clusterConfig).ifPresent(builder::port);
        getValidatedString(name, ClusterConfigKey.LOCALDC, clusterConfig).ifPresent(builder::localDc);
        getValidatedString(name, ClusterConfigKey.USERNAME, clusterConfig).ifPresent(builder::username);
        getValidatedString(name, ClusterConfigKey.PASSWORD, clusterConfig).ifPresent(builder::password);
        return builder.build();
    }

    private static AstraCluster buildAstraCluster(ClusterName name, ImmutableMap<ClusterConfigKey, ?> clusterConfig)
    throws ThemisException {

        AstraCluster.Builder builder = AstraCluster.builder(name.name());
        getValidatedScb(name, clusterConfig).ifPresent(builder::scb);
        getValidatedString(name, ClusterConfigKey.USERNAME, clusterConfig).ifPresent(builder::username);
        getValidatedString(name, ClusterConfigKey.PASSWORD, clusterConfig).ifPresent(builder::password);
        return builder.build();
    }

    private static <T> ImmutableMap<ClusterConfigKey, ?> mapToClusterConfig(Map<String, ?> input) {
        return input.entrySet().stream().collect(ImmutableMap.toImmutableMap(e -> ClusterConfigKey.valueOf(e.getKey().toUpperCase()), e -> e.getValue()));
    }

    private static Optional<Object> getValidatedNonNullObject(ClusterName name, ClusterConfigKey key, ImmutableMap<ClusterConfigKey, ?> config)
            throws ThemisException {

        if (! config.containsKey(key))
            return Optional.empty();
        Object obj = config.get(key);
        if (obj == null)
            throw new ThemisException("Key %s for cluster %s is null", key, name);
        return Optional.of(obj);
    }

    private static Optional<String> getValidatedString(ClusterName name, ClusterConfigKey key, ImmutableMap<ClusterConfigKey, ?> config)
            throws ThemisException {

        Optional<?> option = getValidatedNonNullObject(name, key, config);
        if (option.isEmpty())
            return Optional.empty();
        Object obj = option.get();
        if (! (obj instanceof String))
            throw new ThemisException("Cluster %s expected key %s to be a String but it was of type %s", name, key, obj.getClass().getName());
        String rv = obj.toString();
        if (rv.isBlank())
            throw new ThemisException("Key %s for cluster %s is empty or contains only whitespace", key, name);
        return Optional.of(rv);
    }

    private static Optional<InetAddress> getValidatedAddress(ClusterName name, ImmutableMap<ClusterConfigKey, ?> config)
            throws ThemisException {

        Optional<String> addressOption = getValidatedString(name, ClusterConfigKey.ADDRESS, config);
        if (addressOption.isEmpty())
            return Optional.empty();
        try { return Optional.of(InetAddress.getByName(addressOption.get())); }
        catch (UnknownHostException uhe) {
            throw new ThemisException(uhe, "Exception while resolving address %s for cluster %s", addressOption.get(), name);
        }
    }

    private static Optional<Integer> getValidatedPort(ClusterName name, ImmutableMap<ClusterConfigKey, ?> config)
            throws ThemisException {

        Optional<?> option = getValidatedNonNullObject(name, ClusterConfigKey.PORT, config);
        if (option.isEmpty())
            return Optional.empty();
        Object obj = option.get();
        if (! (obj instanceof Integer))
            throw new ThemisException("Cluster %s expected port (%s) to be an Integer but it was of type %s", name, obj, obj.getClass().getName());
        return Optional.of((Integer)obj);
    }

    private static Optional<Path> getValidatedScb(ClusterName name, ImmutableMap<ClusterConfigKey, ?> config)
            throws ThemisException {

        Optional<String> scbOption = getValidatedString(name, ClusterConfigKey.SCB, config);
        /* Optional.map() would be preferred here but we're leveraging side effects (i.e. exceptions) to notify the top-level
         * CLI... and that doesn't play nice with map() + closures */
        if (scbOption.isEmpty())
            return Optional.empty();
        Path rv = Paths.get(scbOption.get());
        File rvFile = rv.toFile();
        if (! rvFile.exists())
            throw new ThemisException("Cluster %s contains an SCB config %s but that file does not exist", name, rvFile.getAbsolutePath());
        if (! rvFile.isFile())
            throw new ThemisException("Cluster %s contains an SCB config %s but that is not a file", name, rvFile.getAbsolutePath());
        if (! rvFile.canRead())
            throw new ThemisException("Cluster %s contains an SCB config %s but that file can't be read", name, rvFile.getAbsolutePath());
        return Optional.of(rv);
    }
}
