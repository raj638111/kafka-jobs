
name := "kafka-jobs"

scalaVersion := "2.11.8"

lazy val root = (project in file("."))
  .aggregate(hashtag_trends)

lazy val hashtag_trends = (project in file ("hashtag_trends"))


