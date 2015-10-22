package es.upm.oeg.topicmodel.metric

import es.upm.oeg.tovo.domain.Dictionary

import scala.collection.Iterator

/**
 * Created by dvilasuero on 14/09/15.
 */
case class LocalTopicCoherence (d : Dictionary, topicDescriptors : Array[(Array[Int], Array[Double])]) {
  def topicCoherenceScore(): Double= {

    var globalCoherenceScore : Double = 0.0

    topicDescriptors.foreach { topic =>
      val combinations = getBiterms(topic._1)
      val score = combinations.map { pair =>

        val (w1, w2) = pair
        // calculate frequency of word pair for each document
        var w1Freq: Int = 0
        var pairFreq: Int = 1 // smoothing count fixed to 1
        d.documents.foreach { x =>
          val doc = x._2

          val countsPerWord = doc.groupBy(l => l).map(t => (t._1, t._2.length))
          if(countsPerWord.keySet.exists(_ == w1)) {
            w1Freq += 1
            if(countsPerWord.keySet.exists(_ == w2)) {
              pairFreq += 1
            }
          }
        }
        val ratio = pairFreq.toDouble / w1Freq
        Math.log(ratio)
      }.sum
      globalCoherenceScore += score
    }
    globalCoherenceScore / topicDescriptors.length
  }
  private def getBiterms(d:Array[Int]):Iterator[(Int, Int)] = {
    d.toSeq.combinations(2).map { case Seq(w1, w2) =>
      if (w1 < w2) (w1, w2) else (w2, w1)
    }
  }
}
