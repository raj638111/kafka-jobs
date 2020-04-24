package com.charter.trends

import java.net.InetSocketAddress
import java.time.Duration
import java.util.Properties

import com.charter.log.CustomLogger
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.cql.{ResultSet, Row}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.log4j.Logger


/**
 * Kafka stream using High Level DSL
 */
object Trends {

  val log: Logger = CustomLogger.getLogger(this.getClass.getName)

  def main(args: Array[String]): Unit = {
    //val cp = System.getProperty("java.class.path")
    //val sep = System.getProperty("path.separator")
    //cp.split(sep).foreach(log.info(_))
    /*val session = CqlSession
      .builder()
      .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
      .build() */

    val param = Param().parse(args)
    import Serdes._
    val configs: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, "trend2")
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
      p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      p
    }
    val builder = new StreamsBuilder
    val input: KStream[String, String] = builder.stream[String, String](param.input)
    input.map{ case (tweet, tstamp) =>
      log.info(s"tweet -> $tweet, tstamp -> $tstamp")
      /*val rs = session.execute("select release_version from system.local");              // (2)
      val row = rs.one();
      log.info(row.getString("release_version"))*/
      (tweet, tstamp)
    }.to(param.output)
    val streams: KafkaStreams = new KafkaStreams(builder.build(), configs)
    streams.start()
    sys.ShutdownHookThread {
      log.info("end")
      streams.close(Duration.ofSeconds(20))
    }
    log.info("end1")
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
      opt[String]("output") required() action { (x, c) =>
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

