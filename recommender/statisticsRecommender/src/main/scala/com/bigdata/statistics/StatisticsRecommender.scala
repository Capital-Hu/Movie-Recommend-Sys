package com.bigdata.statistics

import com.bigdata.scala.model.{GenresRecommendation, MongoConfig, Movie, MovieRating, Recommendation}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import com.bigdata.java.model.Constant._

import java.text.SimpleDateFormat
import java.util.Date


object StatisticsRecommender {

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://hadoop100:27017/recommender",
      "mongo.db" -> "recommender"
    )

    val sparkConf = new SparkConf().setAppName("StatisticsRecommender").setMaster(config("spark.cores"))

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))



    import spark.implicits._

    val ratingDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_RATING_COLLECTION)
      .format(MONGO_DRIVER_CLASS)
      .load()
      .as[MovieRating]
      .toDF

    val movieDF = spark
      .read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_MOVIE_COLLECTION)
      .format(MONGO_DRIVER_CLASS)
      .load()
      .as[Movie]
      .toDF

    ratingDF.createOrReplaceTempView("ratings")
    // mid, count
    val rateMoreMoviesDF = spark.sql("select mid, count(mid) as count from ratings group by mid")

    rateMoreMoviesDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGO_RATE_MORE_MOVIES)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()

    val simpleDateFormat = new SimpleDateFormat("yyyyMM")
    spark.udf.register("chageDate",(x:Int) => simpleDateFormat.format(new Date(x * 1000L)).toInt)

    val ratingOfYearMonth = spark.sql("select mid, score, chageDate(timestamp) as yearmonth from ratings")

    ratingOfYearMonth.createOrReplaceTempView("ratingOfMonth")

    val rateMoreRecentlyMovies = spark.sql("select mid, count(mid) as count, yearmonth from ratingOfMonth group by yearmonth, mid")

    rateMoreRecentlyMovies
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGO_RATE_MORE_RECENTLY_MOVIES)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()

    val averageMoviesDF = spark.sql("select mid, avg(score) as avg from ratings group by mid")

    averageMoviesDF
      .write
      .option("uri",mongoConfig.uri)
      .option("collection",MONGO_AVERAGE_MOVIES)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()

    val movieWithScore = movieDF.join(averageMoviesDF,Seq("mid", "mid"))
    val genres = List("Action","Adventure","Animation","Comedy","Crime","Documentary","Drama","Family","Fantasy","Foreign","History","Horror","Music","Mystery"
      ,"Romance","Science","Tv","Thriller","War","Western")

    val genresRDD = spark.sparkContext.makeRDD(genres)

    val genresTopMovies = genresRDD.cartesian(movieWithScore.rdd)
      .filter{
        case (genres,row) => row.getAs[String]("genres").toLowerCase.contains(genres.toLowerCase)
      }
      .map{
        case (genres, row) => {
          (genres,(row.getAs[Int]("mid"), row.getAs[Double]("avg")))
        }
      }
      .groupByKey()
      .map{
        case (genres, items) => GenresRecommendation(genres, items.toList.sortWith(_._2 > _._2).take(16).map(item => Recommendation(item._1,item._2)))
      }
      .toDF

    genresTopMovies
      .write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGO_GENRES_TOP_MOVIES)
      .mode("overwrite")
      .format(MONGO_DRIVER_CLASS)
      .save()


    spark.stop()
  }
}
