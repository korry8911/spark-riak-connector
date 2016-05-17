import sbt._
import Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

object BuildSettings {

  val scala210Version = "2.10.5"
  val scala211Version = "2.11.8"

  val groupId = "com.basho.riak"
  val versionStr = "1.5.1-SNAPSHOT"

  val guavaVersion = "14.0.1"
  val jacksonVersion = "2.7.3"
  val junitVersion = "4.12"
  val powermockVersion = "1.6.4"
  val riakClientVersion = "1.5.1-SNAPSHOT"
  val scalaLoggingVersion = "2.1.2"
  val scalaTestVersion = "3.0.0-M15"
  val sparkVersion = "1.6.1"

  // Logging
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
  lazy val scalaLogging =  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % scalaLoggingVersion
  // Testing
  lazy val hamcrest = "org.hamcrest" % "hamcrest-library" % "1.3"
  lazy val jsonUnit = "net.javacrumbs.json-unit" % "json-unit" % "1.5.1"
  lazy val junit = "junit" % "junit" % junitVersion
  lazy val mockito = "org.mockito" % "mockito-core" % "1.10.19"
  lazy val powermockJUnit4 = "org.powermock" % "powermock-module-junit4" % powermockVersion
  lazy val powermockMockito = "org.powermock" % "powermock-api-mockito" % powermockVersion
  lazy val scalatest = "org.scalatest" %% "scalatest" % scalaTestVersion
  // Spark
  lazy val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  lazy val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  lazy val sparkSql = "org.apache.spark" %% "spark-sql" % sparkVersion
  // Riak
  lazy val riakClient = "com.basho.riak" % "dataplatform-riak-client" % riakClientVersion
  // Jackson JSON
  lazy val jackson = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  // Google Guava
  lazy val guava = "com.google.guava" % "guava" % guavaVersion

  lazy val commonSettings = Seq(
    exportJars := true,
    crossScalaVersions := Seq(scala210Version, scala211Version),
    scalaVersion := scala211Version,

    organization := groupId,
    version := versionStr,

    parallelExecution in Test := false,

   publishMavenStyle := true,
    publishTo := {
      val artifactory = "https://basholabs.artifactoryonline.com/basholabs"
      if (version.value.trim.endsWith("SNAPSHOT"))
        Some("Artifactory Realm" at s"$artifactory/libs-snapshot-local")
      else
        Some("Artifactory Realm" at s"$artifactory/libs-release-local")
    },
    pomIncludeRepository := { _ => false },
    pomExtra := (
                <url>https://github.com/jbrisbin/akka-http-docker/</url>
                  <licenses>
                    <license>
                      <name>Apache-2.0</name>
                      <url>https://opensource.org/licenses/Apache-2.0</url>
                      <distribution>repo</distribution>
                    </license>
                  </licenses>
                  <scm>
                    <url>git@github.com:jbrisbin/akka-http-docker.git</url>
                    <connection>scm:git:git@github.com:jbrisbin/akka-http-docker.git</connection>
                  </scm>
                  <developers>
                    <developer>
                        <name>Sergey Galkin</name>
                        <email>sgalkin@basho.com</email>
                        <organization>Basho Technologies, Inc</organization>
                        <organizationUrl>http://www.basho.com</organizationUrl>
                    </developer>
                    <developer>
                        <name>Oleg Rocklin</name>
                        <email>orocklin@basho.com</email>
                        <organization>Basho Technologies, Inc</organization>
                        <organizationUrl>http://www.basho.com</organizationUrl>
                    </developer>
                    <developer>
                      <id>jbrisbin</id>
                      <name>Jon Brisbin</name>
                      <url>http://jbrisbin.com</url>
                    </developer>
                  </developers>
                ),

    resolvers ++= Seq(
      Resolver.bintrayRepo("hseeberger", "maven")
    ),

    libraryDependencies ++= {

      Seq(
        // Logging
        scalaLogging,

        // Testing
        junit % "test",
        hamcrest % "test",
        scalatest % "test",
        logback % "test"
      )
    },

    ivyScala := ivyScala.value map {
      _.copy(overrideScalaVersion = true)
    }
  )

}

object SparkRiakConnectorBuild extends Build {

  import BuildSettings._

  lazy val root = project.in(file("."))
    .aggregate(connector, connectorJava, test)

  lazy val connector = project.in(file("connector"))
    .dependsOn(test % "test")
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings: _*)
    .settings(
      name := "spark-riak-connector",

      libraryDependencies ++= {

        Seq(
          // Google Guava
          guava,

          // Jackson JSON
          jackson,

          // Riak
          riakClient,

          // Spark
          sparkCore,
          sparkSql,
          sparkStreaming,

          // Testing
          mockito % "test",
          powermockJUnit4 % "test",
          powermockMockito % "test",

          "com.novocode" % "junit-interface" % "0.11" % "test"
        )
      }
    )

  lazy val connectorJava = project.in(file("connector-java"))
    .dependsOn(connector, test % "test")
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings: _*)
    .settings(
      name := "spark-riak-connector-java"
    )


  lazy val test = project.in(file("test"))
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings: _*)
  .settings(
    name := "spark-riak-connector-test",

    libraryDependencies ++= {

      Seq(
        // Riak
        riakClient,

        // Spark
        sparkCore,
        sparkSql,
        sparkStreaming,

        // Testing
        junit,
        jsonUnit,
        scalatest,
        mockito,
        powermockJUnit4,
        powermockMockito
      )
    }
  )
}
