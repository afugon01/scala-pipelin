name := "Tuberia_Flink_Llave"

version := "0.1"

scalaVersion := "2.12.12"

idePackagePrefix := Some("flink")




///KAFKA
// https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.5.0"




////FLINK
// https://mvnrepository.com/artifact/org.apache.flink/flink-connector-kafka
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % "1.11.2"

// https://mvnrepository.com/artifact/org.apache.flink/flink-scala
libraryDependencies += "org.apache.flink" %% "flink-scala" % "1.11.2"

// https://mvnrepository.com/artifact/org.apache.flink/flink-streaming-scala
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.11.2"

// https://mvnrepository.com/artifact/org.apache.flink/flink-clients
libraryDependencies += "org.apache.flink" %% "flink-clients" % "1.11.2"

// https://mvnrepository.com/artifact/org.apache.flink/flink-connector-elasticsearch7
libraryDependencies += "org.apache.flink" %% "flink-connector-elasticsearch7" % "1.11.2"




//HTTP HOSTS
//https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.13"


//EJECUTABLES
mainClass in assembly := some("flink.Flink_ES_Llave")
assemblyJarName := "Esearch.jar"
