package com.charter.log
import org.apache.log4j._

object CustomLogger {

  val mPattern = "%d{yy/MM/dd HH:mm:ss} | %p | %c | %M() | %L | %m%n"
  val mFileLoggerName = "FileLogger"
  Logger.getRootLogger.removeAllAppenders()
  setLogger(Logger.getRootLogger, Level.DEBUG, Level.INFO)
  val logLevel = System.getProperty("loglevel") match {
    case "debug" => Level.DEBUG
    case "info" => Level.INFO
    case _ => Level.INFO
  }
  setLogger(Logger.getLogger("com.charter"), Level.DEBUG, logLevel)
  setLogger(Logger.getLogger("org.apache.kafka"), Level.DEBUG, logLevel)

  def setLogger(logger: Logger, logLevel: Level, thresholdLevel: Level): Unit = {
    println("Setting Root Logger...") //scalastyle:ignore
    logger.setLevel(logLevel)
    logger.setAdditivity(false)
    setConsoleAppender(logger, thresholdLevel)
  }

  def getLogger(category: String): Logger = {
    Logger.getLogger(category)
  }

  def setConsoleAppender(logger: Logger, thresholdLevel: Level): Unit = {
    val console = new ConsoleAppender(); //create appender
    console.setLayout(new PatternLayout(mPattern));
    console.setThreshold(thresholdLevel);
    console.activateOptions();
    logger.addAppender(console)
  }

}
