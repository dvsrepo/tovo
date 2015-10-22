package es.upm.oeg.feature

import java.nio.charset.Charset

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
 * Created by cbadenes on 21/04/15.
 */
object CommonTokenizer {

  val log = LoggerFactory.getLogger(CommonTokenizer.getClass);

  def split (line: String): Seq[String] = {
    line.toLowerCase.split("\\s").filter(isValid)
  }

  def isValid (word: String): Boolean ={
    // !StandardAnalyzer.STOP_WORDS_SET.contains(word)
    word.length >= 4  && word.forall(java.lang.Character.isLetter) && !word.startsWith("htt") && word.forall(x=>isEncoded("US-ASCII",x))
  }

  def isEncoded (charset: String, letter: Char): Boolean ={
    Charset.forName(charset).newEncoder().canEncode(letter)
  }

  def printAll(tokens: RDD[Seq[String]]): RDD[Seq[String]] ={
    log.info("*"*20+" Tokenizer.split:")
    tokens.collect().foreach(x => log.info(s"·$x"))
    return tokens
  }


}
