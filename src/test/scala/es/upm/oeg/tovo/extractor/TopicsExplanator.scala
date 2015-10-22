package es.upm.oeg.tovo.extractor

import java.io.{FileOutputStream, File}

import com.hp.hpl.jena.query.DatasetFactory
import com.hp.hpl.jena.rdf.model.ModelFactory
import org.apache.jena.riot.{RDFDataMgr}
import org.openjena.riot.RiotWriter
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by dvilasuero on 12/09/15.
 */
object TopicsExplanator {
  def main (args: Array[String]) {
    val file = "results/b2tm/50-8-topics.k50"
    val d = DatasetFactory.create()
    val s = Source.fromFile(file)
    var count = 0

    try {
      s.getLines().foreach{ l =>
        if(!l.isEmpty) {
          count += 1
          val row = l.replace("\t", " ").stripLineEnd.split(" ")
          val m = ModelFactory.createDefaultModel();
          val senses = row.tail.foreach{
            sense =>
              try {
                println(sense)
                m.read(sense.replace("http://babelnet.org/rdf/", "http://babelnet.org/rdf/data/")+"_Gloss1_EN?output=xml")
                m.read(sense)
              } catch {
                case e: Exception => println(e)
              }

          }
          d.addNamedModel(s"http://tovo.linkeddata.es/topic/id/${count.toString}", m)

        }
      }
    } finally {
      s.close()
    }

    // http://babelnet.org/rdf/data/s03854990n_Gloss1_EN?output=xml
    d.listNames().foreach {
      topic =>
        print(s"TOPIC\t ${topic}\t")
        d.getNamedModel(topic)
          .listObjectsOfProperty(d.getDefaultModel
          .createProperty("http://babelnet.org/model/babelnet#dbpediaCategory")).toSeq
          .foreach {
            category => print(s"${category}\t")
          }
        println
    }

    val out = new FileOutputStream(new File("topicsAsDBpediaCategories20.nq"))
    RiotWriter.writeNQuads(out,d.asDatasetGraph())

  }
}
