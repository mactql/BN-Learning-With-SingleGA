package RunableClass

import Experiences._

object Main {
	def main(args: Array[String]): Unit = {
		//exp1：基于标准GA的贝叶斯网络结构学习
		SingleGA.run()

		//exp2：基于分布式GA的贝叶斯网络结构学习
		//DistributedGA.run()
	}
}
