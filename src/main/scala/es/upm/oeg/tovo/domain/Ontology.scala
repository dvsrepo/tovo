package es.upm.oeg.tovo.domain

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * An ontology expressed in RDFS or OWL
 * @param iri (internationalized resource identifier) string of UNICODE characters used to identify the ontology
 */
//  url: String, metadata: Metadata, bagOfWords: Seq[String], topics: Seq[Topic]
case class Ontology(iri: String, text: String, bagOfWords: Seq[String]) extends Serializable {

}
