package es.upm.oeg.tovo.corpus

import java.io.{PrintWriter, File}

import com.hp.hpl.jena.ontology.OntModelSpec
import com.hp.hpl.jena.rdf.model.ModelFactory
import es.upm.oeg.feature.{BabelfySenseTermsTokenizer, LuceneTokenizer, BabelfySenseTokenizer}
import es.upm.oeg.tovo.domain.Ontology
import es.upm.oeg.cidercl.extraction.OntologyExtractor
import scala.collection.JavaConversions._
import scala.io.Source

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Creating a corpus from LOV vocabularies
 */
object LovFromFile {
  val fileCorpus = "src/test/corpus/lov-corpora/lov-contexts-clear.tsv"
  val s = Source.fromFile(fileCorpus)

  val corpusLov = Source.fromFile(fileCorpus).getLines().map { line =>
    val uri = line.split("\t").head
    val text = line.split("\t").tail.mkString(" ")
    (uri, text)
  }
  s.close()

  def main(args: Array[String]) {

    // This is used to create several files for different thresholds
    val confidenceList = List(0.0)

    confidenceList.foreach { confidence =>
      val corpusLov = Source.fromFile(fileCorpus).getLines().map { line =>
        val uri = line.split("\t").head
        val text = line.split("\t").tail.mkString(" ")
        (uri, text)
      }
      s.close()
      val corpusLovBabelfied = new PrintWriter(new File(s"src/test/corpus/lov-corpora/lov-senses-corpus-conf-$confidence.tsv"))
      corpusLov.foreach {vocab =>
        val (uri, text) = vocab
        val senseTokens = BabelfySenseTermsTokenizer(text, confidence)
        if(!senseTokens.isEmpty)  corpusLovBabelfied.println(s"${uri}\t${senseTokens.mkString(" ")}")
      }
      corpusLovBabelfied.close()
    }


  }

}
