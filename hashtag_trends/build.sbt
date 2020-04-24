name := "hashtag_trends"

scalaVersion := "2.11.8"

excludeDependencies ++= Seq(
  ExclusionRule("com.typesafe", "config")
  , ExclusionRule("org.rocksdb", "rocksdbjni")
)

libraryDependencies ++= Seq(
		"org.apache.kafka" %% "kafka-streams-scala" % "2.3.1"
	, "com.typesafe" % "config" % "1.4.0"
	,	"log4j" % "log4j" % "1.2.17"
 	, "org.rocksdb" % "rocksdbjni" % "6.4.6"
	, "com.github.scopt" %% "scopt" % "3.2.0"
  , "com.datastax.oss" % "java-driver-core" % "4.2.0"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

