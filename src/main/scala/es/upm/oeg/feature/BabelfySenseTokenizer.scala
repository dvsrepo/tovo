package es.upm.oeg.feature

import es.upm.oeg.cidercl.util.StringTools
import it.uniroma1.lcl.jlt.util.Language

import it.uniroma1.lcl.babelfy.core.Babelfy
import scala.collection.JavaConversions._

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Tokenizer that sends the text to Babelfy and returns the set of found Babelnet senses
 */
object BabelfySenseTokenizer {
  def apply (text: String, confidence: Double): Seq[String] = {

    val q = text
      .replace("class", " ").replace("property", " ").replace("ontology", " ").replace("object", " ") // TODO remove more stop words?
      .split(" ").filter(CommonTokenizer.isValid)
      .map(StringTools.splitCamelCase(_).toLowerCase).mkString(". ")
    val tokens = new Babelfy().babelfy(q, Language.EN).toList.map {
      x  => (x.getBabelNetURL, x.getCoherenceScore)
    }.filter(_._2 >= confidence).map(_._1).filter(_.length >0)
    tokens
  }
}
