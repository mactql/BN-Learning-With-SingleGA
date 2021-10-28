package Utils

import breeze.linalg.DenseMatrix

import scala.collection.{Set, mutable}
import scala.util.Random

object CycleUtils {

	def removeCycleWithDegree(matrix:DenseMatrix[Int], numOfAttribute:Int):DenseMatrix[Int] = {
		val nextBn = matrix.copy
		/*
		val cycleUtils:CycleUtils = new CycleUtils()
		cycleUtils.numOfAttribute = numOfAttribute
		var nodeList:List[Int] = (0 until numOfAttribute).toList
		var seq1:List[Int] = List[Int]()
		var seq2:List[Int] = List[Int]()
		while(nodeList.nonEmpty) {
			var brk = false
			while (!brk && nodeList.nonEmpty) {
				// find min out degree
				var dpm:(Int, Int) = (-1, Integer.MAX_VALUE)
				nodeList.foreach(node => {
					val tmpSetWithoutNode:Set[Int] = nodeList.filter(_!=node).toSet
					val curOutDegree:Int = cycleUtils.outDegree(nextBn, node, tmpSetWithoutNode)
					if(curOutDegree > dpm._2) {
						dpm = (node, curOutDegree)
					}
				})
				if (dpm._2 == 0) {
					seq2 = dpm._1 :: seq2
					nodeList = nodeList.filter(_!=dpm._1)
				} else {
					brk = true
				}
				/*
				var removedNode = false
				for (node <- nodeList if !removedNode) {
				  val tmpSetWithoutNode:Set[Int] = nodeList.filter(_!=node).toSet
				  if(cycleUtils.outDegree(nextBn, node, tmpSetWithoutNode) == 0) {
					seq2 = node :: seq2
					nodeList = nodeList.filter(_!=node)
					removedNode = true
				  } else if(node == nodeList.last) {
					brk = true
				  }
				}
				*/
			}
			brk = false
			while(!brk && nodeList.nonEmpty) {
				// find min out degree
				var dpm:(Int, Int) = (-1, Integer.MAX_VALUE)
				nodeList.foreach(node => {
					val tmpSetWithoutNode:Set[Int] = nodeList.filter(_!=node).toSet
					val curInDegree:Int = cycleUtils.inDegree(nextBn, node, tmpSetWithoutNode)
					if(curInDegree > dpm._2) {
						dpm = (node, curInDegree)
					}
				})
				if (dpm._2 == 0) {
					seq1 = seq1 :+ dpm._1
					nodeList = nodeList.filter(_!=dpm._1)
				} else {
					brk = true
				}
				/*
				var removedNode = false
				for (node <- nodeList if !removedNode) {
				  val tmpSetWithoutNode:Set[Int] = nodeList.filter(_!=node).toSet
				  if(cycleUtils.inDegree(nextBn, node, tmpSetWithoutNode) == 0) {
					seq1 = seq1 :+ node
					nodeList = nodeList.filter(_!=node)
					removedNode = true
				  } else if(node == nodeList.last) {
					brk = true
				  }
				}
				*/
			}
			if(nodeList.nonEmpty) {
				var max_out_in = -1
				var add_node = -1
				for(node <- nodeList) {
					val tmpSetWithoutNode:Set[Int] = nodeList.filter(_!=node).toSet
					val temp_max_inout = cycleUtils.outDegree(nextBn, node, tmpSetWithoutNode) - cycleUtils.inDegree(nextBn, node, tmpSetWithoutNode)
					if(temp_max_inout > max_out_in) {
						max_out_in = temp_max_inout
						add_node = node
					}
				}
				seq1 = seq1 :+ add_node
				nodeList = nodeList.filter(_!=add_node)
			}
		}
		val seq = seq1 ++ seq2
		// remove arc
		var ind = -1
		for(jI <- 1 until seq.size) {
			val j = seq(jI)
			ind += 1
			for(kI <- 0 until ind) {
				val k = seq(kI)
				nextBn.update(j, k, 0)
			}
		}

		 */
		nextBn
	}

	//删除超过最大父节点数量的冗余边
	def limitParentNaive(matrix:DenseMatrix[Int], numOfAttribute:Int, maxParent:Int):DenseMatrix[Int] = {
		/*
			找出每个节点的父节点集合，删除超过maxParent的边
		 */
		val validBN = matrix.copy
		for (node <- 0 until numOfAttribute) {
			//把父节点过滤出来
			val parentSet: Array[Int] = Range(0, numOfAttribute).filter(matrix(_, node) == 1).toArray
			//删除冗余的边
			if (parentSet.length > maxParent) {
				val needRemoveLength = parentSet.length - maxParent
				val needRemoveSet: mutable.Set[Int] = mutable.Set()
				Range(0, needRemoveLength).foreach(index => {
					var tempParent = parentSet(Random.nextInt(parentSet.length))
					//是否这里会出现多次随机到已经选到过的节点的情况，优化？
					while (needRemoveSet.contains(tempParent)) {
						tempParent = parentSet(Random.nextInt(parentSet.length))
					}
					needRemoveSet.add(tempParent)
				})
				needRemoveSet.foreach(validBN.update(_, node, 0))
			}
		}
		validBN
	}
}
class CycleUtils{
	var numOfAttribute = 0
}
