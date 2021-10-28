package Utils

import breeze.linalg.DenseMatrix

import scala.collection.mutable
import scala.collection.mutable.Map

object BayesTools {

	//对贝叶斯数据集和贝叶斯结构操作相关のutils


	/*
		将每个节点的取值种类用,连成string作为Value，用index作为key，组成set集合
		0 NoVisit,Visit
		1 Absent,Present
		...
	 */
	def getNodeValueMap(tf:Array[Array[String]]):Map[Int,String] = {
		//对每行数据的节点取值标记index并去重
		val temp:Array[(Int, String)] = tf.flatMap(_.zipWithIndex).distinct.map(z=>(z._2,z._1))
		val ans:mutable.Map[Int, String] = mutable.Map[Int, String]()

		//将相同index的节点取值用,进行合并
		temp.foreach(kv => {
			val index = kv._1
			val value = kv._2
			if(ans.contains(index)) {
				ans(index) = ans(index) + "," + value
			} else {
				ans(index) = "" + value
			}
		})
		ans
	}

	/*
		设置BN结构矩阵的边的方向，即节点是否是另一个节点的父亲
		edgeType=0表示aij=1，即i是j的父节点，i指向j
		edgeType=1表示aji=1，即j是i的父节点，j指向i
	 */
	def setEdgeDirectType(denseMatrix: DenseMatrix[Int], x:Int, y:Int, edgeType:Int):DenseMatrix[Int] = {
		val newMatrix:DenseMatrix[Int] = denseMatrix.copy
		if(edgeType == 0) {
			newMatrix.update(x, y, 0)
			newMatrix.update(y, x, 0)
		} else if(edgeType == 1) {
			newMatrix.update(x, y, 1)
			newMatrix.update(y, x, 0)
		} else {
			newMatrix.update(x, y, 0)
			newMatrix.update(y, x, 1)
		}
		newMatrix
	}
}
