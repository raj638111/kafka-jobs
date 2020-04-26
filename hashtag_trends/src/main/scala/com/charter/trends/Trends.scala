package com.charter.trends

import java.time.Duration
import java.util.Properties
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.charter.log.CustomLogger
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.log4j.Logger
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.kstream.TimeWindows

/**
 * Kafka stream using High Level DSL
 */
object Trends {

  val log: Logger = CustomLogger.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    val param = Param().parse(args)
    import Serdes._
    val configs = prop()
    val builder = new StreamsBuilder
    val input: KStream[String, String] = builder.stream[String, String](param.input)
    val rgx = "#[a-zA-Z0-9]+".r
    val res = input.map{ case (tweet, tstamp) =>
      log.info(s"tweet -> $tweet, tstamp -> $tstamp")
      val hashtag = rgx.findFirstIn(tweet) match {
        case bind@Some(hashTag) => hashTag
        case None => ""
      }
      (hashtag, tstamp)
    }
    val res2 = res.groupBy{ case (hashtag, tstamp) =>
      s"${hashtag}_${tstamp}"
    }
    //val res3 = res2.count()
    // 1 minute window
    val windowSizeMs = TimeUnit.MINUTES.toMillis(1);
    val res4 = res2
        .windowedBy(TimeWindows.of(windowSizeMs))
        .count()
    val res5 = res4.toStream
    val res6 = res5.map{ case (key, value) =>
      (key.key(), value)
    }
    res6.to(param.output)
    val streams: KafkaStreams = new KafkaStreams(builder.build(), configs)
    try {
      streams.start()
    } catch {
      case x: Throwable =>
        log.error("Issue with starting")
        System.exit(1)
    }
    sys.ShutdownHookThread {
      log.info("end")
      streams.close(Duration.ofSeconds(20))
    }
    log.info("end1")
  }

  def prop(): Properties = {
    val p = new Properties()
    p.put(StreamsConfig.APPLICATION_ID_CONFIG, "trend4")
    p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
    //p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0")
    p.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")
    //p.put("num.stream.threads", "1")
    //p.put("max.poll.records", "2")
    p
  }
}

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

