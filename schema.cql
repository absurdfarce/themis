CREATE KEYSPACE themis WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1};
CREATE TABLE themis.keyvalue (key int PRIMARY KEY, value text);