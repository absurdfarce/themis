package com.datastax.themis.config;

import com.datastax.themis.cluster.Cluster;
import com.google.common.collect.ImmutableMap;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {

    public static ImmutableMap<ClusterName, Cluster> loadAllClusters(InputStream in) {

        Yaml yaml = new Yaml();
        Map<String, Map<String, ?>> data = yaml.load(in);

        /* It's okay if we don't have an entry in the YAML for every cluster... we'll just try to work with what we have */
        ImmutableMap.Builder<ClusterName, Cluster> rvBuilder = ImmutableMap.builder();
        for (Map.Entry<String, Map<String, ?>> entry : data.entrySet()) {

            rvBuilder.put(
                    ClusterName.valueOf(entry.getKey()),
                    ClusterFactory.buildCluster(entry.getValue()));
        }
        return rvBuilder.build();
    }
}
