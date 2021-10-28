package Models

import breeze.linalg.DenseMatrix

object BNStructure{

	def BNConvertFromMatrix(BNMatrixs:Array[DenseMatrix[Int]]):Array[BNStructure] = {
		val arrSize = BNMatrixs.length
		val BNStructurePopulation:Array[BNStructure] = new Array[BNStructure](arrSize)

/*		师兄代码,使用zipWithIndex+foreach会比直接for循环效率更高吗？
		BNMatrixs.zipWithIndex.foreach(BNWithIndex => {
			val index:Int = BNWithIndex._2
			val bn:DenseMatrix[Int] = BNWithIndex._1
			BNStructurePopulation(index) = new BNStructure(bn)
		})
 */
		for(index <- 0 to arrSize){
			BNStructurePopulation(index) = new BNStructure(BNMatrixs(index))
		}
		BNStructurePopulation
	}
}

class BNStructure {

	var structure:DenseMatrix[Int] = DenseMatrix.zeros(1,1)

	var score:Double = Double.MinValue

	def this(BN:DenseMatrix[Int]) = {
		this()
		this.structure = BN
		this.score = Double.MinValue
	}
}
