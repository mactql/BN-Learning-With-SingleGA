package Experiences

import Config.RedisConfig
import Experiences.SingleGA.SPARK_JARS_HOME
import Models.BNStructure
import Utils.{BayesTools, MutualInformationUtils}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import redis.clients.jedis.{Jedis, Pipeline}

import scala.collection.Set

object DistributedGA {

	var maxPa:Int = 4

	var nrOfAttr:Int = 0

	var nrOfPopulation:Int = 100

	var nrOfMaxIterator:Int = 200

	var crossoverRate:Double = 0.5

	var mutationRate:Double = 0

	var SPARK_JARS_HOME = "/usr/hdp/3.1.0.0-78/spark2/jars/"

	def run(): Unit = {
		val ga: DistributedGA = new DistributedGA()
		ga.run()
	}
}


class DistributedGA extends java.io.Serializable{
	var sampleName = "asia"
	var inputPath = "/Users/caiyiming/SingleGA/Samples/asia50000.csv"

	var finalBNStructure:BNStructure = _

	def run(): Unit ={

		val tournamentSize:Int = 2
		val scoreJedis:Jedis = new Jedis(RedisConfig.redisHosts, RedisConfig.redisPort)
		val scoreJedisPipeline:Pipeline = scoreJedis.pipelined()

		//创建sparkContext
		val conf = new SparkConf().setAppName("DistributedGA")
				.setMaster("yarn")
				.setSparkHome(SPARK_JARS_HOME)
		val sc = new SparkSession.Builder().config(conf).getOrCreate().sparkContext

		//读取输入数据RDD，最小分区数为48(师兄设置的)
		val textfile:RDD[Array[String]] = sc.textFile(inputPath,48).cache().map(_.split(","))

		//获取样本数据的节点数目
		val numOfAttributes = textfile.take(1)(0).length

		//记录算法开始时间
		val startTime = System.currentTimeMillis()

		/*
			将每个节点的取值种类用","连成string作为Value，用index作为key，组成set集合
			0 NoVisit,Visit
			1 Absent,Present
			...
		 */
		val valueTypeSet:Set[(Int,String)] = BayesTools.getNodeValueMap(textfile).collect().toSet

		//广播每个节点取值种类
		val broadValueTpye = sc.broadcast(valueTypeSet)

		/*
			计算互信息矩阵
		 */
		//val mutualInfoMatrix = MutualInformationUtils.getMutualInfoMatrix(textfile,numOfAttributes,valueTypeSet,scoreJedisPipeline,sc,num)
	}
}
