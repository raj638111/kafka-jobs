package com.charter.trends

import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit
import com.charter.log.CustomLogger
import com.datastax.oss.driver.api.core.CqlSession
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.{KStream, _}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.log4j.Logger
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.kstream.TimeWindows
import scala.concurrent.duration._

object Trends {

  val log: Logger = CustomLogger.getLogger(this.getClass.getName)

  // Caffeine cached: Used to filter out duplicate tweets
  val cache: Cache[String, String] = Scaffeine()
    .recordStats()
    .expireAfterWrite(1.hour) // Cached data expires after this time
    .maximumSize(2000) // Total amount of records allowed to cached
    .build[String, String]()

  // Cassandra Tables
  val TABLE_TRENDS = "charter.trends"
  val TABLE_TRENDS_TSTAMP = "charter.trends_tstamp"
  val TABLE_TRENDS_HASHTAG = "charter.trends_bytag"

  /**
   * Yep. Application starts here...
   * @param args Command line arguments
   */
  def main(args: Array[String]): Unit = {
    val session: CqlSession = CqlSession.builder().build()
    val param = Param().parse(args)
    import Serdes._
    val configs = prop()
    val builder = new StreamsBuilder
    val input: KStream[String, String] = builder.stream[String, String](param.input)
    val output = transform(input, session)
    output.to(param.output)
    val streams: KafkaStreams = new KafkaStreams(builder.build(), configs)
    try {
      streams.start()
    } catch {
      case x: Throwable =>
        log.error("Issue with starting")
        System.exit(1)
    }
    sys.ShutdownHookThread {
      log.info("Closing cassandra session")
      session.close()
      log.info("Closing stream")
      streams.close(Duration.ofSeconds(20))
    }
    log.info("end1")
  }

  /**
   * All the stream processors are grouped here
   * @param input Stream from the source topic
   * @param session To write data into Cassandra table
   * @return Final stream (that will be written to output topic)
   */
  def transform(input: KStream[String, String], session: CqlSession) = {
    import Serdes._
    val mapped = input.map{ case (hashtag, tweetNtstamp) =>
      log.info(s"hashtag -> $hashtag, tweetntstamp -> $tweetNtstamp")
      val (tweet, tstamp) = tweetNtstamp.split("[|]{1}![|]{1}") match {
        case x if x.size == 2 => (x(0).trim, x(1).trim)
        case _ => throw new UnsupportedOperationException(s"$tweetNtstamp not splittable")
      }
      (s"${hashtag}|!|${tstamp}", s"${hashtag}|!|${tweet}")
    }
    val filtered = mapped.filter{ case (_, hashNtweet) =>
      cache.getIfPresent(hashNtweet)match {
        case Some(_) =>
          log.warn("NOT Allowed. Already in cache -> " + hashNtweet)
          false
        case None =>
          log.info("Allowed. Adding to cache -> " + hashNtweet)
          cache.put(hashNtweet, "")
          true
      }
    }
    val grouped = filtered.groupBy{ case (hashNtstamp, _) =>
      hashNtstamp
    }
    // Create 5 minute Tumble Window
    val windowSizeMs = TimeUnit.MINUTES.toMillis(5);
    val windowedCount = grouped
      .windowedBy(TimeWindows.of(windowSizeMs))
      .count()
    val result = windowedCount.toStream.map{ case (key, value) =>
      (key.key(), value)
    }
    val peeked = result.peek{ case (key, value) =>
      val (hash, tstamp) = key.split("[|]{1}![|]{1}") match {
        case x => (x(0), x(1))
      }
      insertToTrends(tstamp, value, hash, session)
      insertToTrendsByTag(tstamp, value, hash, session)
      insertToTstamp(tstamp, session)
    }
    peeked
  }

  def insertToTrends(tstamp: String, value: Long, hash: String, session: CqlSession) = {
    val query = s"insert into ${TABLE_TRENDS}(tstamp, count, hashtag) " +
      s"values('$tstamp', $value, '$hash')"
    log.info("Executing query -> " + query)
    session.execute(query)
    log.info("Done executing query -> " + query)
  }

  def insertToTrendsByTag(tstamp: String, value: Long, hash: String, session: CqlSession) = {
    val query = s"insert into ${TABLE_TRENDS_HASHTAG} (hashtag, tstamp, count) " +
      s"values('$hash', '$tstamp', $value)"
    log.info("Executing query -> " + query)
    session.execute(query)
    log.info("Done executing query -> " + query)
  }

  def insertToTstamp(tstamp: String, session: CqlSession): Unit = {
    val query = s"insert into ${TABLE_TRENDS_TSTAMP}(dummy, tstamp) " +
      s"values('-', '$tstamp')"
    log.info("Executing query -> " + query)
    session.execute(query)
    log.info("Done executing query -> " + query)
  }

  /**
   * Kafka specific properties
   * @return properties
   */
  def prop(): Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "trend4")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    //p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
    p
  }
}

/**
 * Command line arguments are stored here
 * @param input Input topic
 * @param output Output topic
 */
case class Param(
  input: String = null,
  output: String = null){

  val log: Logger = CustomLogger.getLogger(this.getClass.getName.dropRight(1))

  def parse(args: Array[String]): Param = {
    val parser = new scopt.OptionParser[Param]("ParquetTest") {
      head("scopt", "3.x")
      opt[String]("input").required().action { (x, c) =>
        c.copy(input = x)
      }
      opt[String]("output").required().action { (x, c) =>
        c.copy(output = x)
      }
    }
    parser.parse(args, Param()) match {
      case Some(param) => param
      case _ =>
        log.error("Bad arguments")
        throw new Exception
    }
  }
}

