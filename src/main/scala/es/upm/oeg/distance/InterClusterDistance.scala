package es.upm.oeg.distance

import scala.collection.mutable.{ArrayBuffer}

/**
 * Created by dvilasuero on 21/09/15.
 */
object InterClusterDistance {
  var documentTopicDistribution = Map[String,Array[Double]]()
  def apply (clusters: scala.collection.mutable.Map[String,ArrayBuffer[String]]): Double = {
    // Sanity check remove vocabs from cluster that do not have a topic distribution
    val clustersArrays = clusters.map(_._2).map {a =>
      a.filter((documentTopicDistribution isDefinedAt _))
    }
    // Create combinations of clusters
    val pairsClusters = for(i_ <- clustersArrays; j_ <- clustersArrays) yield (i_, j_)
    val avgC = pairsClusters.map { case (ci, cj) =>
      // Create combinations of vocabs
      val pairs = for(i_ <- ci; j_ <- cj) yield (i_, j_)
      val avg = pairs.map { case(vi, vj)=>
        JensenShannonDivergence(documentTopicDistribution(vi), documentTopicDistribution(vj))
      }.sum
      avg/pairs.size
    }.sum
    avgC / pairsClusters.size
  }
}
