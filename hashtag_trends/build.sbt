name := "hashtag_trends"

scalaVersion := "2.13.1"

resolvers += "confluent" at "https://packages.confluent.io/maven/"

javaSource in AvroConfig := baseDirectory.value / "target/generated"
stringType in AvroConfig := "String"
sourceDirectory in AvroConfig := baseDirectory.value / "src/main/resources/tweet.avsc"

unmanagedSourceDirectories in Compile += (baseDirectory( _ / "target/generated" )).value

excludeDependencies ++= Seq(
  ExclusionRule("com.typesafe", "config")
  , ExclusionRule("org.rocksdb", "rocksdbjni")
)

libraryDependencies ++= Seq(
		"org.apache.kafka" %% "kafka-streams-scala" % "2.5.0"
	, "com.github.scopt" %% "scopt" % "3.7.1"
	, "com.github.blemale" %% "scaffeine" % "3.1.0" % "compile"
	, "com.typesafe" % "config" % "1.4.0"
	,	"log4j" % "log4j" % "1.2.17"
 	, "org.rocksdb" % "rocksdbjni" % "6.4.6"
  , "com.datastax.oss" % "java-driver-core" % "4.2.0"
	//, "io.confluent" % "kafka-streams-avro-serde" % "5.4.1"
	, "com.github.ben-manes.caffeine" % "caffeine" % "2.8.1"
	, "com.github.ben-manes.caffeine" % "guava" % "2.8.1"
	, "com.github.ben-manes.caffeine" % "jcache" % "2.8.1"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

