package es.upm.oeg.distance

import scala.collection.mutable.ArrayBuffer

/**
 * Created by dvilasuero on 21/09/15.
 */
object IntraClusterDistance {
  var documentTopicDistribution = Map[String,Array[Double]]()

  def apply (members: ArrayBuffer[String]): Double = {
    // Sanity check remove vocabs from cluster that do not have a topic distribution
    val clusterArrays : ArrayBuffer[String] = members.map(v => v).filter(documentTopicDistribution isDefinedAt _)
    val pairsVocabs = for(i_ <- clusterArrays; j_ <- clusterArrays) yield (i_, j_)

    // Calculate the distance for each pair, sum and avg
    val avg = pairsVocabs.map { case(vi, vj)=>
      JensenShannonDivergence(documentTopicDistribution(vi), documentTopicDistribution(vj))
    }.sum / pairsVocabs.size
    avg
  }
}
