package com.datastax.themis.cli;

import com.datastax.themis.cluster.AstraCluster;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.cluster.ClusterName;
import com.datastax.themis.cluster.LocalCluster;
import com.datastax.themis.config.ConfigException;
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

    private enum LocalKeys {
        ADDRESS,
        PORT,
        LOCALDC
    }

    private enum AstraKeys {
        SCB,
        CLIENTID,
        SECRET
    }

    private static final Set<String> localKeys = ImmutableSet.copyOf(Arrays.stream(LocalKeys.values()).map(Enum::name).collect(Collectors.toSet()));
    private static final Set<String> astraKeys = ImmutableSet.copyOf(Arrays.stream(AstraKeys.values()).map(Enum::name).collect(Collectors.toSet()));

    public static Map<ClusterName, Cluster> load(InputStream in)
    throws ConfigException {

        Yaml yaml = new Yaml();
        Map<String, Map<String, String>> data = yaml.load(in);
        return buildClusters(data);
    }

    private static Map<ClusterName, Cluster> buildClusters(Map<String,Map<String,String>> data)
    throws ConfigException {
        Map<ClusterName, Cluster> rv = Maps.newHashMap();
        for (ClusterName key : ClusterName.values()) {
            if (! data.containsKey(key.name())) {
                throw new ConfigException(String.format("Config must contain top-level key %s", key.name()));
            }
            rv.put(key, buildCluster(data.get(key)));
        }
        return rv;
    }

    private static Cluster buildCluster(Map<String,String> data)
    throws ConfigException {

        Set<String> keys = data.keySet();
        if (isAstraClusterConfig(keys)) {
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

    private static AstraCluster buildAstraCluster(Map<String, String> data)
    throws ConfigException{

        String scbPath = getConfigString(data, AstraKeys.SCB.name(), "Astra config contains entry for secure connect bundle but it is empty");
        Path scb = Paths.get(scbPath);

        String clientId = getConfigString(data, AstraKeys.CLIENTID.name(), "Astra config contains entry for client ID but it is empty");
        String secret = getConfigString(data, AstraKeys.SECRET.name(), "Astra config contains entry for secret but it is empty");

        return new AstraCluster(scb, clientId, secret);
    }

    private static LocalCluster buildLocalCluster(Map<String, String> data)
    throws ConfigException {

        String localDc = getConfigString(data, LocalKeys.LOCALDC.name(), "Local config contains entry for local DC but it is empty");

        String addressStr = getConfigString(data, LocalKeys.ADDRESS.name(), "Local config contains entry for address but it is empty");
        InetAddress address;
        try { address = InetAddress.getByName(addressStr); }
        catch (UnknownHostException uhe) {
            throw new ConfigException(String.format("Exception converting address entry into an InetAddress: %s", addressStr), uhe);
        }

        String portStr = getConfigString(data, LocalKeys.PORT.name(), "Local config contains entry for port but it is empty");
        int port = -1;
        try { port = Integer.parseInt(portStr); }
        catch (NumberFormatException nfe) {
            throw new ConfigException(String.format("Exception converting port entry into an integer: %s", portStr), nfe);
        }

        return new LocalCluster(localDc, address, port);
    }

    private static String getConfigString(Map<String, String> data, String key, String emptyMsg)
    throws ConfigException {

        String val = data.get(key);
        if (val.isEmpty()) {
            throw new ConfigException(emptyMsg);
        }
        return val;
    }
}
