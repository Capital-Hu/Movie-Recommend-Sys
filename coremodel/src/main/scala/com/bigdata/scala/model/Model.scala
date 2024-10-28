package com.bigdata.scala.model

/**
 * ID,Name,Description,Length,Publish Date,Start Date,Language,Type,Actor,Leader
 */
case class Movie(val mid:Int, val name:String, val descri:String, val timelong:String, val issue:String,
                 val shoot:String, val language:String, val genres:String, val actors:String, val directors:String)
/**
 * UserId,MovieId,Rating,Date
 */
case class MovieRating(val uid:Int, val mid:Int, val score:Double, val timestamp: Int)
/**
 * UserId,MovieId,Tag,Date
 */
case class Tag(val uid:Int, val mid:Int, val tag:String, val timestamp: Int)

/**
 *
 * @param uri 链接
 * @param db 数据库
 */
case class MongoConfig(val uri:String, val db:String)

/**
 * ES
 * @param httpHosts 主机列表，以，分隔
 * @param transportHosts  Transport主机列表。以，分隔
 * @param index 需要操作的索引
 * @param clustername ES集群的名称
 */
case class ESConfig(val httpHosts:String, val transportHosts:String, val index:String, val clustername:String )

//推荐
case class Recommendation(rid:Int, r:Double)

// 用户的推荐
case class UserRecs(uid:Int, recs:Seq[Recommendation])

//电影的相似度
case class MovieRecs(mid:Int, recs:Seq[Recommendation])

/**
 * 电影类别的推荐
 * @param genres 类别
 * @param recs 集合
 */
case class GenresRecommendation(genres:String, recs:Seq[Recommendation])

object Model {

}
