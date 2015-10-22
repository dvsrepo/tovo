package es.upm.oeg.tovo.corpus

import java.io.{PrintWriter, File}

import com.hp.hpl.jena.ontology.OntModelSpec
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.upm.oeg.feature.{LuceneTokenizer, BabelfySenseTokenizer}
import es.upm.oeg.tovo.domain.Ontology
import es.upm.oeg.cidercl.extraction.OntologyExtractor
import scala.collection.JavaConversions._

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Creating a corpus from LOV vocabularies
 */
object Lov {
  val confidenceBabelfy = 0.2
  for (x <- 0.1 to 0.9 by 0.1) {
    val corpusLovBabelfied = new PrintWriter(new File(s"src/test/corpus/lov-senses-corpus-conf-$x.txt"))
    val downloadURL = "src/test/corpus/lov.nq"
    val d = OntologyExtractor.createOntologicalModelFromQuads(downloadURL)
    d.removeNamedModel("http://lov.okfn.org/dataset/lov")
    val corpus = d.listNames().toList.map {uri =>

      println(s"Started processing URI: $uri");
      // TODO check the effect of adding reasoning
      val m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, d.getNamedModel(uri))
      val text = OntologyExtractor.getAllLabelsAndComments(m, "en").mkString(" ")
      val senseTokens = BabelfySenseTokenizer(text, x)

      if(!senseTokens.isEmpty)  corpusLovBabelfied.println(s"${uri}\t${senseTokens.mkString(" ")}")
      println(s"Finished processing URI: $uri")
    }
    corpusLovBabelfied.close()
    corpus
  }
}
