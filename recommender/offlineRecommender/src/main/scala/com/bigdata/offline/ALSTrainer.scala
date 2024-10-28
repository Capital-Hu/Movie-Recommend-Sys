package com.bigdata.offline

import breeze.numerics.sqrt
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import com.bigdata.java.model.Constant._
import com.bigdata.scala.model.{MongoConfig, MovieRating}

object ALSTrainer {
  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://hadoop100:27017/recommender",
      "mongo.db" -> "recommender"
    )

    val sparkConf = new SparkConf().setAppName("ALSTrainer").setMaster(config("spark.cores"))

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val mongoConfig = MongoConfig(config("mongo.uri"),config("mongo.db"))

    import spark.implicits._
    val ratingRDD = spark
      .read
      .option("uri",mongoConfig.uri)
      .option("collection", MONGO_RATING_COLLECTION)
      .format(MONGO_DRIVER_CLASS)
      .load()
      .as[MovieRating]
      .rdd
      .map(rating => Rating(rating.uid,rating.mid,rating.score)).cache()

    //output optimal param
    adjustALSParams(ratingRDD)

    spark.close()



  }
  def adjustALSParams(trainData:RDD[Rating]): Unit={
    val result = for (
      rank <- Array(30,40,50,60,70);
      lambda <- Array(1,0.1,0.001)
    )
      yield {
        val model = ALS.train(trainData,rank,5,lambda)
        val rmse = getRmse(model, trainData)
        (rank, lambda, rmse)
      }
    println(result.sortBy(_._3).head)
  }

  def getRmse(model:MatrixFactorizationModel,trainData:RDD[Rating]): Double ={
    val userMovies = trainData.map(item => (item.user,item.product))
    val predictRating = model.predict(userMovies)
    val real = trainData.map(item => ((item.user,item.product),item.rating))
    val predict = predictRating.map(item => ((item.user,item.product),item.rating))

    sqrt(
      real.join(predict).map{case ((uid,mid),(real,pre)) =>
        val err = real - pre
        err*err
      }.mean()
    )
  }
}
