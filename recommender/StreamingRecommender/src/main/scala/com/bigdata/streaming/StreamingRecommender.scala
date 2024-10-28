package com.bigdata.streaming

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import com.bigdata.java.model.Constant._

import scala.collection.JavaConversions._

object ConnHelper extends Serializable{
  lazy val jedis = new Jedis("hadoop100")
  lazy val mongoClient = MongoClient(MongoClientURI("mongodb://hadoop100:27017/recommender"))
}

case class MongoConfig(uri:String,db:String)
//推荐
case class Recommendation(rid:Int, r:Double)

// 用户的推荐
case class UserRecs(uid:Int, recs:Seq[Recommendation])

//电影的相似度
case class MovieRecs(mid:Int, recs:Seq[Recommendation])


object StreamingRecommender {
  val MAX_USER_RATINGS_NUM = 20
  val MAX_SIM_MOVIES_NUM = 20

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://hadoop100:27017/recommender",
      "mongo.db" -> "recommender",
      "kafka.topic" -> "recommender"
    )

    val sparkConf = new SparkConf().setAppName("OnlineRecommender").setMaster(config("spark.cores"))
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val sc = spark.sparkContext

    val ssc = new StreamingContext(sc, Seconds(2))
    implicit val mongoConfig = MongoConfig(config("mongo.uri"),config("mongo.db"))
    import spark.implicits._

    val simMoviesMatrix = spark
      .read
      .option("uri",config("mongo.uri"))
      .option("collection", MONGO_MOVIE_RECS_COLLECTION)
      .format(MONGO_DRIVER_CLASS)
      .load()
      .as[MovieRecs]
      .rdd
      .map {recs =>
        (recs.mid,recs.recs.map(x=> (x.rid,x.r)).toMap)
      }.collectAsMap()

    val simMoviesMatrixBroadCast = sc.broadcast(simMoviesMatrix)

    val trigger = sc.makeRDD(1 to 2)
    trigger.map(x => simMoviesMatrixBroadCast.value.get(1)).count()




    val kafkaPara = Map(
      "bootstrap.servers" -> "hadoop100:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "recommender",
      "auto.offset.reset" -> "latest"
    )

    val kafkaStream = KafkaUtils.createDirectStream[String,String](ssc,LocationStrategies.PreferConsistent,ConsumerStrategies.Subscribe[String,String](Array(config("kafka.topic")),kafkaPara))

    val ratingStream = kafkaStream.map{case msg =>
      val attr = msg.value().split("\\|")
      (attr(0).toInt,attr(1).toInt,attr(2).toDouble,attr(3).toInt)
    }

    ratingStream.foreachRDD{rdd =>
      rdd.map{case (uid,mid,score,timestamp) =>
        println(">>>>>>>>>>>>>>>")
        val userRecentlyRatings = getUserRecentlyRating(MAX_USER_RATINGS_NUM,uid,ConnHelper.jedis)
        println(">>>>>>>>>>>>>>>")
        val simMovies = getTopSimMovies(MAX_SIM_MOVIES_NUM,mid,uid,simMoviesMatrixBroadCast.value)
        println(">>>>>>>>>>>>>>>")
        val streamRecs = computeMovieScores(simMoviesMatrixBroadCast.value,userRecentlyRatings, simMovies)
        println(">>>>>>>>>>>>>>>")
        saveRecsToMongoDB(uid, streamRecs)
        println("saved")

      }.count()
    }

    ssc.start()
    ssc.awaitTermination()

  }
  def saveRecsToMongoDB(uid:Int, streamRecs:Array[(Int,Double)])(implicit mongoConfig: MongoConfig): Unit = {
    val streamRecsCollection =  ConnHelper.mongoClient(mongoConfig.db)(MONGO_STREAM_RECS_COLLECTION)
    streamRecsCollection.findAndRemove(MongoDBObject("uid" -> uid))
    streamRecsCollection.insert(MongoDBObject("uid" -> uid, "recs" -> streamRecs.map(x=> x._1+":"+x._2).mkString("|")))

  }

  def computeMovieScores(simMovies:scala.collection.Map[Int,scala.collection.immutable.Map[Int,Double]],
                         userRecentlyRatings:Array[(Int, Double)],topSimMovies:Array[Int]): Array[(Int, Double)] ={

    val score = scala.collection.mutable.ArrayBuffer[(Int,Double)]()
    val increMap = scala.collection.mutable.HashMap[Int, Int]()
    val decreMap = scala.collection.mutable.HashMap[Int, Int]()

    for (topSimMovie <- topSimMovies; userRecentlyRating <- userRecentlyRatings) {
      val simScore = getMoviesSimScore(simMovies, userRecentlyRating._1, topSimMovie)
      if (simScore > 0.6) {
        score += ((topSimMovie, simScore * userRecentlyRating._2))
        if (userRecentlyRating._2 > 3) {
          increMap(topSimMovie) = increMap.getOrElse(topSimMovie, 0) + 1
        } else {
          decreMap(topSimMovie) = decreMap.getOrElse(topSimMovie, 0) + 1
        }
      }
    }
    score.groupBy(_._1).map { case (mid, sims) =>
      val incre = increMap.getOrElse(mid, 0)
      val decre = decreMap.getOrElse(mid, 0)
      (mid, sims.map(_._2).sum / sims.length + log(incre + 1) - log(decre + 1))
    }.toArray

  }

  def log(m:Int):Double ={
    math.log(m) / math.log(2)
  }

  def getMoviesSimScore(simMovies:scala.collection.Map[Int,scala.collection.immutable.Map[Int,Double]], userRatingMovie:Int, topSimMovie:Int): Double ={
    simMovies.get(topSimMovie) match {
      case Some(sim) => sim get(userRatingMovie) match {
        case Some(score) => score
        case None => 0.0
      }
      case None => 0.0
    }
  }

  def getTopSimMovies(num:Int, mid:Int, uid:Int,simMovies:scala.collection.Map[Int,scala.collection.immutable.Map[Int,Double]])(implicit mongoConfig: MongoConfig): Array[Int] ={
    val allSimMovies = simMovies.get(mid).get.toArray
    val ratingExist = ConnHelper.mongoClient(mongoConfig.db)(MONGO_RATING_COLLECTION).find(MongoDBObject("uid" -> uid)).toArray.map{item =>
      item.get("mid").toString.toInt
    }
    allSimMovies.filter(x => !ratingExist.contains(x._1)).sortWith(_._2 >_._2).take(num).map(x=> x._1)
  }

  def getUserRecentlyRating(num:Int, uid:Int, jedis:Jedis): Array[(Int, Double)] = {
    jedis.lrange("uid:"+uid.toString,0,num).map{item =>
      val attr = item.split(":")
      (attr(0).trim.toInt, attr(1).trim.toDouble)
    }.toArray
  }

}
