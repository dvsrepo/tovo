package es.upm.oeg.cidercl.extraction;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.log4j.Logger;

import java.util.*;


/**
 * Class to extract different parts of the ontological context of an ontology
 * entity.
 *
 * @author OEG group (Universidad Polit�cnica de Madrid) and SID group
 *         (Universidad de Zaragoza)
 *
 */
public class OntologyExtractor {

	private static String namespaceRDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private static String namespaceOWL = "http://www.w3.org/2002/07/owl#";


	private static Logger log = Logger.getLogger(OntologyExtractor.class);

	public static OntModel createOntologicalModel(String ontology) {
		final Model model = ModelFactory.createDefaultModel();

		RDFDataMgr.parse(new StreamRDFBase() {
			@Override
			public void triple(Triple triple) {

				model.getGraph().add(triple);
			}

			@Override
			public void quad(Quad quad) {

				model.getGraph().add(quad.asTriple());
			}

		}, ontology);

		return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_TRANS_INF, model);

	}

    // Loads a set of ontologies coming from an nquad file
    public static Dataset createOntologicalModelFromQuads(String ontology) {
        Dataset d = RDFDataMgr.loadDataset(ontology);
        return d;
    }

    public static String getComment(OntModel model, Object uri, String language) {
		String comment = "";
		OntResource r = model.getOntResource((String) uri);
		if (r.getComment(language) != null)
			comment = r.getComment(language);
		return comment;
	}

	public static ArrayList<String> getComments(OntModel model, Object uri,
			String language) {
		ArrayList<String> comments = new ArrayList<String>();

		// Get labels of URI
		OntResource r = model.getOntResource((String) uri);

		for (ExtendedIterator<RDFNode> i = r.listComments(language); i
				.hasNext();) {
			String comm = i.next().asNode().getLiteralLexicalForm();
			comments.add(comm);
		}

		return comments;
	}

    // New getLabel getting all literals
    public static ArrayList<String> getLabels(OntModel model, Object uri, String language) {
        ArrayList<String> synonyms = new ArrayList<>();

        // Get lexical descriptions attached to this URI
        OntResource r = model.getOntResource((String) uri);
        for (StmtIterator i = r.listProperties(); i.hasNext();) {
            RDFNode n = i.next().getObject();

            if(n.isLiteral()) {

                if(n.asLiteral().getLanguage().equals(language) || n.asLiteral().getLanguage().isEmpty()  ) {

                    String syn = n.asLiteral().getLexicalForm();
                    synonyms.add(syn);
                }

            }
        }
        return synonyms;

    }

	public static ArrayList<String> getAllLabelsAndComments(OntModel model, String lang) {


		ArrayList<String> tokens = new ArrayList<String>();

        if(!model.listClasses().hasNext()) {
            for (ResIterator iClasses = model.listSubjectsWithProperty(RDF.type, RDFS.Class); iClasses
                    .hasNext(); ) {

                String uri = iClasses.next().getURI();

                if (uri != null) {
                    tokens.addAll(getLabels(model, uri, lang));
                    tokens.add(getUriFragment(model, uri));
                }
            }
        } else {
            // Iterate over classes and get their labels and comments
            for (ExtendedIterator<OntClass> iClasses = model.listClasses(); iClasses
                    .hasNext();) {

                String uri = iClasses.next().getURI();

                if (uri != null) {
                    tokens.addAll(getLabels(model, uri, lang));
                    tokens.add(getUriFragment(model, uri));

                }
            }
        }

		// Iterate over properties and get their labels and comments
        if(!model.listClasses().hasNext()) {

            for (ResIterator iClasses = model.listSubjectsWithProperty(RDF.type, RDF.Property); iClasses
                    .hasNext(); ) {
                String uri = iClasses.next().getURI();

                if (uri != null) {
                    tokens.addAll(getLabels(model, uri, lang));
                    tokens.add(getUriFragment(model, uri));
                }
            }
        } else if(model.listAllOntProperties().hasNext()) {

            for (ExtendedIterator<OntProperty> iProp = model.listAllOntProperties(); iProp.hasNext(); ) {

                String uri = iProp.next().getURI();

                if (uri != null) {
                    tokens.addAll(getLabels(model, uri, lang));
                    tokens.add(getUriFragment(model, uri));
                }
            }
        }
		// Iterate over individuals and get their labels and comments
		for (ExtendedIterator<Individual> iInd = model.listIndividuals();
		iInd.hasNext();){
            String uri = iInd.next().getURI();

		    if (uri != null) {
		        tokens.addAll(getLabels(model, uri, null));
                tokens.add(getUriFragment(model, uri));
		    }
        }
        if(tokens.isEmpty()) {
            // Extract lexical components from all statements only if nothing else available
            for (StmtIterator iSt = model.listStatements(); iSt.hasNext(); ) {
                String uri = iSt.next().getSubject().getURI();
                if (uri != null) {
                    tokens.addAll(getLabels(model, uri, lang));
                    tokens.add(getUriFragment(model, uri));
                }
            }
        }
		return tokens;
	}

	public static String getUriFragment(OntModel model, String uri) {

		OntResource r = model.getOntResource(uri);
        String fragment = uri.substring(uri.lastIndexOf('#') + 1, uri.length());
        if(fragment.isEmpty())
            fragment = uri.substring(uri.lastIndexOf('/') + 1, uri.length());
		return !r.getLocalName().isEmpty()? r.getLocalName() : fragment ;

	}

	public static ArrayList<String> getAllUriFragments(OntModel model) {

		ArrayList<String> tokens = new ArrayList<String>();

		// Iterate over classes and get their uri fragments
		for (ExtendedIterator<OntClass> iClasses = model.listClasses(); iClasses
				.hasNext();) {

			String uri = iClasses.next().getURI();
			if (uri != null)
				tokens.add(getUriFragment(model, uri));
		}

		// Iterate over properties and get their uri fragments
		for (ExtendedIterator<OntProperty> iProp = model.listAllOntProperties(); iProp
				.hasNext();) {

			String uri = iProp.next().getURI();
			if (uri != null)
				tokens.add(getUriFragment(model, uri));

		}

		return tokens;
	}

	public static ArrayList<String> getLabelLanguages(OntModel model, Object uri) {

		ArrayList<String> languages = new ArrayList<String>();
		OntResource resource = model.getOntResource((String) uri);

		for (ExtendedIterator<RDFNode> i = resource.listLabels(null); i
				.hasNext();) {
			Literal label = i.next().asLiteral();
			if (label.getLanguage() != null && label.getLanguage() != "")
				languages.add(label.getLanguage());
		}
		return languages;

	}

	public static ArrayList<String> getCommentLanguages(OntModel model,
			Object uri) {

		ArrayList<String> languages = new ArrayList<String>();
		OntResource resource = model.getOntResource((String) uri);

		for (ExtendedIterator<RDFNode> i = resource.listComments(null); i
				.hasNext();) {
			Literal comment = i.next().asLiteral();
			if (comment.getLanguage() != null && comment.getLanguage() != "")
				languages.add(comment.getLanguage());
		}
		return languages;

	}

	public static HashSet<String> getAllLanguages(OntModel model) {

		HashSet<String> languages = new HashSet<String>();

		// Iterate over classes and get the language of their labels and
		// comments
		for (ExtendedIterator<OntClass> iClasses = model.listClasses(); iClasses
				.hasNext();) {

			String uri = iClasses.next().getURI();
			if (uri != null) {
				languages.addAll(getLabelLanguages(model, uri));
				languages.addAll(getCommentLanguages(model, uri));
			}
		}

		// Iterate over properties and get the language of their labels and
		// comments
		for (ExtendedIterator<OntProperty> iProp = model.listAllOntProperties(); iProp
				.hasNext();) {

			String uri = iProp.next().getURI();
			if (uri != null) {
				languages.addAll(getLabelLanguages(model, uri));
				languages.addAll(getCommentLanguages(model, uri));
			}
		}

		return languages;

	}

	public static ArrayList<String> getSuperterms(OntModel model, Object uri) {

		ArrayList<String> superterms = new ArrayList<String>();

		OntResource or = model.getOntResource((String) uri);
		if (or.isClass()) {
			OntClass concept = or.as(OntClass.class);
			for (ExtendedIterator<OntClass> i = concept.listSuperClasses(); i
					.hasNext();) {
				OntClass superclass = i.next();
				if (superclass.getURI() != null
						&& !superclass.getURI().equals(namespaceOWL + "Thing")
						&& !superclass.getURI().equals(
								namespaceRDFS + "Resource"))
					superterms.add(superclass.getURI());
			}
		} else if (or.isProperty()) {
			OntProperty property = or.as(OntProperty.class);
			for (ExtendedIterator<? extends OntProperty> i = property
					.listSuperProperties(); i.hasNext();) {
				OntProperty superproperty = i.next();
				if (superproperty.getURI() != null)
					superterms.add(superproperty.getURI());
			}
		}
		// in case the term itself is in the list, it is removed
		superterms.remove(uri);
		return superterms;

	}

	public static ArrayList<String> getDirectSuperterms(OntModel model,
			Object uri) {

		ArrayList<String> superterms = new ArrayList<String>();

		OntResource or = model.getOntResource((String) uri);
		if (or.isClass()) {
			OntClass concept = or.as(OntClass.class);
			for (ExtendedIterator<OntClass> i = concept.listSuperClasses(true); i
					.hasNext();) { // this 'true' restricts search to direct
									// superclasses
				OntClass superclass = i.next();
				if (superclass.getURI() != null
						&& !superclass.getURI().equals(namespaceOWL + "Thing")
						&& !superclass.getURI().equals(
								namespaceRDFS + "Resource"))
					superterms.add(superclass.getURI());
			}
		} else if (or.isProperty()) {
			OntProperty property = or.as(OntProperty.class);
			for (ExtendedIterator<? extends OntProperty> i = property
					.listSuperProperties(true); i.hasNext();) { // this 'true'
																// restricts
																// search to
																// direct
																// superproperties
				OntProperty superproperty = i.next();
				if (superproperty.getURI() != null)
					superterms.add(superproperty.getURI());
			}
		}
		// in case the term itself is in the list, it is removed
		superterms.remove(uri);
		return superterms;

	}

	public static ArrayList<String> getSubterms(OntModel model, Object uri) {

		ArrayList<String> subterms = new ArrayList<String>();

		OntResource or = model.getOntResource((String) uri);
		if (or.isClass()) {
			OntClass concept = or.as(OntClass.class);
			for (ExtendedIterator<OntClass> i = concept.listSubClasses(); i
					.hasNext();) {
				OntClass subclass = i.next();
				if (subclass.getURI() != null
						&& !subclass.getURI().equals(namespaceOWL + "Nothing"))
					subterms.add(subclass.getURI());
			}
		} else if (or.isProperty()) {
			OntProperty property = or.as(OntProperty.class);
			for (ExtendedIterator<? extends OntProperty> i = property
					.listSubProperties(); i.hasNext();) {
				OntProperty subproperty = i.next();
				if (subproperty.getURI() != null)
					subterms.add(subproperty.getURI());
			}
		}
		// in case the term itself is in the list, it is removed
		subterms.remove(uri);
		return subterms;

	}

	public static ArrayList<String> getDirectSubterms(OntModel model, Object uri) {

		ArrayList<String> subterms = new ArrayList<String>();

		OntResource or = model.getOntResource((String) uri);
		if (or.isClass()) {
			OntClass concept = or.as(OntClass.class);
			for (ExtendedIterator<OntClass> i = concept.listSubClasses(true); i
					.hasNext();) { // this 'true' restricts search to direct
									// subclasses
				OntClass subclass = i.next();
				if (subclass.getURI() != null
						&& !subclass.getURI().equals(namespaceOWL + "Nothing"))
					subterms.add(subclass.getURI());
			}
		} else if (or.isProperty()) {
			OntProperty property = or.as(OntProperty.class);
			for (ExtendedIterator<? extends OntProperty> i = property
					.listSubProperties(true); i.hasNext();) { // this 'true'
																// restricts
																// search to
																// direct
																// subproperties
				OntProperty subproperty = i.next();
				if (subproperty.getURI() != null)
					subterms.add(subproperty.getURI());
			}
		}
		// in case the term itself is in the list, it is removed
		subterms.remove(uri);
		return subterms;

	}

	public static ArrayList<String> getPropertiesOfClass(OntModel model,
			Object uri) {

		ArrayList<String> propertiesOfClass = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);
		OntClass concept = r.as(OntClass.class);

		// BOTTLENECK HERE, this Jena method takes, sometimes, a significant
		// amount of time
		for (ExtendedIterator<OntProperty> i = concept
				.listDeclaredProperties(false); i.hasNext();) { // boolean in
																// 'listDeclaredProperties':
																// if true,
																// restrict the
																// properties
																// returned to
																// those
																// directly
																// associated
																// with this
																// class.
			Property prop = i.next();
			if (prop.getURI() != null)
				propertiesOfClass.add(prop.getURI());
		}
		return propertiesOfClass;
	}

	public static ArrayList<String> getDirectPropertiesOfClass(OntModel model,
			Object uri) {

		ArrayList<String> propertiesOfClass = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);
		OntClass concept = r.as(OntClass.class);

		// BOTTLENECK HERE, this Jena method takes, sometimes, a significant
		// amount of time
		for (ExtendedIterator<OntProperty> i = concept
				.listDeclaredProperties(true); i.hasNext();) { // boolean in
																// 'listDeclaredProperties':
																// if true,
																// restrict the
																// properties
																// returned to
																// those
																// directly
																// associated
																// with this
																// class.
			Property prop = i.next();
			if (prop.getURI() != null)
				propertiesOfClass.add(prop.getURI());
		}
		return propertiesOfClass;
	}

	public static ArrayList<String> getDomainsOfProperty(OntModel model,
			Object uri) {

		ArrayList<String> domains = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);

		if (r.isProperty()) {
			OntProperty property = r.as(OntProperty.class);
			for (ExtendedIterator<? extends OntResource> i = property
					.listDeclaringClasses(false); i.hasNext();) { // boolean in
																	// 'listDeclaringClasses':
																	// if true,
																	// restrict
																	// the
																	// properties
																	// returned
																	// to those
																	// directly
																	// associated
																	// with this
																	// class.
				OntClass domain = (OntClass) i.next();
				if (domain.getURI() != null)
					domains.add(domain.getURI());
			}
		}
		return domains;
	}

	public static ArrayList<String> getDirectDomainsOfProperty(OntModel model,
			Object uri) {

		ArrayList<String> domains = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);

		if (r.isProperty()) {
			OntProperty property = r.as(OntProperty.class);
			for (ExtendedIterator<? extends OntResource> i = property
					.listDeclaringClasses(true); i.hasNext();) { // boolean in
																	// 'listDeclaringClasses':
																	// if true,
																	// restrict
																	// the
																	// properties
																	// returned
																	// to those
																	// directly
																	// associated
																	// with this
																	// class.
				OntClass domain = (OntClass) i.next();
				if (domain.getURI() != null)
					domains.add(domain.getURI());
			}
		}
		return domains;
	}

	public static ArrayList<String> getRangesOfObjectProperty(OntModel model,
			Object uri) {

		ArrayList<String> ranges = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);

		if (r.isObjectProperty()) {
			OntProperty property = r.as(OntProperty.class);
			for (ExtendedIterator<? extends OntResource> i = property
					.listRange(); i.hasNext();) {
				OntClass range = (OntClass) i.next();
				if (range.getURI() != null)
					ranges.add(range.getURI());
			}
		}
		return ranges;
	}

	public static ArrayList<String> getRangesOfDatatypeProperty(OntModel model,
			Object uri) {

		ArrayList<String> ranges = new ArrayList<String>();

		OntResource r = model.getOntResource((String) uri);

		if (r.isDatatypeProperty()) {
			OntProperty property = r.as(OntProperty.class);
			for (ExtendedIterator<? extends OntResource> i = property
					.listRange(); i.hasNext();) {
				OntClass range = (OntClass) i.next();
				if (range.getURI() != null)
					ranges.add(range.getURI());
			}
		}
		return ranges;
	}

	public static ArrayList<String> getRelatedTermsOfClass(OntModel model,
			Object uri, int depth) {

		ArrayList<String> relatedterms = new ArrayList<String>();
		if (depth > 0) {

			ArrayList<String> properties = getPropertiesOfClass(model, uri);
			ArrayList<String> ranges = new ArrayList<String>();

			for (int i = 0; i < properties.size(); i++) {

				OntResource r = model.getOntResource(properties.get(i));
				if (r.isObjectProperty()) {
					// add ranges of the properties of the class
					ranges.addAll(getRangesOfObjectProperty(model,
							properties.get(i)));
					relatedterms.addAll(ranges);
					// explore other ranges at deeper level (recursively)
					for (int j = 0; j < ranges.size(); j++) {
						relatedterms.addAll(getRelatedTermsOfClass(model,
								ranges.get(j), depth - 1));
					}
				}

			}
		}
		return relatedterms;

	}

	public static ArrayList<String> getEquivalentTerms(OntModel model,
			Object uri) {

		ArrayList<String> eqterms = new ArrayList<String>();

		OntResource or = model.getOntResource((String) uri);
		or.listSameAs();
		for (ExtendedIterator<?> i = or.listSameAs(); i.hasNext();) {
			OntResource eqterm = (OntResource) i.next();
			if (eqterm.getURI() != null)
				eqterms.add(eqterm.getURI());
		}

		// NOTE: this gives an error if the equivalent class is an external
		// reference. The way to avoid this is to
		// create the model with OWL inference activated
		if (or.isClass()) {
			OntClass concept = or.as(OntClass.class);
			for (ExtendedIterator<?> i = concept.listEquivalentClasses(); i
					.hasNext();) {
				OntResource eqclass = (OntResource) i.next();
				if (eqclass.getURI() != null)
					eqterms.add(eqclass.getURI());
			}
		} else if (or.isProperty()) {
			OntProperty property = or.as(OntProperty.class);
			for (ExtendedIterator<? extends OntProperty> i = property
					.listEquivalentProperties(); i.hasNext();) {
				OntProperty eqproperty = i.next();
				if (eqproperty.getURI() != null)
					eqterms.add(eqproperty.getURI());
			}

		}
		return eqterms;
	}

	public static Map<String, Collection<String>> getBoWClasses(OntModel model, String lang) {

		Map<String, Collection<String>> map = new HashMap<>();
		System.out.println(model.getBaseModel().listSubjectsWithProperty(RDF.type, RDFS.Class).toList());
		// Iterate over classes and get their labels and comments
		if(!model.listClasses().hasNext()) {
			for (ResIterator iClasses = model.listSubjectsWithProperty(RDF.type, RDFS.Class); iClasses
					.hasNext();) {

				String uri = iClasses.next().getURI();
				if (uri != null) {
					ArrayList<String> temp = new ArrayList<>();
					temp.addAll(getLabels(model, uri, lang));
					temp.addAll(getComments(model, uri, lang));
					temp.add(getUriFragment(model, uri));
					try {
						// get equivalent terms
						getEquivalentTerms(model, uri).forEach(s -> {
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get sub classes
						getDirectSubterms(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get super classes
						getDirectSuperterms(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get direct properties
						getDirectPropertiesOfClass(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get
						getPropertiesOfClass(model, uri).forEach(s -> {
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
					} catch (Exception e) {
						// carry on
						log.error(e);
					}


					map.put(uri,temp);
					log.info("Added BoW " + temp + " to " + uri);

				}
			}
		} else {
			for (ExtendedIterator<OntClass> iClasses = model.listClasses(); iClasses
					.hasNext();) {

				String uri = iClasses.next().getURI();
				if (uri != null) {
					ArrayList<String> temp = new ArrayList<>();
					temp.addAll(getLabels(model, uri, lang));
					temp.addAll(getComments(model, uri, lang));
					temp.add(getUriFragment(model, uri));
					try {
						// get equivalent terms
						getEquivalentTerms(model, uri).forEach(s -> {
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get sub classes
						getDirectSubterms(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get super classes
						getDirectSuperterms(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get direct properties
						getDirectPropertiesOfClass(model, uri).forEach(s->{
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
						// get
						getPropertiesOfClass(model, uri).forEach(s -> {
							temp.addAll(getLabels(model, s, lang));
							temp.addAll(getComments(model, s, lang));
							temp.add(getUriFragment(model, s));
						});
					} catch (Exception e) {
						// carry on
						log.error(e);
					}


					map.put(uri,temp);
					log.info("Added BoW " + temp + " to " + uri);

				}
			}
		}

		return map;
	}

}
