package es.upm.oeg.tovo.topicmodel

import java.io.{File, PrintWriter}

import es.upm.oeg.distance.{InterClusterDistance, IntraClusterDistance}
import es.upm.oeg.topicmodel.metric.LocalTopicCoherence
import es.upm.oeg.topicmodel.LDAWrapper
import es.upm.oeg.tovo.corpus.LovTags
import es.upm.oeg.tovo.domain.Dictionary
import org.apache.spark.mllib.clustering.DistributedLDAModel

/**
 * Created by dvilasuero on 14/09/15.
 * Runs grid executions for LDA from a LOV corpus
 */
object EvaluateLDAS {
  val path = "src/test/corpus/lov-corpora/lov-sense-corpus.tsv"
  val d = Dictionary(path)
  val clusters = LovTags.clusters
  val ldaWrapper = new LDAWrapper()

  def main(args: Array[String]) {
    d.load()
    val iterations = 1000
    val log = new PrintWriter(new File(s"results/clustering/tokenloglda"))
    Iterator.range(10, 70, 10).foreach { k =>
      val o = new PrintWriter(new File(s"results/coherence/3009final-babelfied-lda$k.csv"))
      val oLOV = new PrintWriter(new File(s"results/clustering/3009final-lov-babelfied-lda$k.csv"))
      Iterator.range(0,10).foreach { i =>

        println(s"Starting with k = $k for iteration $i")
        val lda:DistributedLDAModel  = ldaWrapper.run(ldaWrapper.corpus(d), k, iterations).asInstanceOf[DistributedLDAModel]

        Iterator.range(5, 25, 5).foreach { T =>

          o.println("LDA_S;" // method
            +k+";" // number of topics
            +iterations+";" // iterations
            +i+";" // repetition
            +T+";" // top T terms
            +LocalTopicCoherence(d, lda.describeTopics(T)).topicCoherenceScore() // Topic coherence score
          )

        }

        // Calculate LOVTAGS scores
        clusteringScores(lda, k, i, oLOV, log)

        // Save topic descriptions of T = 20
        val topics = new PrintWriter(new File(s"results/blda/final-$i-$k-documents.k$k"))
        lda.describeTopics(20).foreach { t =>
          topics.println(t._2.mkString(" "))
        }
        topics.close()


      }
      o.close()
      oLOV.close()
    }
    log.close()
  }
  def clusteringScores(lda: DistributedLDAModel, k:Int, i:Int, o: PrintWriter, log:PrintWriter) {

    val documentTopicDistribution  = d.documents.map(_._1).zip(lda.topicDistributions.collect().map(_._2.toArray)).toMap


    log.println(s"document-topic-dist-size ; ${documentTopicDistribution.size} ; $i ; $k ")
    // Calculate intra-cluster distance for each cluster LOV
    IntraClusterDistance.documentTopicDistribution = documentTopicDistribution
    val intra =  clusters.map { cluster =>
      val (id, members) = cluster
      val idist = IntraClusterDistance(members)
      log.println(s"Intracluster distance LOV ($id) = $idist")
      idist
    }.sum / clusters.size

    // Calculate inter-cluster distance between each cluster
    InterClusterDistance.documentTopicDistribution = documentTopicDistribution
    val inter = InterClusterDistance(clusters)

    // output results for LOV
    o.println("LDA_S;" // method
      +"LOVTAGS" // number of topics
      +k+";" // number of topics
      +i+";" // repetition
      +intra+";" // Intra-cluster distance
      +inter+";" // Inter-cluster distance
      +intra/inter // I ratio
    )
  }
}
