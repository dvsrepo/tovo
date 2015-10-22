package es.upm.oeg.topicmodel

import java.io.{File, PrintWriter}

import breeze.linalg.normalize
import es.upm.oeg.tovo.domain.Dictionary
import org.apache.spark.mllib.linalg.Matrix
import breeze.linalg.{DenseMatrix => BDM, DenseVector => BDV, argmax, argtopk, normalize, sum}
import breeze.numerics.{exp, lgamma}
import org.slf4j.LoggerFactory

import scala.collection._
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Random


/**
 * Created by dvilasuero on 05/08/15.
 */

/**
 * Experimental implementation of BTM (Biterm topic modelling) [X. Yan et al WWW2013]
 */
class BTM(val alpha: Double, val beta: Double, val k:Int, val maxIterations:Int) {

  val log = LoggerFactory.getLogger(classOf[BTM])
  var documentTopicDistribution = scala.collection.mutable.Map[String,Array[Double]]()
  private var words:Array[String] = null
  private var documents:Array[(String, Array[Int])] = null
  private var biterms:Array[(Int, Int)] = null
  private var m:Int = 0
  private var b_z:Array[Int] = null
  private var n_z:Array[Long] = null
  private var n_w_z:Array[Long] = null
  private var theta:Array[Double] = null
  private var phi:Array[Double] = null
  private var table:Array[Double] = null

  def load(d:Dictionary): Unit = {
    m = d.m
    words = new Array(m)
    d.dict.iterator.foreach{ case(word, i) =>
      words(i) = word
    }
    documents = d.documents
    biterms = d.biterms
    b_z = new Array(biterms.length)
    n_z = new Array(k)
    n_w_z = new Array(k * m)
    theta = new Array(k)
    phi = new Array(k * m)
    table = new Array(k)
    println(s" Executing BTM with parameters |B|: ${biterms.length}, K: $k, M: $m")
  }

  def load(file:String): Unit = {

    log.info(s"Started loading $file")
    val dict = mutable.HashMap[String,Int]()
    val count = Iterator.from(0)
    val buf1 = mutable.ArrayBuffer[(String, Array[Int])]()
    val buf2 = mutable.ArrayBuffer[(Int, Int)]()
    val s = Source.fromFile(file)

    try {
      s.getLines().foreach{ l =>
        if(!l.isEmpty) {

          val row = l.replace("\t", " ").stripLineEnd.split(" ")
          if(!row.tail.isEmpty) {
            val d = row.tail.map(word => dict.getOrElseUpdate(word, count.next))
            buf1 += ((row.head, d))
            buf2 ++= getBiterms(d)
          } else {
            log.info(s"Document ${row.head} has 0 tokens")
          }

        }
      }
    } finally {
      s.close()
    }

    m = count.next

    words = new Array(m)
    dict.iterator.foreach{ case(word, i) =>
      words(i) = word
    }
    documents = buf1.toArray
    biterms = buf2.toArray
    b_z = new Array(biterms.length)
    n_z = new Array(k)
    n_w_z = new Array(k * m)
    theta = new Array(k)
    phi = new Array(k * m)
    table = new Array(k)

    log.info(s"|B|: ${biterms.length}, K: $k, M: $m")

  }


  def estimate {
    Iterator.continually(0L).copyToArray(n_z)
    Iterator.continually(0L).copyToArray(n_w_z)

    biterms.iterator.zipWithIndex.foreach { case (b, i) =>
      setTopic(b, i, Random.nextInt(k))
    }

    Iterator.range(0, maxIterations).foreach { n =>

      biterms.iterator.zipWithIndex.foreach { case (b, i) =>
        unsetTopic(b, b_z(i))
        setTopic(b, i, sampleTopic(b))
      }

    }
    calcTheta
    calcPhi
  }

