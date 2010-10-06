package org.opentox.service.ontology;

import java.io.File;
import java.io.Serializable;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;


/**
 * Can be queried by ?subject=""&predicate=""&object=""
 * @author nina
 *
 * @param <T>
 */
public class TDBOntologyResource<T extends Serializable> extends AbstractOntologyResource {

	protected String directory = String.format("%s/tdb",System.getProperty("java.io.tmpdir")); 
	
	public TDBOntologyResource() {
		super();
	}

	protected Model createOntologyModel(boolean init) throws ResourceException {
		File dir  = new File(directory);
		if (!dir.exists()) {
			dir.mkdir();
			try {
				new File(directory+"/fixed.opt").createNewFile();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		Model ontology = TDBFactory.createModel(directory) ;
		if (init && (ontology.size()==0)) readOntologies(ontology);
		ontology.setNsPrefix( "ot", "http://www.opentox.org/api/1.1#");
		ontology.setNsPrefix( "ota", "http://www.opentox.org/algorithmTypes.owl#" );
		ontology.setNsPrefix( "otee", "http://www.opentox.org/echaEndpoints.owl#" );
		ontology.setNsPrefix( "owl", OWL.NS );
		ontology.setNsPrefix( "dc", DC.NS );
		ontology.setNsPrefix( "bx", "http://purl.org/net/nknouf/ns/bibtex#" );
		ontology.setNsPrefix( "bo", "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#" );		
		/*
		if (ontology == null) ontology = TDBFactory.createModel(directory) ;
		if (ontology.size()==0) readOntologies();
		*/
		return ontology;
	}

	/*
	@Override
	protected Representation delete(Variant variant) throws ResourceException {
		Model ontology = createOntologyModel(false);
		try {
		if (ontology!=null) deleteModel(ontology);
		} catch (Exception x) {
			
		} finally {
			ontology.close();

		}
		return get(variant);
	}
	*/

}
