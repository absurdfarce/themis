package com.datastax.themis.config;

import com.datastax.themis.ThemisException;
import com.datastax.themis.cluster.Cluster;
import com.datastax.themis.cluster.DefaultCluster;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {

    public static ImmutableMap<ClusterName, Cluster> load(InputStream in)
    throws ThemisException {

        Yaml yaml = new Yaml();
        Map<String, Map<String, ?>> data = yaml.load(in);
        return buildClusters(data);
    }

    private static ImmutableMap<ClusterName, Cluster> buildClusters(Map<String,Map<String,?>> config)
    throws ThemisException {

        ImmutableMap<ClusterName, Map<String,?>> data = convertConfigMap(config);

        Map<ClusterName, Cluster> rv = Maps.newHashMap();
        for (ClusterName name : ClusterName.values()) {
            if (! data.containsKey(name)) {
                throw new ThemisException(String.format("Config must contain top-level key %s", name));
            }
            rv.put(name,
                    new DefaultCluster(
                            name.name(),
                            ClusterConfig.buildFromConfig(name, convertClusterMap(data.get(name)))));
        }
        return ImmutableMap.copyOf(rv);
    }

    private static <T> ImmutableMap<ClusterName, T> convertConfigMap(Map<String, T> input) {
        return input.entrySet().stream().collect(ImmutableMap.toImmutableMap(e -> ClusterName.valueOf(e.getKey().toUpperCase()), e -> e.getValue()));
    }

    private static <T> ImmutableMap<ClusterConfigKey, ?> convertClusterMap(Map<String, ?> input) {
        return input.entrySet().stream().collect(ImmutableMap.toImmutableMap(e -> ClusterConfigKey.valueOf(e.getKey().toUpperCase()), e -> e.getValue()));
    }
}