  def report(path: String): Unit ={

    val o1 = new PrintWriter(new File(s"$path-topics.k$k"))
    Iterator.range(0, k).foreach { z =>
      val ws = (0 until m).sortBy(w => -phi(w*k+z)).take(20)
      o1.println(ws.map(words).mkString(" "))

    }
    o1.close

    val o2 = new PrintWriter(new File(s"$path-words.k$k"))
    Iterator.range(0, k).foreach { w =>
      val p_w_z = Iterator.range(w*k, (w+1)*k-1).map(phi)
      val weight = p_w_z.zip(theta.iterator).map { case (p, q) => p * q }.toArray
      val h = 1.0 / weight.sum
      val p_z_w = weight.iterator.map(_ * h)
      o2.println(s"${words(w)}\t" ++ p_z_w.mkString("\t"))

    }
    o2.close();


    val o3 = new PrintWriter(new File(s"$path-documents.k$k"))
    documents.foreach { case (id, d) =>
      val bs = getBiterms(d).toArray
      val hs = bs.map { b =>

        val (w1, w2) = b

        1.0 / Iterator.range(0, k).map { z =>
          theta(z) * phi(w1*k+z) * phi(w2*k+z)
        }.sum / bs.length
      }

      val p_z_d = Iterator.range(0, k).map { z =>
        bs.iterator.zip(hs.iterator).map { case (b, h) =>
          val (w1, w2) = b
          theta(z) * phi(w1*k+z) * phi(w2*k+z) * h
        }.sum
      }

      documentTopicDistribution(id) = p_z_d.toArray
      o3.println(s"${id}\t${documentTopicDistribution(id).mkString(" ")}\t")

    }
    o3.close

  }

   def describeTopics(maxTermsPerTopic: Int): Array[(Array[Int], Array[Double])] = {

     Range(0, k).map { topicIndex =>
       val terms : Array[Int] = (0 until m).sortBy(w => -phi(w*k+topicIndex)).take(maxTermsPerTopic).toArray
       val termWeights : Array[Double] = terms.map(term => -phi(term*k+topicIndex) )
       (terms, termWeights)
     }.toArray
  }

  private def getBiterms(d:Array[Int]):Iterator[(Int, Int)] = {
    d.toSeq.combinations(2).map { case Seq(w1, w2) =>
      if (w1 < w2) (w1, w2) else (w2, w1)
    }
  }

  private def setTopic(b:(Int, Int), i:Int, z:Int) {
    val (w1, w2) = b
    b_z(i) = z
    n_z(z) += 1
    n_w_z(w1*k+z) += 1
    n_w_z(w2*k+z) += 1
  }

  private def unsetTopic(b:(Int, Int), z:Int) {
    val (w1, w2) = b
    n_z(z) -= 1
    n_w_z(w1*k+z) -= 1
    n_w_z(w2*k+z) -= 1
  }

  private def sampleTopic(b:(Int, Int)):Int = {
    val (w1, w2) = b
    Iterator.range(0, k).map { z =>
      val h = m / (n_z(z) * 2 + m * beta)
      val p_z_w1 = (n_w_z(w1*k+z) + beta) * h
      val p_z_w2 = (n_w_z(w2*k+z) + beta) * h
      (n_z(z) + alpha) * p_z_w1 * p_z_w2
    }.scanLeft(0.0)(_ + _).drop(1).copyToArray(table)
    val r = Random.nextDouble * table.last
    table.indexWhere(_ >= r)
  }

  private def calcTheta {
    Iterator.range(0, k).map { z =>
      (n_z(z) + alpha) / (biterms.length + k * alpha)
    }.copyToArray(theta)
  }

  private def calcPhi {
    Iterator.range(0, m).flatMap { w =>
      Iterator.range(0, k).map { z =>
        (n_w_z(w*k+z) + beta) / (n_z(z) * 2 + m * beta)
      }
    }.copyToArray(phi)
  }
}

object BTM {
  def main(args:Array[String]) {
    val btm = new BTM(1.0 / 20, 0.01, 20, 200)
    btm.load(args(0))
    btm.estimate

  }



}
