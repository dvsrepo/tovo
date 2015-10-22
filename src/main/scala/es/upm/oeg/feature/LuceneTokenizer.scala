package es.upm.oeg.feature

import es.upm.oeg.cidercl.util.StringTools
import es.upm.oeg.tovo.feature.LuceneClassifier
import org.apache.commons.lang.StringUtils

import scala.collection.JavaConverters

/**
 * Created by cbadenes on 22/04/15.
 */
object LuceneTokenizer {

  def apply (text: String): Seq[String] = {

    JavaConverters.asScalaBufferConverter(LuceneClassifier
      .guessFromString(text.replace("class", " ").replace("property", " ").replace("ontology", " ").replace("object", " ")
      .split(" ").filter(CommonTokenizer.isValid).map(StringTools.splitCamelCase(_)).mkString(" ")))
      .asScala
      .toList
      .map(_.getStem)

  }


}
