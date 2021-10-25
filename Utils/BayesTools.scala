package Utils

import scala.collection.mutable
import scala.collection.mutable.Map

object BayesTools {
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
}
