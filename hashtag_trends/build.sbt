name := "hashtag_trends"

scalaVersion := "2.11.8"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

//javaSource in AvroConfig := baseDirectory.value / "target"
//stringType in AvroConfig := "String"
//sourceDirectory in AvroConfig := baseDirectory.value / "src/main/resources/tweet.avsc"

val typesafe = ExclusionRule(organization = "com.typesafe")

libraryDependencies ++= Seq(
		"org.apache.kafka" %% "kafka-streams-scala" % "2.3.1" exclude("org.rocksdb","rocksdbjni")
	,	"log4j" % "log4j" % "1.2.17"
 	, "org.rocksdb" % "rocksdbjni" % "6.4.6"
	, "com.github.scopt" %% "scopt" % "3.2.0"
  , "com.datastax.oss" % "java-driver-core" % "4.2.0"
	//, "com.typesafe" % "config" % "1.4.0"
  , "com.datastax.oss" % "java-driver-query-builder" % "4.2.0"
  , "com.datastax.oss" % "java-driver-mapper-runtime" % "4.2.0" 
	//, "io.confluent" % "kafka-streams-avro-serde" % "5.4.1"
	//, "org.apache.avro" % "avro" % "1.9.2"
).map(_.exclude("com.typesafe", "config")) //:+ "com.typesafe" % "config" % "1.3.4"
//).map(_.excludeAll(typesafe)) //:+ "com.typesafe" % "config" % "1.4.0"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 //case x if x.contains("com/typesafe") => MergeStrategy.rename
 case x => MergeStrategy.first
}

