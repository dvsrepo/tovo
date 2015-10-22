package es.upm.oeg.tovo.corpus

import com.hp.hpl.jena.rdf.model.Model
import es.upm.oeg.tovo.domain.Dictionary
import org.apache.jena.riot.RDFDataMgr

import scala.collection.mutable.ArrayBuffer

import scala.collection.JavaConversions._
/**
 * Created by dvilasuero on 28/09/15.
 */
object LodThemes {
  val path = "/Users/oeg/dev/tovo/lovBowCorpusBabelfied.txt";
  val d = Dictionary(path)

  // Load LOV corpus to extract clusters and cluster members
  val mLOD: Model = RDFDataMgr.loadDataset("src/test/corpus/evaluation-corporat/vocabs-subjects-lod.nt").getDefaultModel
  val clusterTagLOD = mLOD.createProperty("http://tovo.linkeddata.es/def/is-subject-of")
  val clusters = scala.collection.mutable.Map[String,ArrayBuffer[String]]()
  // get all values for tags (our clusters ids)
  mLOD.listResourcesWithProperty(clusterTagLOD).toList.foreach { r =>
    // get URI
    val URI = r.getURI
    val list = ArrayBuffer[String]()
    // get all vocabs for each tag (our cluster members)

    mLOD.listObjectsOfProperty(r,clusterTagLOD).toList.foreach { vocab =>
      if(mLOD.listSubjectsWithProperty(clusterTagLOD, vocab).size <= 1) {

        list += vocab.asResource().getURI
      }

    }
    // map of clusters and cluster members
    if(list.size > 1) clusters(URI) = list
  }
  clusters
}
