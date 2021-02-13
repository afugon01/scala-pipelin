package flink

import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}

import java.time.LocalDateTime
import java.util.Properties



object Kafka_Productor {


  val bootstrapServers = "kafka:9093"
  val groupId = "kafka-example"
  val topics = "flink-llave"

  val props: Properties = {
    val p = new Properties()
    p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    p.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    p.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    // optional configs
    // See the Kafka Producer tutorial for descriptions of the following
    p.put(ProducerConfig.ACKS_CONFIG, "all")

    p
  }



  def main(args: Array[String]): Unit = {


    val callback = new Callback
    {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if (exception!=null)
          exception.printStackTrace()
        else
          println(" ,Offsets enviados: "+metadata.offset().toString)
      }
    }



    val producer = new KafkaProducer[String, String](props)
    var contador=0
    var variable=""
    var tupla_string=""
    var valor=0

    val generador= new scala.util.Random


    while(true)
    {

      //T con milisegundos
      val fecha_actual=LocalDateTime.now()


      //mira si el contador es par o impar
      if (contador % 2==0) {

        //genera valores de 25 a 32
        valor = 25 + generador.nextInt(( 32 - 25) + 1)
        variable="temp"

      } else {

        valor= 60 + generador.nextInt(( 70 - 60) + 1)
        variable="hume"
      }


      tupla_string=fecha_actual+";"+variable+";"+valor

      println(tupla_string)

      contador+=1

      //producer.send(new ProducerRecord(topics, s"key ${k}", "oh the value! "+k))

      //asincrono con callback
      producer.send(new ProducerRecord(topics,tupla_string), callback)

      Thread.sleep(30000)
    }


    producer.close()

  }




}
