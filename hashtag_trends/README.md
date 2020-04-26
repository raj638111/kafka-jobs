
# About

This project contains the KSTream that computes Hashtag trend count for tweets & stores the result in Kafka topic as well as in Cassandra table

# Prerequisites: Install Kafka & Cassandra

Ensure both Kafka & Cassandra are installed with
1. `Kafka` Bootstrap server available on Port 9092 (Default)
2. `Cassandra` server available on port 9042 (Port)  


# Prerequisites: DDL (Cassandra)

### **Create KeySpace**

```
create keyspace charter
with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};  
```

### **Create all tables**

```
drop table if exists charter.tweets;
create table charter.tweets(
  tweet text,
  tstamp text,
  hashtag text,
  primary key (tweet)
);
```

```
drop table if exists charter.trends;
create table trends(
  tstamp timestamp,
  hashtag text,
  count bigint,
  primary key (tstamp, hashtag)
);
```
```
drop materialized view charter.trends_bycount;
create materialized view trends_bycount as
  select hashtag from charter.trends
  where tstamp is not null and count is not null
  and hashtag is not null
  primary key(tstamp, count, hashtag)
  with clustering order by (count desc);
select * from charter.trends_bycount;
```

```
drop table if exists charter.trends_bytag;
create table charter.trends_bytag(
  hashtag text,
  tstamp timestamp,
  count bigint,
  primary key (hashtag, tstamp)
) with clustering order by (tstamp desc);
select * from trends_bytag;
```

```
drop table if exists charter.trends_tstamp;
create table charter.trends_tstamp (
  dummy text,
  tstamp text,
  primary key (dummy, tstamp)
) WITH CLUSTERING ORDER BY (tstamp DESC);
```

# Prerequisites: Kafka Topics

```
kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --partitions 3 \
--replication-factor 1 --topic tweets


kafka-topics.sh --zookeeper 127.0.0.1:2181 --create --partitions 3 \
--replication-factor 1 --topic trends
```

In case if needed to delete the topics, use the following command,

```
kafka-topics.sh --zookeeper 127.0.0.1:2181  --delete --topic tweets
kafka-topics.sh --zookeeper 127.0.0.1:2181  --delete --topic trends
```

# How to Compile?

From `$PROJECT_HOME/hashtag_trends` ,

```
sbt clean assembly
```

produces jar,

```
$PROJECT_HOME/hashtag_trends/target/scala-2.13/hashtag_trends-assembly-0.1.0-SNAPSHOT.jar
```

# How to execute?

From `$PROJECT_HOME/hashtag_trends` run,

```
scala -classpath target/scala-2.13/hashtag_trends-assembly-0.1.0-SNAPSHOT.jar com.charter.trends.Trends --input tweets --output trends

where 
--input is input topic for the application
--trends is the output topic for the application
```

# How to read Kafka topics?

```
kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092   \
--from-beginning   --formatter kafka.tools.DefaultMessageFormatter \
--property print.key=true   --property print.value=true   \
--property \
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
--property \
value.deserializer=org.apache.kafka.common.serialization.StringDeserializer  \
--topic tweets

kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092   \
--from-beginning   --formatter kafka.tools.DefaultMessageFormatter \
--property print.key=true   --property print.value=true \
--property \
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer   \
--property \
value.deserializer=org.apache.kafka.common.serialization.LongDeserializer   \
--topic trends
```

# How to list tables & views in Cassandra?

```
use charter;
desc tables; 
SELECT view_name FROM system_schema.views;
```