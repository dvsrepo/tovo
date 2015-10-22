package es.upm.oeg.tovo.corpus

import com.hp.hpl.jena.rdf.model.Model
import es.upm.oeg.tovo.domain.Dictionary
import org.apache.jena.riot.RDFDataMgr
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
 * Created by dvilasuero on 24/09/15.
 */
object LovTags {

  val path = "src/test/corpus/lov-corpora/lov-contexts-clear.tsv";
  val d = Dictionary(path)
  // Load LOV corpus to extract clusters and cluster members
  val m: Model = RDFDataMgr.loadDataset("src/test/corpus/lov-metadata.n3").getDefaultModel
  val clusterTag = m.createProperty("http://www.w3.org/ns/dcat#keyword")
  val clusters = scala.collection.mutable.Map[String,ArrayBuffer[String]]()
  // get all values for tags (our clusters ids)
  m.listObjectsOfProperty(clusterTag).toList.foreach { r =>
    val tag = r.toString

    val list = ArrayBuffer[String]()
    // get all vocabs for each tag (our cluster members)
    m.listResourcesWithProperty(clusterTag, tag).toList.foreach { vocab =>

      if(m.listObjectsOfProperty(vocab,clusterTag).toList.size <= 1) {
        list += vocab.getURI
      } else {
        println(vocab)
      }
    }
    // map of clusters and cluster members
    clusters(tag) = list

  }
  println(clusters)
  clusters
}
