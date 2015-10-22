package es.upm.oeg.tovo.domain

/**
 * Created by dvilasuero on 11/09/15.
 */
/**
 * Information about a resource
 * @param title A name given to the resource.
 * @param description A text describing the resource
 */
case class Metadata (title: String, description: String) extends Serializable{

  //TODO license, format, ....
}
