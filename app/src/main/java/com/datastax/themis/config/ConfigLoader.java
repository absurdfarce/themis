package com.datastax.themis.config;

import com.datastax.themis.cluster.*;
import com.datastax.themis.config.ConfigException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Load config information from a YAML file.
 * <br><br>
 * Config format requirements and validation operations should remain encapsulated within this class
 */
public class ConfigLoader {

    private enum ClusterKeys {

        ADDRESS,
        PORT,
        LOCALDC,
        SCB,
        CLIENTID,
        SECRET
    }

    private static final Set<String> localKeys =
            ImmutableSet.of(ClusterKeys.ADDRESS.name(), ClusterKeys.PORT.name(), ClusterKeys.LOCALDC.name());
    private static final Set<String> astraKeys =
            ImmutableSet.of(ClusterKeys.SCB.name(), ClusterKeys.CLIENTID.name(), ClusterKeys.SECRET.name());
    private static final Set<String> proxyKeys =
            ImmutableSet.of(ClusterKeys.ADDRESS.name(), ClusterKeys.PORT.name(), ClusterKeys.CLIENTID.name(), ClusterKeys.SECRET.name());

    public static ImmutableMap<ClusterName, Cluster> load(InputStream in)
    throws ConfigException {

        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(in);
        return buildClusters(data);
    }

    private static ImmutableMap<ClusterName, Cluster> buildClusters(Map<String,Map<String,String>> config)
    throws ConfigException {

        ImmutableMap<String,Map<String,String>> data = normalizeConfigMap(config);

        Map<ClusterName, Cluster> rv = Maps.newHashMap();
        for (ClusterName key : ClusterName.values()) {
            String keyName = key.name();
            if (! data.containsKey(keyName)) {
                throw new ConfigException(String.format("Config must contain top-level key %s", keyName));
            }
            rv.put(key, buildCluster(normalizeConfigMap(data.get(keyName))));
        }
        return ImmutableMap.copyOf(rv);
    }

    private static Cluster buildCluster(Map<String,String> data)
    throws ConfigException {

        Set<String> keys = data.keySet();
        /* Note that we have to check for proxy configs first here (since proxy configs are a superset of local configs) */
        if (isProxyClusterConfig(keys)) {
            return buildProxyCluster(data);
        }
        else if (isAstraClusterConfig(keys)) {
            return buildAstraCluster(data);
        }
        else if (isLocalClusterConfig(keys)) {
            return buildLocalCluster(data);
        }
        throw new ConfigException(String.format("Config map can't be identified as a local or Astra cluster config, keys: %s", keys));
    }

    private static boolean isAstraClusterConfig(Set<String> keys) {
        return keys.containsAll(astraKeys);
    }

    private static boolean isLocalClusterConfig(Set<String> keys) {
        return keys.containsAll(localKeys);
    }

    private static boolean isProxyClusterConfig(Set<String> keys) {
        return keys.containsAll(proxyKeys);
    }

    private static AstraCluster buildAstraCluster(Map<String, String> data)
    throws ConfigException{

        String scbPath = getConfigString(data, ClusterKeys.SCB.name(), "Astra config contains entry for secure connect bundle but it is empty");
        Path scb = Paths.get(scbPath);

        String clientId = getConfigString(data, ClusterKeys.CLIENTID.name(), "Astra config contains entry for client ID but it is empty");
        String secret = getConfigString(data, ClusterKeys.SECRET.name(), "Astra config contains entry for secret but it is empty");

        return new AstraCluster(scb, clientId, secret);
    }

    private static LocalCluster buildLocalCluster(Map<String, ?> data)
    throws ConfigException {

        String addressStr = getConfigString(data, ClusterKeys.ADDRESS.name(), "Local config contains entry for address but it is empty");
        InetAddress address;
        try { address = InetAddress.getByName(addressStr); }
        catch (UnknownHostException uhe) {
            throw new ConfigException(String.format("Exception converting address entry into an InetAddress: %s", addressStr), uhe);
        }

        /* snakeyaml automatically converts discovered integer strings to ints by default */
        Object portObj = data.get(ClusterKeys.PORT.name());
        if (! (portObj instanceof Integer)) {
            throw new ConfigException(String.format("Local config contains entry for port but it is of type %s", portObj.getClass()));
        }
        int port = ((Integer)portObj).intValue();

        String localDc = getConfigString(data, ClusterKeys.LOCALDC.name(), "Local config contains entry for local DC but it is empty");

        return new LocalCluster(address, port, localDc);
    }

    private static ProxyCluster buildProxyCluster(Map<String, ?> data)
            throws ConfigException {

        String addressStr = getConfigString(data, ClusterKeys.ADDRESS.name(), "Proxy config contains entry for address but it is empty");
        InetAddress address;
        try { address = InetAddress.getByName(addressStr); }
        catch (UnknownHostException uhe) {
            throw new ConfigException(String.format("Exception converting address entry into an InetAddress: %s", addressStr), uhe);
        }

        /* snakeyaml automatically converts discovered integer strings to ints by default */
        Object portObj = data.get(ClusterKeys.PORT.name());
        if (! (portObj instanceof Integer)) {
            throw new ConfigException(String.format("Proxy config contains entry for port but it is of type %s", portObj.getClass()));
        }
        int port = ((Integer)portObj).intValue();

        String localDc = getConfigString(data, ClusterKeys.LOCALDC.name(), "Proxy config contains entry for local DC but it is empty");

        String clientId = getConfigString(data, ClusterKeys.CLIENTID.name(), "Proxy config contains entry for client ID but it is empty");
        String secret = getConfigString(data, ClusterKeys.SECRET.name(), "Proxy config contains entry for secret but it is empty");

        return new ProxyCluster(address, port, localDc, clientId, secret);
    }

    private static String getConfigString(Map<String, ?> data, String key, String emptyMsg)
    throws ConfigException {

        Object val = data.get(key);
        if (! (val instanceof String)) {
            throw new ConfigException(String.format("Expected config value with key %s to be a String, instead found type %s", key, val.getClass()));
        }
        String rv = val.toString();
        if (rv.isEmpty()) {
            throw new ConfigException(emptyMsg);
        }
        return rv;
    }

    /**
     * Normalize a Map<String,T> to something that plays nicely with the various enums.  Really this is just about making sure the
     * keys are capitalized.
     */
    private static <T> ImmutableMap<String, T> normalizeConfigMap(Map<String, T> input) {
        return input.entrySet().stream().collect(ImmutableMap.toImmutableMap(e -> e.getKey().toUpperCase(), e -> e.getValue()));
    }
}
