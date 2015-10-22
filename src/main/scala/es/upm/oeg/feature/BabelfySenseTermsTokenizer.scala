package es.upm.oeg.feature

import es.upm.oeg.cidercl.util.StringTools
import it.uniroma1.lcl.jlt.util.Language

import it.uniroma1.lcl.babelfy.core.Babelfy
import scala.collection.JavaConversions._

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Tokenizer that sends the text to Babelfy
 * and returns the set of found Babelnet senses plus the  generating term and the DBpedia URL if exists
 */
object BabelfySenseTermsTokenizer {
  def apply (text: String, confidence: Double): Seq[(String, String, String)] = {

    val q = text
      .replace("class", " ").replace("property", " ").replace("ontology", " ").replace("object", " ") // TODO remove more stop words?
      .split(" ").filter(CommonTokenizer.isValid)
      .map(StringTools.splitCamelCase(_).toLowerCase).mkString(". ")
    val tokens = new Babelfy().babelfy(q, Language.EN).toList.map {

      x =>
        val term = q.substring(x.getCharOffsetFragment.getStart, x.getCharOffsetFragment.getEnd + 1)

        (x.getBabelNetURL, term, x.getDBpediaURL)
    }.toSeq
    tokens
  }
}
