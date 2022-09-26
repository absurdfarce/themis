# Themis
A tool to make sure the scales [stay balanced](https://en.wikipedia.org/wiki/Themis)

# How Do You Build It
Build is implemented via [Gradle](https://gradle.org/).  You'll want to build a shadow JAR unless you want to manage dependencies some other way:

> ./gradlew shadowJar

# Overview
Themis is inteded to simplify the process of querying a pair of Cassandra servers fronted by a CQL proxy which might be sending reads or writes to one or both of those clusters.  The goal is to validate proper functioning of the proxy by providing an interactive way to write random sample data to one of those endpoints (either of the servers or the proxy directly) and then query those same endpoints.

# How Is It Configured?
Via a YAML file located at ~/.themis.yaml.  A sample:

```
origin:
  address: 127.0.0.1
  port: 9042
  localDc: datacenter1
target:
  scb: /my/astra/secure-connect-database.zip
  username: myastraclientid
  password: myastrasecret
proxy:
  address: 127.0.0.1
  port: 14002
  localDc: datacenter1  
  username: myastraclientid
  password: myastrasecret
```

In this case my "origin" server is a local Cassandra install while my "target" is an instance on [DataStax Astra](https://astra.datastax.com/).  The proxy is also installed locally but since it must proxy to Astra as well as my local instance Astra credentials are required here as well.

If your config contains an "scb" key it's assumed to represent an Astra-managed instance; in this case only the "username" and "password" keys will be used in addition to the secure connect bundle identified by "scb".

# What Can It Do?
There are three commands available at the moment:

* schema - Creates the mock schema on the specified server
* insert - Add some amount of randomly generated data to the server
* query - Retrieve and display some amount of data from one (or more) of the servers

Each command includes detailed help, so this kind of thing might be useful:

```
$ java -jar app/build/libs/app-all.jar insert --help
Usage: themis insert [-hopt] [-c=<count>]
  -c, --count=<count>   Number of records to insert
  -h, --help            Show this help and exit
  -o, --origin          Execute the insertion against the origin
  -p, --proxy           Execute the insertion against the proxy
  -t, --target          Execute the insertion against the target
```

# Can You Give Me A More Complete Example?
Sure, why not?

```
$ java -jar app/build/libs/app-all.jar schema --origin -target                                                                                                                               
Creating schema on cluster ORIGIN 
Creating schema on cluster TARGET
Cluster TARGET is an Astra cluster, skipping keyspace creation (Astra keyspaces must be created through the Astra UI) 
$ java -jar app/build/libs/app-all.jar insert --origin --target -c 20
Inserting 20 new rows into cluster ORIGIN
Inserting 20 new rows into cluster TARGET
$ java -jar app/build/libs/app-all.jar query --target -l 5
Querying cluster TARGET
20 => tQWG\|\OmgZ5
19 => ]tM\Zk3EkoPD
18 => J${Sc.mT<Ji*
17 => ].b}M&J(y%1j
16 => m.D)q@^tyWqw
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

These results confirm that the proxy is returning data from the origin cluster (and appears to be doing so correctly).

```
$ java -jar app/build/libs/app-all.jar insert --proxy -c 10
Inserting 10 new rows into cluster PROXY
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

These results confirm that writes to the proxy are successfully propagated to both origin and target.
