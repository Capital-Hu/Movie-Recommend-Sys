package com.bigdata.dataloader

import com.bigdata.scala.model.{ESConfig, MongoConfig, Movie, MovieRating, Tag}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import com.bigdata.java.model.Constant.{ES_DRIVER_CLASS, ES_INDEX, ES_TYPE, MONGO_DATABASE, MONGO_DRIVER_CLASS, MONGO_MOVIE_COLLECTION, MONGO_RATING_COLLECTION, MONGO_TAG_COLLECTION}

import java.net.InetAddress

object DataLoader {

  def main(args: Array[String]): Unit = {

    if (args.length != 7) {
      System.err.println("Usage: java - jar dataloader.jar <mongo_server> <es_http_server> <es_trans_server> <es_cluster_name> <movie_data_path> <rating_data_path> <tag_data_path>\n"
        + "  <mongo_server> is the mongo server to connect, eg. hadoop100:27017\n"
        + "  <es_http_server> is the elasticsearch http servers to connect, eg. hadoop100:9200,hadoop101:9200\n"
        + "  <es_trans_server> is the elasticsearch transport servers to connect, eg. hadoop100:9300,hadoop101:9300\n"
        + "  <es_cluster_name> is the elasticsearch cluster name, eg. es-cluster\n"
        + "  <movie_data_path>\n"
        + "  <rating_data_path>\n"
        + "  <tag_data_path>\n\n")
      System.exit(1)
    }
    System.exit(1)
    val mongo_server = args(0)
    val es_http_server = args(1)
    val es_trans_server = args(2)
    val es_cluster_name = args(3)
    val movie_data_path = args(4)
    val rating_data_path = args(5)
    val tag_data_path = args(6)

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> ("mongodb://hadoop100:27017/"+MONGO_DATABASE),
      "mongo.db" -> "recommender",
      "es.httpHosts" -> "hadoop100:9200",
      "es.transportHosts" -> "hadoop100:9300",
      "es.index" -> ES_INDEX,
      "es.cluster.name" -> "es-cluster"
    )
    // create SparkConf
    val sparkConf = new SparkConf().setAppName("DataLoader").setMaster(config.get("spark.cores").get)

    // create SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // load dataset
    val movieRDD = spark.sparkContext.textFile(movie_data_path)
    val movieDF = movieRDD.map(item =>{
      val attr = item.split("\\^")
      Movie(attr(0).toInt, attr(1).trim, attr(2).trim, attr(3).trim, attr(4).trim, attr(5).trim, attr(6).trim, attr(7).trim, attr(8).trim, attr(9).trim )
    }).toDF()
    val ratingRDD = spark.sparkContext.textFile(rating_data_path)
    val ratingDF = ratingRDD.map(item => {
      val attr = item.split(",")
      MovieRating(attr(0).toInt, attr(1).toInt, attr(2).toDouble, attr(3).toInt)
    }).toDF()
    val tagRDD = spark.sparkContext.textFile(tag_data_path)
    val tagDF = tagRDD.map(item => {
      val attr = item.split(",")
      Tag(attr(0).toInt, attr(1).toInt, attr(2).trim, attr(3).toInt)
    }).toDF()

    implicit val mongoConfig = MongoConfig(config.get("mongo.uri").get, config.get("mongo.db").get )
    // save to MongoDB
    storeDataInMongoDB(movieDF, ratingDF, tagDF)

    // process Tag
    import org.apache.spark.sql.functions._
    /**
     *  MID, Tags
     *  1     tag1|tag2|tag3....
     */
    val newTag = tagDF.groupBy($"mid").agg(concat_ws("|",collect_set($"tag")).as("tags")).select("mid","tags")
    // integrate
    val movieWithTagsDF = movieDF.join(newTag, Seq("mid", "mid"), "left")

    // declare an implicit esConfig
    implicit val esConfig = ESConfig(config.get("es.httpHosts").get,config.get("es.transportHosts").get, config.get("es.index").get, config.get("es.cluster.name").get)
    // save to ES
    storeDataInES(movieWithTagsDF)

    // close Spark
    spark.stop()
  }
  def storeDataInMongoDB(movieDF:DataFrame, ratingDF:DataFrame, tagDF:DataFrame)(implicit mongoConfig: MongoConfig): Unit = {
    // new a connection
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))
    // if mongodb has db, should remove it
    mongoClient(mongoConfig.db)(MONGO_MOVIE_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGO_RATING_COLLECTION).dropCollection()
    mongoClient(mongoConfig.db)(MONGO_TAG_COLLECTION).dropCollection()
    // write data into MongoDB
    movieDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_MOVIE_COLLECTION)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()

    ratingDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_RATING_COLLECTION)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()

    tagDF
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_TAG_COLLECTION)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()
    // index db
    mongoClient(mongoConfig.db)(MONGO_MOVIE_COLLECTION).createIndex(MongoDBObject("mid" -> 1))
    mongoClient(mongoConfig.db)(MONGO_RATING_COLLECTION).createIndex(MongoDBObject("uid" -> 1))
    mongoClient(mongoConfig.db)(MONGO_RATING_COLLECTION).createIndex(MongoDBObject("mid" -> 1))
    mongoClient(mongoConfig.db)(MONGO_TAG_COLLECTION).createIndex(MongoDBObject("uid" -> 1))
    mongoClient(mongoConfig.db)(MONGO_TAG_COLLECTION).createIndex(MongoDBObject("mid" -> 1))
    // close connection
    mongoClient.close()
  }

  def storeDataInES(movieDF:DataFrame)(implicit eSConfig: ESConfig): Unit = {
    // create a config
    val settings:Settings = Settings.builder().put("cluster.name", eSConfig.clustername).build()

    // create an ES client
    val esClient = new PreBuiltTransportClient(settings)

    // add TransportHosts to esClient
    val REGEX_HOST_PORT = "(.+):(\\d+)".r
    eSConfig.transportHosts.split(",").foreach{
      case REGEX_HOST_PORT(host:String,port:String) => {
        esClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host),port.toInt))
      }
    }

    // clear original data
    if(esClient.admin().indices().exists(new IndicesExistsRequest(eSConfig.index)).actionGet().isExists){
      esClient.admin().indices().delete(new DeleteIndexRequest(eSConfig.index))
    }

    esClient.admin().indices().create(new CreateIndexRequest(eSConfig.index))

    // write data into ES
    movieDF
      .write
      .option("es.nodes",eSConfig.httpHosts)
      .option("es.http.timeout","100m")
      .option("es.mapping.id","mid")
      .mode("overwrite")
      .format(ES_DRIVER_CLASS)
      .save(eSConfig.index+"/"+ES_TYPE)
  }
}




















