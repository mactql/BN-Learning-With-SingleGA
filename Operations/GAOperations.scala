package Operations

import Utils.CycleUtils
import breeze.linalg.DenseMatrix
import org.apache.spark._

import scala.util.Random

object GAOperations {

	def initPopulationAllWithRemoveCycle(arrSize:Int, nrOfAttributes:Int, sc:SparkContext):Array[DenseMatrix[Int]] = {
		val pop:Array[DenseMatrix[Int]] = (0 until arrSize).toArray.map(i => {
			var tmpBN:DenseMatrix[Int] = DenseMatrix.zeros(nrOfAttributes,nrOfAttributes)
			(0 until nrOfAttributes-1).foreach(x => {
				((x+1) until nrOfAttributes).foreach(y => {
					val edgeType = Random.nextInt(3)
					tmpBN = setEdgeDirectType(tmpBN, x, y, edgeType)
				})
			})
			var next = CycleUtils.removeCycleWithDegree(tmpBN, nrOfAttributes)
			next = CycleUtils.limitParentNaive(next, nrOfAttributes, SingleGA.maxPa)
			next
		})
		pop
	}

}
