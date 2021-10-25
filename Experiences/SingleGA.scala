package Experiences

import Experiences.SingleGA._
import Models.ScoreModels._
import Utils._
import Operations.GAOperations._

import org.apache.spark.sql._
import redis.clients.jedis._
import scala.collection._
import breeze.linalg._

object SingleGA{

	var numOfPopulation:Int = 100

	def run(): Unit = {
		val ga: SingleGA = new SingleGA()
		ga.run()
	}
}

class SingleGA extends java.io.Serializable{

	var sampleName = "asia"
	var inputPath = "/Users/caiyiming/Documents/Sparkproject/Samples/asia_50000.csv"

	def run(): Unit = {
		var tournamentSize:Int = 2
//		val scoreJedis:Jedis = new Jedis(RedisConfig.redisHosts, RedisConfig.redisPort)
//		val scoreJedisPipeline:Pipeline = scoreJedis.pipelined()

		//创建sparkContext
		val sc = new SparkSession.Builder().appName("SingleGA").master("local").getOrCreate().sparkContext

		//读取输入数据，最小分区数为48(师兄设置的),用collect将RDD转化为数组，即样本数据的二维数组
		val textfile:Array[Array[String]] = sc.textFile(inputPath,48).cache().map(_.split(",")).collect()

		//获取样本数据的属性数目
		val numOfAttributes = textfile(0).length

		//初始化贝叶斯评分
		val score:BICScore = new BICScore(numOfAttributes,textfile)

		/*
			将每个节点的取值种类用,连成string作为Value，用index作为key，组成set集合
			0 NoVisit,Visit
			1 Absent,Present
			...
		 */
		val nodeValueMap:Set[(Int,String)] = BayesTools.getNodeValueMap(textfile).toSet

		//初始化种群
		val populationArray:Array[DenseMatrix[Int]] = initPopulationAllWithRemoveCycle(numOfPopulation * 2, numOfAttributes, sc)

		
	}

}
