package Models.ScoreModels



class BICScore extends java.io.Serializable{

	var numOfSamples:Long = 0
	var numOfAttributes = 0

	def this(initNumOfAttr:Int, dataSet:Array[Array[String]]) = {
		this()
//		BICScore.eachIterCountCalNumFile.initCSV()
		numOfAttributes = initNumOfAttr
		this.numOfSamples = dataSet.length
	}
}
