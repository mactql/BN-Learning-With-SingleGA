package Utils

import Models.BNStructure
import breeze.linalg.DenseMatrix
import org.apache.spark.SparkContext

object EndUtils {

	def evaluateAccuracyOfTruePositive(stdModelName:String, curModel:DenseMatrix[Int],sc:SparkContext): Double = {

		//val stdModel:Array[Array[Int]] = CSVFileUtils.readStructureFromCsv(stdModelName)
		val stdModel:Array[Array[Int]] = sc.textFile("/Users/caiyiming/SingleGA/Models/CSV/"+stdModelName+".csv").map(line=>{
			var temp:Array[String] = line.split(",")
			temp.map(t=>{
				if(t.equals("1"))
					1;
				else 0;
			})
		}).collect()

		val nrOfAttr = stdModel.length
		var totP0 = 0
		var totN0 = 0
		for (i <- 0 until nrOfAttr) {
			for (j <- i until nrOfAttr) {
				if (stdModel(i)(j) != stdModel(j)(i)) {
					totP0 = totP0 +1
				} else {
					totN0 = totN0 + 1
				}
			}
		}
		var f1:Double = 0
		var se:Double = 0
		var sp:Double = 0
		var matchP:Int = 0
		var matchN:Int = 0
		var totP:Int = 0
		var totN:Int = 0
		for (i <- 0 until nrOfAttr) {
			for (j <- i until nrOfAttr) {
				val ij = curModel(i, j)
				val ji = curModel(j, i)
				if (ij != ji) {
					totP = totP+1
				} else {
					totN = totN+1
				}
				if (ij == stdModel(i)(j) && ji == stdModel(j)(i)) {
					if (ij == 0 && ji == 0) {
						matchN = matchN+1
					} else {
						matchP = matchP+1
					}
				}
			}
		}
		if (totP0 == 0) {
			se = 1.0
		} else {
			se = 1.0 * matchP/totP0
		}
		var pr:Double = 0
		if (totP == 0) {
			pr = 1
		} else {
			pr = 1.0 * matchP/totP
		}
		if (totN0 == 0) {
			sp = 1
		} else {
			sp = 1.0 * matchN/totN0
		}
		if (pr+se == 0) {
			f1 = 0
		} else {
			f1 = 1.0 * 2*pr*se/(pr+se)
		}
		f1
	}


}
