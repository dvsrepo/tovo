package es.upm.oeg.tovo.extractor


import java.io.{File, PrintWriter}
import com.hp.hpl.jena.ontology.OntModelSpec
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.upm.oeg.cidercl.extraction.OntologyExtractor
import es.upm.oeg.cidercl.util.StringTools
import es.upm.oeg.feature.CommonTokenizer
import scala.collection.JavaConversions._

/**
 * Created by dvilasuero on 22/09/15.
 * Extracts lexical components for each vocabulary in the LOV corpus and writes to individual file
 */
object LovTextExtractor {
  def main(args: Array[String]) {
    val lovURL = "src/test/corpus/lov.nq"
    val outputPath = "src/test/corpus/lov-corpora/"
    val writer = new PrintWriter(new File(s"${outputPath}lov-contexts-clear.tsv" ))
    val d = OntologyExtractor.createOntologicalModelFromQuads(lovURL)
    d.removeNamedModel("http://lov.okfn.org/dataset/lov")
    val corpus = d.listNames().toList.map {uri =>
      println(s"Started processing URI: $uri")
      // TODO check the effect of adding reasoning
      val m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF, d.getNamedModel(uri))
      val text = OntologyExtractor.getAllLabelsAndComments(m, "en").filter(CommonTokenizer.isValid)
      .map(StringTools.splitCamelCase(_).toLowerCase).mkString(" ")
      writer.write(s"$uri\t$text\n")

      println(s"Finished writing for URI: $uri")
    }
    writer.close()
  }

}
