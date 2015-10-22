package es.upm.oeg.topicmodel

import es.upm.oeg.tovo.domain.Dictionary
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.mllib.clustering.{LDAModel, LDA, DistributedLDAModel}
import scala.collection.mutable
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.rdd.RDD


/**
 * Latent Dirichlet Allocation (LDA) algorithm wrapper for Spark 1.5.0
 */
class LDAWrapper () extends Serializable {

  val conf = new SparkConf().
    setMaster("local[*]").
    setAppName("LDA spark for tovo").
    set("spark.executor.memory", "16g").
    set("spark.driver.maxResultSize","0")
  val sc = new SparkContext(conf)

  def corpus( d : Dictionary) : RDD[(Long, Vector)] = {

    // Convert documents into term count vectors
    val documents: Seq[(Long, Vector)] =
      d.documents.zipWithIndex.map { case (doc, id) =>
        val (uri, terms) = doc
        val counts = new mutable.HashMap[Int, Double]()
        terms.foreach { id =>
          counts(id) = counts.getOrElse(id, 0.0) + 1.0
        }

        (id.toLong, Vectors.sparse(d.dict.size, counts.toSeq))
      }
    sc.parallelize(documents)
  }

  def run(corpus: RDD[(Long, Vector)], k : Int,  maxIterations: Int) : LDAModel = {
    // Set LDA parameters
    val lda = new LDA().setK(k).setMaxIterations(maxIterations).setCheckpointInterval(1).run(corpus)
    lda
  }

}
