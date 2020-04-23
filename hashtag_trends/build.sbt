name := "hashtag_trends"

scalaVersion := "2.12.10"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

javaSource in AvroConfig := baseDirectory.value / "target"
stringType in AvroConfig := "String"
sourceDirectory in AvroConfig := baseDirectory.value / "src/main/resources/tweet.avsc"


libraryDependencies ++= Seq(
		"org.apache.kafka" %% "kafka-streams-scala" % "2.3.1" //exclude("org.rocksdb","rocksdbjni")
	,	"log4j" % "log4j" % "1.2.17"
  , "com.datastax.oss" % "java-driver-core" % "4.5.0"
  , "com.datastax.oss" % "java-driver-query-builder" % "4.5.0"
  , "com.datastax.oss" % "java-driver-mapper-runtime" % "4.5.0"
	, "io.confluent" % "kafka-streams-avro-serde" % "5.4.1"
	, "org.apache.avro" % "avro" % "1.9.2"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

