# Themis
A tool to make sure the scales [stay balanced](https://en.wikipedia.org/wiki/Themis)

# How Do You Build It
Build is implemented via [Gradle](https://gradle.org/).  You'll want to build a shadow JAR unless you want to manage dependencies some other way:

> ./gradlew shadowJar

# Overview
Themis is intended to simplify the process of querying a pair of Cassandra servers fronted by a CQL-compatible proxy which might be sending reads or writes to one or both of those clusters. The goal is to validate proper functioning of the proxy by providing an interactive way to write random sample data to one of those endpoints (either of the servers or the proxy directly) and then query those same endpoints.

In this example we will be using the [ZDM Proxy](https://github.com/datastax/zdm-proxy), which routes all requests as follows:
- Writes are always sent to both Origin and Target.
- Reads are sent to either Origin or Target, depending on how the proxy is configured.

For more information on the ZDM Proxy, please refer to its README and [official documentation](https://docs.datastax.com/en/astra-serverless/docs/migrate/introduction.html)

# How Is It Configured?
Via a YAML file called `.themis.yaml` and placed in your home directory.

Here's a sample for a test where the Origin cluster and the ZDM Proxy run locally, and Target is an AstraDB cluster:

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

In this case my Origin cluster is a local Cassandra install while my Target is an instance on [DataStax AstraDB](https://astra.datastax.com/).  The proxy is also installed locally but since it must proxy to AstraDB as well as my local instance Astra credentials are required here as well.

If your config contains an "scb" key it's assumed to represent an AstraDB-managed instance; in this case only the "username" and "password" keys will be used in addition to the secure connect bundle identified by "scb".

Here's another example to connect to a more general deployment, where the Origin cluster and the ZDM proxy run on dedicated instances and Target is still an AstraDB cluster:

```
origin:
  address: 191.100.20.210
  port: 9042
  localDc: my_origin_dc
  username: my_origin_username
  password: my_origin_password
target:
  scb: /my/astra/secure-connect-database.zip
  username: my_astra_clientid
  password: my_astra_secret
proxy:
  address: 172.13.10.78
  port: 9042
  localDc: my_origin_dc  
  username: my_astra_clientid
  password: my_astra_secret
```

Note that in this case Origin has internal authentication enabled, so I need to pass valid credentials for it. Also, the proxy's local datacenter name is the same as Origin's local datacenter.

And finally, here is an example where neither cluster is an AstraDB cluster and both clusters require authentication:
```
origin:
  address: 191.100.20.210
  port: 9042
  localDc: my_origin_dc
  username: my_origin_username
  password: my_origin_password
target:
  address: 191.200.35.170
  port: 9042
  localDc: some_other_dc
  username: my_target_username
  password: my_target_password
proxy:
  address: 172.13.10.78
  port: 9042
  localDc: my_origin_dc  
  username: my_target_username
  password: my_origin_password
```

# What Can It Do?
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
