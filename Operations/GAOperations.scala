package Operations


import Experiences.SingleGA
import Utils.BayesTools.setEdgeDirectType
import Utils.CycleUtils
import breeze.linalg.DenseMatrix
import org.apache.spark._

import scala.util.Random

object GAOperations {

	//初始化种群
	def initPopulationAllWithRemoveCycle(arrSize:Int, numOfAttributes:Int, sc:SparkContext):Array[DenseMatrix[Int]] = {
		//n个变量的BN结构可以用n*n的邻接矩阵表示，aij=1则表示i是j的父节点
		val populationArray:Array[DenseMatrix[Int]] = (0 until arrSize).toArray.map(i => {
			var tmpBN:DenseMatrix[Int] = DenseMatrix.zeros(numOfAttributes,numOfAttributes)
			(0 until numOfAttributes-1).foreach(x => {
				((x+1) until numOfAttributes).foreach(y => {
					val edgeType = Random.nextInt(3)
					tmpBN = setEdgeDirectType(tmpBN, x, y, edgeType)
				})
			})

			var validBN = CycleUtils.removeCycleWithDegree(tmpBN, numOfAttributes)
			validBN = CycleUtils.limitParentNaive(validBN, numOfAttributes, SingleGA.maxParent)
			validBN

		})
		populationArray
	}


}
