package es.upm.oeg.tovo.topicmodel

import java.io.{File, PrintWriter}

import es.upm.oeg.distance.{InterClusterDistance, IntraClusterDistance}
import es.upm.oeg.topicmodel.metric.LocalTopicCoherence
import es.upm.oeg.topicmodel.BTM
import es.upm.oeg.tovo.corpus.LovTags
import es.upm.oeg.tovo.domain.Dictionary

/**
 * Created by dvilasuero on 14/09/15.
 * Runs grid executions for BTM from a LOV corpus
 */
object EvaluateBTM {
  val path = "src/test/corpus/lov-corpora/lov-senses-corpus.tsv";
  val d = Dictionary(path)
  val clusters = LovTags.clusters

  def main(args: Array[String]) {
    d.load()
    val iterations = 1000
    val log = new PrintWriter(new File(s"results/clustering/0110logbtm"))
    Iterator.range(10, 70, 10).foreach { k =>
      val o = new PrintWriter(new File(s"results/coherence/0110final-babelfied-btm$k.csv"))
      val oLOV = new PrintWriter(new File(s"results/clustering/0110final-lov-babelfied-btm$k.csv"))
      Iterator.range(0,10).foreach { i =>

        println(s"Starting with k = $k for iteration $i")
        val btm = new BTM(1, 0.01, k, iterations)
        btm.load(d)
        btm.estimate
        btm.report(s"results/btms/0110final-$i-$k")

        Iterator.range(5, 25, 5).foreach { T =>

          o.println("BTM_S;" // method
            +k+";" // number of topics
            +iterations+";" // iterations
            +i+";" // repetition
            +T+";" // top T terms
            +LocalTopicCoherence(d, btm.describeTopics(T)).topicCoherenceScore() // Topic coherence score
          )
        }

        // Calculate LOVTAGS scores
        clusteringScores(btm, k, i, oLOV, log)


      }
      o.close()
      oLOV.close()
    }
    log.close()
  }
  def clusteringScores(btm: BTM, k:Int, i:Int, o: PrintWriter, log:PrintWriter) {

    val cleanDocumentTopicDist = btm.documentTopicDistribution


    log.println(s"document-topic-dist-size ; ${btm.documentTopicDistribution} ; $i ; $k ")
    // Calculate intra-cluster distance for each cluster LOV
    IntraClusterDistance.documentTopicDistribution = btm.documentTopicDistribution.toMap
    val intra =  clusters.map { cluster =>
      val (id, members) = cluster
      val idist = IntraClusterDistance(members)
      log.println(s"Intracluster distance LOVTAGS ($id) = $idist")
      idist
    }.sum / clusters.size

    // Calculate inter-cluster distance between each cluster
    InterClusterDistance.documentTopicDistribution = btm.documentTopicDistribution.toMap
    val inter = InterClusterDistance(clusters)

    // output results for LOV
    o.println("BTM_S;" // method
      +"LOVTAGS" // number of topics
      +k+";" // number of topics
      +i+";" // repetition
      +intra+";" // Intra-cluster distance
      +inter+";" // Inter-cluster distance
      +intra/inter // I ratio
    )
  }
}
