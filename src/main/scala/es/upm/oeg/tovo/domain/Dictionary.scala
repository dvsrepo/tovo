package es.upm.oeg.tovo.domain

import org.slf4j.LoggerFactory

import scala.collection.{Iterator, mutable}
import scala.io.Source


/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Dictionary belonging to a corpus, including the set of Biterms
 */
case class Dictionary (corpus : String) extends Serializable {

  var words:Array[String] = null
  var documents:Array[(String, Array[Int])] = null
  var biterms:Array[(Int, Int)] = null
  val dict = mutable.HashMap[String,Int]()
  var m:Int = 0


  def load(): Unit = {


    val count = Iterator.from(0)
    val buf1 = mutable.ArrayBuffer[(String, Array[Int])]()
    val buf2 = mutable.ArrayBuffer[(Int, Int)]()
    val s = Source.fromFile(corpus)

    try {
      s.getLines().foreach{ l =>
        if(!l.isEmpty) {

          val row = l.replace("\t", " ").stripLineEnd.split(" ")
          if(!row.tail.isEmpty) {
            val d = row.tail.map(word => dict.getOrElseUpdate(word, count.next))
            buf1 += ((row.head, d))
            buf2 ++= getBiterms(d)
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
    println(s"Vocabulary loaded with |biterms|: ${biterms.length}, |dictionary size|: $m")

  }
  private def getBiterms(d:Array[Int]):Iterator[(Int, Int)] = {
    d.toSeq.combinations(2).map { case Seq(w1, w2) =>
      if (w1 < w2) (w1, w2) else (w2, w1)
    }
  }

}
