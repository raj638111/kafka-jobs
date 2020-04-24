

scala -classpath target/scala-2.11/hashtag_trends-assembly-0.1.0-SNAPSHOT.jar \
com.charter.trends.Trends --input tweets --output t1


--

create keyspace
  charter
with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
use charter;


create table tweets(
  tweet text,
  tstamp text,
  hashtag text,
  primary key (tweet)
);

