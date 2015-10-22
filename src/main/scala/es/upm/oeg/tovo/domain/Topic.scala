package es.upm.oeg.tovo.domain

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * A topic that can be associated to LODsets and Ontologies
 * @param iri (uniform resource identifier) string of characters used to identify the topic (probably not dereferenceable)
 * @param metadata
 * @param topics
 */
case class Topic(iri: String, metadata: Metadata, topics: Seq[Topic]) extends Serializable {

}
