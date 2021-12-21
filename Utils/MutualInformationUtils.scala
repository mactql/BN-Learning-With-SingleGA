package Utils

import Models.ScoreCondition
import breeze.linalg.DenseMatrix
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import redis.clients.jedis.Pipeline

import scala.collection.{Map, Set}

object MutualInformationUtils {

	def getMutualInfoMatrix(textfile:RDD[Array[String]], numOfAttribute:Int, nodeValueType:Set[(Int,String)], scoreJedisPipeline:Pipeline, sc:SparkContext, nrOfSamples:Long):DenseMatrix[Double] = {
		//求每一个节点的所有可能的condition
		val singleConditions:Set[ScoreCondition] = (0 until numOfAttribute).flatMap(line => {
			val xValues:Set[(Int,String)] = nodeValueType.filter(_._1==line).flatMap(m => m._2.split(",")).map((line, _)).toSet
			val xsingleConditions = xValues.map(xType => {
				val singleScoreCondition = new ScoreCondition()
				singleScoreCondition.addKeyValue(xType._1.toString,xType._2)
				println(xType._1 + " " + xType._2)
				singleScoreCondition

			})
			xsingleConditions
		}).toSet

		//求每两个节点的所有可能的condition
		val pairConditions:Set[ScoreCondition] = (0 until numOfAttribute).flatMap(lineX => {
			val xValues:Set[(Int,String)] = nodeValueType.filter(_._1==lineX).flatMap(m => m._2.split(",")).map((lineX, _)).toSet
			val xNeeds:Set[ScoreCondition] = ((lineX+1) until numOfAttribute).flatMap(lineY => {
				val yValues:Set[(Int,String)] = nodeValueType.filter(_._1==lineY).flatMap(m => m._2.split(",")).map((lineY, _)).toSet
				val yNeeds:Set[ScoreCondition] = xValues.flatMap(xType => {
					yValues.map(yType => {
						val singleScoreCondition = new ScoreCondition()
						singleScoreCondition.addKeyValue(xType._1.toString,xType._2)
						singleScoreCondition.addKeyValue(yType._1.toString,yType._2)
						singleScoreCondition
					})
				})
				yNeeds
			}).toSet
			xNeeds
		}).toSet
		val needs = singleConditions.union(pairConditions)
		val dataMap:Map[ScoreCondition, Long] = getMapsFromDataWithRedis(textfile, needs, sc, scoreJedisPipeline)

		val mutualInformationMatrix:DenseMatrix[Double] = DenseMatrix.zeros(numOfAttribute, numOfAttribute)
		(0 until numOfAttribute).foreach(lineX => {
			(lineX+1 until numOfAttribute).foreach(lineY => {
				val xValues:Set[(Int,String)] = nodeValueType.filter(_._1==lineX).flatMap(m => m._2.split(",")).map((lineX, _)).toSet
				val yValues:Set[(Int,String)] = nodeValueType.filter(_._1==lineY).flatMap(m => m._2.split(",")).map((lineY, _)).toSet
				val mutualInformation:Double = yValues.map(yType => {
					val curYValue = yType._2
					xValues.map(xType => {
						var curAnsValue:Double = 0
						val curXValue = xType._2
						val xCondition = new ScoreCondition()
						xCondition.addKeyValue(xType._1.toString, curXValue)
						val yCondition = new ScoreCondition()
						yCondition.addKeyValue(yType._1.toString, curYValue)
						if(dataMap(xCondition) == 0 || dataMap(yCondition) == 0) {
							curAnsValue = 0
						} else {
							val xyCondition = new ScoreCondition()
							xyCondition.addKeyValue(xType._1.toString, curXValue)
							xyCondition.addKeyValue(yType._1.toString, curYValue)
							var p_xy:Double = 0
							if(dataMap(xyCondition) == 0) {
								curAnsValue = 0
							}
							else {
								p_xy = 1.0 * dataMap(xyCondition) / nrOfSamples
								val p_x = 1.0 * dataMap(xCondition) / nrOfSamples
								val p_y = 1.0 * dataMap(yCondition) / nrOfSamples
								curAnsValue = p_xy * Math.log(p_xy / (p_x * p_y))
							}
						}
						curAnsValue
					}).sum
				}).sum
				mutualInformationMatrix.update(lineX, lineY, mutualInformation)
				mutualInformationMatrix.update(lineY, lineX, mutualInformation)
			})
		})
		mutualInformationMatrix
	}
}
