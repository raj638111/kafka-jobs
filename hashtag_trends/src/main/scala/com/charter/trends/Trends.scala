package com.charter.trends

import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.Properties

import com.charter.log.CustomLogger
import com.datastax.oss.driver.api.core.CqlSession
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.log4j.Logger

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serde

object Trends {

  val log: Logger = CustomLogger.getLogger(this.getClass.getName)

  //val session = CqlSession.builder().build()



  def main(args: Array[String]): Unit = {
    import Serdes._
    val configs: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "new")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      //p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass().getName())
      //p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
      //  classOf[SpecificAvroSerde[_ <: SpecificRecord]])
      p
    }
    //implicit val ser: Serde[Tweet] = new SpecificAvroSerde[Tweet]
    val rgx = "#[a-zA-Z0-9]+".r
    val builder = new StreamsBuilder
    // Create stream from input topic
    val input: KStream[String, String] = builder.stream[String, String]("tweets")
    val result = input.map{ case (tweet: String, tstamp: String) =>
      log.info(s"tweet -> $tweet, tstamp -> $tstamp")
      (tweet, tstamp)
    }
    result.to("result")

    val streams: KafkaStreams = new KafkaStreams(builder.build(), configs)
    streams.start()
    log.info("Topology -> " + streams.toString)
    sys.ShutdownHookThread {
      log.info("In shutdown hook")
      streams.close(Duration.ofSeconds(20))
    }
    log.info("end")
  }

}


/*
      log.info(s"tweet -> $tweet, tStamp -> $tStamp")
      //val tInfo = new Tweet()
      rgx.findFirstIn(tweet) match {
        case bind @ Some(hashTag) =>
          log.info(s"HashTag -> " + hashTag)
          //tInfo.setTweet(tweet)
          //(tweet, tInfo)
          (tweet, tweet)
        case None =>
          log.warn("No HashTag")
          //tInfo.setTweet(tweet)
          //(tweet, tInfo)
          (tweet, tweet)

 */