package flink

import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.util.Collector
import org.apache.http.HttpHost
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.Requests

import java.time.LocalDateTime
import java.util.Properties

object Flink_ES_Llave {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(30000)

    //propiedades de kafka
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "kafka:9093")
    properties.setProperty("group.id", "test")

    //1. obtiene los datos de kafka
    val fuente_kafka: DataStream[String] = env.addSource(
      new FlinkKafkaConsumer[String]("flink-llave", new SimpleStringSchema(), properties))




    //2.divide por ; y lo mete en array, luego hace una tupla de cada valor, devuelve DataStream[Tuple2]
    val tupla_stream = fuente_kafka.map(value => {
      val arreglo_columnas = value.split(";")
      val tupla_tre= (arreglo_columnas(0), arreglo_columnas(1),arreglo_columnas(2).toInt)
      tupla_tre

    })


    //suma cada dos minutos valores por llave(temp o hume)
 /*   val keyed_stream=tupla_stream
      .keyBy(_._2)
      .timeWindow(Time.minutes(2))
      .sum(2)*/


    //3. establece como llave el segundo campo (temp,hume), sejecta cada dos min y saca promedio por llave
    val keyed_stream=tupla_stream
      .keyBy(_._2)
      .timeWindow(Time.minutes(2))
      .apply(

        //lo q entra, lo q sale son tuplas
        new WindowFunction[(String, String,Int), (String,String,Double), String, TimeWindow] {

          def apply(key: String, window: TimeWindow, input: Iterable[(String, String,Int)], out: Collector[(String,String,Double)]): Unit = {

            var contador = 0.0
            var suma=0.0
            var promedio=0.0

            //la fecha q se hace el promedio
            val fecha_actual=LocalDateTime.now()
            var variable=""

            //recorre lo q entra
            for (in <- input) {
              variable=in._2

              contador+=1
              suma+=in._3

              println(variable+" "+in._3)
            }

            promedio=suma/contador

            //sale la tupla con e promedio double
            val salida=(fecha_actual.toString,variable,promedio)

            out.collect(salida)
          }
        }


      )





    //4. ENVIO A ESEARCH EL PROMEDIO CADA DOS MINUTOS
    val httpHosts = new java.util.ArrayList[HttpHost]

    httpHosts.add(new HttpHost("esearch", 9200, "http"))
    //httpHosts.add(new HttpHost("10.2.3.1", 9200, "http"))

    //lo q entra es una tupla
    val esSinkBuilder = new ElasticsearchSink.Builder[(String,String,Double)](
      httpHosts, (element: (String,String,Double), ctx: RuntimeContext, indexer: RequestIndexer) => {

        val json = new java.util.HashMap[String, String]


        json.put("fecha", element._1)
        json.put("variable", element._2)
        json.put("promedio", element._3.toString)

        println("ENVIADO")

        val rqst: IndexRequest = Requests.indexRequest
          .index("flink-llave")
          .source(json)

        indexer.add(rqst)


      }
    )



    // configuration for the bulk requests; this instructs the sink to emit after every element, otherwise they would be buffered
    esSinkBuilder.setBulkFlushMaxActions(1)


    // finally, build and add the sink to the job's pipeline
    keyed_stream.addSink(esSinkBuilder.build)





    env.execute()

  }




}
