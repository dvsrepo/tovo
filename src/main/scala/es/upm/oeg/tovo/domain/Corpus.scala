package es.upm.oeg.tovo.domain

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * A corpus
 * @param url (uniform resource locator) string of characters used to find either the corpus or information about it
 * @param metadata
 * @param topics
 */
case class Corpus(url: String, metadata: Metadata, topics: Seq[Topic]) extends Serializable {

}
