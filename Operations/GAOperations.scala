package Operations


import Experiences.SingleGA
import Experiences.SingleGA._
import Models.BNStructure
import Utils.BayesTools.{getEdgeDirectType, setEdgeDirectType}
import Utils.CycleUtils
import breeze.linalg.DenseMatrix
import org.apache.spark._

import scala.util.Random

object GAOperations {

	val crossoverRate = 0.5

	//初始化种群
	def initPopulationAllWithRemoveCycle(numOfPopulation:Int, numOfAttributes:Int, sc:SparkContext):Array[DenseMatrix[Int]] = {
		//n个变量的BN结构可以用n*n的邻接矩阵表示，aij=1则表示i是j的父节点
		val populationArray:Array[DenseMatrix[Int]] = (0 until numOfPopulation).toArray.map(i => {
			var tmpBN:DenseMatrix[Int] = DenseMatrix.zeros(numOfAttributes,numOfAttributes)
			(0 until numOfAttributes-1).foreach(x => {
				((x+1) until numOfAttributes).foreach(y => {
					val edgeType = Random.nextInt(3)
					tmpBN = setEdgeDirectType(tmpBN, x, y, edgeType)
				})
			})

			var validBN = CycleUtils.removeCycleWithDegree(tmpBN, numOfAttributes)
			validBN = CycleUtils.limitParentNaive(validBN, numOfAttributes, maxParent)
			validBN

		})
		populationArray
	}

	//获取种群中评分最高的个体，即获取精英个体
	def getEliteIndividual(populations:Array[BNStructure]):BNStructure = {
		var curBestBN = new BNStructure()
		populations.foreach(individual =>{
			if(individual.score > curBestBN.score){
				curBestBN.structure = individual.structure.copy
				curBestBN.score = individual.score
			}
		})
		curBestBN
	}
	
	//找出种群中评分最差的劣质个体并用精英个体替换
	def replaceLowestWithElite(populations:Array[BNStructure], elite:BNStructure):Array[BNStructure] = {
		var minIndex:Int = -1
		var minScore:Double = Double.MaxValue
		populations.zipWithIndex.foreach(m => {
			if(m._1.score < minScore) {
				minScore = m._1.score
				minIndex = m._2
			}
		})
		populations(minIndex) = elite
		populations
	}
	
	
	/*	二元锦标赛算子
		每次从种群中随机选择两个个体，根据下标找到并组成锦标赛BN数组(tournamentBNArray)
		找出锦标赛BN数组中评分最高的个体放入下一代
		循环前两步，直到下一代达到种群规模
	 */
	def tournamentSelection(doublePopulation: Array[BNStructure], tournamentSize:Int, numOfPopulation:Int, sc:SparkContext):Array[BNStructure] = {
		val nextPopulations:Array[BNStructure] = new Array[BNStructure](numOfPopulation)
		val curPopulationSize:Int = doublePopulation.length
		for (populationIndex <- 0 until numOfPopulation) {
			/* 师兄代码
			//在种群中随机取两个个体下标，并根据下标构成锦标赛BN数组
			val tournamentIndexArr:Array[Int] = (0 until tournamentSize).map(i => {Random.nextInt(curPopulationSize)}).toArray
			val tournamentBNArray:Array[BNStructure] = tournamentIndexArr.map(doublePopulation(_))
			//找到评分最高的个体
			val maxScore:Double = tournamentBNArray.map(_.score).max
			val winner:BNStructure = tournamentBNArray.find(_.score == maxScore).orNull
			val copyWinner:BNStructure = new BNStructure(winner.structure.copy, winner.score)
			nextPopulations(populationIndex) = copyWinner
			*/


			val tournamentA = doublePopulation(Random.nextInt((curPopulationSize)))
			val tournamentB = doublePopulation(Random.nextInt((curPopulationSize)))
			var winner:BNStructure = null
			if(tournamentA.score > tournamentB.score)
				winner = new BNStructure(tournamentA.structure.copy,tournamentA.score)
			else
				winner = new BNStructure(tournamentB.structure.copy,tournamentB.score)
			nextPopulations(populationIndex) = winner
		}
		nextPopulations
	}

	
	//调用均匀交叉，得到两百条染色题的population：前100条染色体为锦标赛选择出来的种群，后100条为前100条经过均匀交叉后的种群
	def uniformCrossoverAll(populations:Array[BNStructure], numOfPopulation:Int, numOfAttribute:Int,sc:SparkContext):Array[BNStructure] = {
		val nextPopulations:Array[BNStructure] = new Array[BNStructure](numOfPopulation)
		populations.zipWithIndex.foreach(pi => {
			val index = pi._2
			val p:DenseMatrix[Int] = pi._1.structure
			var tempIndex = Random.nextInt(populations.length)
			while (tempIndex == index) {
				tempIndex = Random.nextInt(populations.length)
			}
			val tempP:DenseMatrix[Int] = populations(tempIndex).structure
			nextPopulations(index) = new BNStructure(uniformCrossover(p, tempP,numOfAttribute))
		})
		val population = populations++nextPopulations
		population
	}
	//对两条染色体均匀交叉+去环+限制父节点数量
	private def uniformCrossover(base: DenseMatrix[Int], other: DenseMatrix[Int],numOfAttribute:Int):DenseMatrix[Int] = {
		var next:DenseMatrix[Int] = base.copy
		(0 until numOfAttribute).foreach(x => {
			((x+1) until numOfAttribute).foreach(y => {
				val useBase:Boolean = Random.nextDouble() <= crossoverRate
				if (!useBase) {
					next.update(x, y, base(x, y))
					next.update(y, x, base(y, x))
				} else {
					next.update(x, y, other(x, y))
					next.update(y, x, other(y, x))
				}
			})
		})
		next = CycleUtils.removeCycleWithDegree(next, numOfAttribute)
		next = CycleUtils.limitParentNaive(next, numOfAttribute, maxParent)
		next
	}

	
	//调用单点突变 得到200条突变后的染色体
	def singlePointMutationAll(populations:Array[BNStructure],numOfAttribute:Int):Array[BNStructure] = {
		val nextPopulation = populations.map(p =>{
			new BNStructure(singlePointMutation(p.structure,numOfAttribute))
		})
		nextPopulation
	}
	//对一条染色体单点突变+去环+限制父节点数量
	private def singlePointMutation(base: DenseMatrix[Int],numOfAttribute:Int):DenseMatrix[Int] = {
		var next:DenseMatrix[Int] = base.copy
		val rate:Double = 1.0 / (numOfAttribute * (numOfAttribute - 1) / 2)
		//对每一条边进行突变
		(0 until numOfAttribute).foreach(x => {
			((x+1) until numOfAttribute).foreach(y => {
				val originType:Int = getEdgeDirectType(base, x, y)
				if(rate >= Random.nextDouble()) {
					val newType:Int = (originType + Random.nextInt(2) + 1) % 3
					next = setEdgeDirectType(next, x, y, newType)
				}
			})
		})
		next = CycleUtils.removeCycleWithDegree(next, numOfAttribute)
		next = CycleUtils.limitParentNaive(next, numOfAttribute, maxParent)
		next
	}
	
	
}
