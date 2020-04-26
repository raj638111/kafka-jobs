

scala -classpath target/scala-2.11/hashtag_trends-assembly-0.1.0-SNAPSHOT.jar com.charter.trends.Trends --input tweets --output t1

sbt avro:generate
--

create keyspace
  charter
with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
use charter;

drop table tweets;
create table tweets(
  tweet text,
  tstamp text,
  hashtag text,
  primary key (tweet)
);

drop table trends;
create table trends(
  tstamp timestamp,
  hashtag text,
  count bigint,
  primary key (tstamp, hashtag)
);
select * from trends;

drop table trends_bytag;
create table trends_bytag(
  hashtag text,
  tstamp timestamp,
  count bigint,
  primary key (hashtag, tstamp)
) with clustering order by (tstamp desc);
select * from trends_bytag;

drop materialized view charter.trends_bycount;
create materialized view trends_bycount as
  select hashtag from trends
  where tstamp is not null and count is not null
  and hashtag is not null
  primary key(tstamp, count, hashtag)
  with clustering order by (count desc);
select * from charter.trends_bycount;


drop table charter.trends_tstamp;
create table charter.trends_tstamp (
  dummy text,
  tstamp text,
  primary key (dummy, tstamp)
) WITH CLUSTERING ORDER BY (tstamp DESC);
select * from charter.trends_tstamp;

kafka-avro-console-consumer \
    --bootstrap-server 127.0.0.1:9092 \
    --property schema.registry.url=http://127.0.0.1:8081 \
    --from-beginning \
    --topic avrotest

kafka-console-consumer.sh --bootstrap-server 127.0.0.1:9092 \
  --from-beginning \
  --formatter kafka.tools.DefaultMessageFormatter \
  --property print.key=true \
  --property print.value=true \
  --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer \
  --topic i1


desc tables; SELECT view_name FROM system_schema.views;

 view_name
-------------------
 trends_countorder

drop materialized view charter.trends_bycount;

