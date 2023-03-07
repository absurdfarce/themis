# Themis
A tool to make sure the scales [stay balanced](https://en.wikipedia.org/wiki/Themis).

# How Do You Build It
Build is implemented via [Gradle](https://gradle.org/).  You'll want to build a shadow JAR unless you want to manage dependencies some other way:

> ./gradlew shadowJar

# Overview
Themis is intended to simplify the process of querying a pair of Cassandra cluster fronted by a CQL-compatible proxy, which might be sending reads or writes to one or both of those clusters. 

In this example we will be using the [ZDM Proxy](https://github.com/datastax/zdm-proxy), which routes all requests as follows:
- Writes are always sent to both Origin and Target.
- Reads are sent to either Origin or Target, depending on how the proxy is configured.

For more information on the ZDM Proxy, please refer to its README and [official documentation](https://docs.datastax.com/en/astra-serverless/docs/migrate/introduction.html).

Themis's goal is to validate proper functioning of the proxy by providing an interactive way to write random sample data directly to one of those endpoints (Origin, Target or the proxy directly) and then query those same endpoints. For this reason, Themis will open connections as follows:
* Directly to the Origin cluster.
* To the proxy deployment (which in turn will connect to Origin and Target based on its configuration).
* Directly to the Target cluster.

This allows Themis to write and read data directly from any of these endpoints, enabling you to compare results and verify how the proxy is routing your requests. The examples explained later in this document will show you how this works in practice.

In the examples that follow, we will be using a [DataStax AstraDB](https://astra.datastax.com/) cluster as our Target. For more details on AstraDB, please see its [documentation](https://docs.datastax.com/en/astra-serverless/docs/index.html).

# How Is Themis Configured?
Via a YAML file called `.themis.yaml` and placed in your home directory.

In this file we have to specify three sets of connection parameters: one for the Origin cluster, one for the Target cluster and a third one for the proxy deployment. The exact parameters to specify vary depending on whether the connection is to a regular cluster or an AstraDB one, and on which cluster(s) require authentication.

If the configuration for a cluster contains an `scb` key it is assumed to represent an AstraDB cluster: in this case only the `username` and `password` keys will be used in addition to the secure connect bundle identified by `scb`, which contains all the other AstraDB connection parameters.

Starting from the simplest case, here's an example where the Origin cluster and the ZDM Proxy run locally, and Target is an AstraDB cluster:

```
origin:
  address: 127.0.0.1
  port: 9042
  localDc: my_origin_dc
target:
  scb: /my/astra/secure-connect-database.zip
  username: my_astra_clientid
  password: my_astra_secret
proxy:
  address: 127.0.0.1
  port: 14002
  localDc: my_origin_dc  
  username: my_astra_clientid
  password: my_astra_secret
```

In this case Origin is a single Cassandra node running locally and does not require authentication. The proxy is also installed locally, but since it must proxy to AstraDB (which does require authentication) the AstraDB credentials are required here as well.

If you are using a multi-node cluster, you can specify multiple contact points simply by passing them as a list. This also applies to proxy deployments with multiple proxy instances. For example, let's consider a more "production-like" deployment. In this case:
* The Origin cluster and the ZDM proxy run on dedicated machines, and we have to specify their private IP addresses.
* There are three contact points for Origin, listening on port 9042. Also, Origin requires authentication, so we have to pass valid credentials for it.
* Target is still an AstraDB cluster, which always requires authentication. You do not need to specify addresses or ports for AstraDB clusters - all this is implicitly contained in the Secure Connect Bundle.
* There are three proxy instances, also listening on port 9042. The credentials passed to connect to the proxy are the same as for Target.

The configuration file would look like this:

```
origin:
  address: [192.168.20.45, 192.168.21.63, 192.168.22.184]
  port: 9042
  localDc: my_origin_dc
  username: my_origin_username
  password: my_origin_password
target:
  scb: /my/astra/secure-connect-database.zip
  username: my_astra_clientid
  password: my_astra_secret
proxy:
  address: [172.18.10.37, 172.18.11.104, 172.18.12.91]
  port: 9042
  localDc: my_origin_dc  
  username: my_astra_clientid
  password: my_astra_secret
```

For more information on how to connect a client application to the ZDM Proxy, please refer to [this documentation page](https://docs.datastax.com/en/astra-serverless/docs/migrate/connect-clients-to-proxy.html).

# What Can Themis Do?
There are three commands available at the moment:

* schema - Creates the mock schema on the specified server
* insert - Add some amount of randomly generated data to the server
* query - Retrieve and display some amount of data from one (or more) of the servers

Each command includes detailed help, so this kind of thing might be useful:

```
$ java -jar app/build/libs/app-all.jar insert --help
Usage: themis insert [-hopt] [-a=<table>] [-c=<count>] [-k=<keyspace>]
  -a, --table=<table>   The table the operation should use. Defaults to "keyvalue".
  -c, --count=<count>   Number of records to insert
  -h, --help            Show this help and exit
  -k, --keyspace=<keyspace>
                        The keyspace the operation should use. Defaults to "themis".
  -o, --origin          Execute the operation against the origin
  -p, --proxy           Execute the insertion against the proxy
  -t, --target          Execute the operation against the target
```

A couple of notes about keyspace and table names:

- On AstraDB clusters, you will have to create the keyspace from the Astra UI. 
- The default keyspace name is `themis`, but if you wish to use a different name for the keyspace you can specify it with the option `--keyspace=<your_keyspace_name>`. 
- Likewise, the default table name is `keyvalue`, which can be overridden using the option `--table=<your_table_name>`

# Can You Give Me A More Complete Example?
Sure, why not?

Let's create the schema first:
```
$ java -jar app/build/libs/app-all.jar schema --origin --target                                                                                                                               
Creating schema on cluster ORIGIN 
Creating schema on cluster TARGET
Cluster TARGET is an Astra cluster, skipping keyspace creation (Astra keyspaces must be created through the Astra UI) 
```
Now we can insert 20 rows directly into Origin and Target:
```
$ java -jar app/build/libs/app-all.jar insert --origin --target -c 20
Inserting 20 new rows into cluster ORIGIN
Inserting 20 new rows into cluster TARGET
```
Note that these rows are randomly generated as they are inserted into each cluster directly, so they will have different values in each cluster. We can verify this by querying the last five rows from Target:
```
$ java -jar app/build/libs/app-all.jar query --target -l 5
Querying cluster TARGET
20 => tQWG\|\OmgZ5
19 => ]tM\Zk3EkoPD
18 => J${Sc.mT<Ji*
17 => ].b}M&J(y%1j
16 => m.D)q@^tyWqw
```
And then the last five rows from Origin and from the ZDM Proxy, which is currently configured to route all reads to Origin:
```
$ java -jar app/build/libs/app-all.jar query --origin --proxy -l 5
Querying cluster ORIGIN
20 => "x{O$K<fm-cL
19 => Tk<kR=hKTC\B
18 => Y%Iff%n0Y=.$
17 => E\{-&`!&.3tY
16 => uC\_LPRYb9L)
Querying cluster PROXY
20 => "x{O$K<fm-cL
19 => Tk<kR=hKTC\B
18 => Y%Iff%n0Y=.$
17 => E\{-&`!&.3tY
16 => uC\_LPRYb9L)
```

Note that the Origin data is different from the data inserted independently in Target, and that the ZDM proxy returns the same data as Origin. These results confirm that the proxy is returning data from the origin cluster (and appears to be doing so correctly).

Now, let's insert 10 rows through the ZDM proxy:
```
$ java -jar app/build/libs/app-all.jar insert --proxy -c 10
Inserting 10 new rows into cluster PROXY
```
The proxy always routes all writes to both clusters, so this time the same data has been inserted into both clusters. We can verify this again by reading the last five rows directly from Origin and from Target, as well as from the ZDM proxy, to ensure that all three result sets are the same:
```
$ java -jar app/build/libs/app-all.jar query --origin --target --proxy -l 5
Querying cluster ORIGIN
30 => Izwypsh0ec8X
29 => Z/%(I0IglMQ\
28 => &2DAqo<"~B:D
27 => _oCXWBB|Mp+p
26 => >u-aK,bW%3[1
Querying cluster TARGET
30 => Izwypsh0ec8X
29 => Z/%(I0IglMQ\
28 => &2DAqo<"~B:D
27 => _oCXWBB|Mp+p
26 => >u-aK,bW%3[1
Querying cluster PROXY
30 => Izwypsh0ec8X
29 => Z/%(I0IglMQ\
28 => &2DAqo<"~B:D
27 => _oCXWBB|Mp+p
26 => >u-aK,bW%3[1
```

These results confirm that writes to the proxy are successfully propagated to both Origin and Target.
