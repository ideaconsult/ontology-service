package org.opentox.service.ontology;

import java.io.File;
import java.io.Serializable;
import java.util.logging.Logger;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;

/**
 * Can be queried by ?subject=""&predicate=""&object=""
 * 
 * @author nina
 * 
 * @param <T>
 */
public class TDBOntologyResource<T extends Serializable> extends
		AbstractOntologyResource {

	public TDBOntologyResource() {
		super();
	}

	protected Dataset createOntologyModel(boolean init)
			throws ResourceException {
		try {
			String directory = OntologyServiceConfig.getInstance()
					.getTDBDirectory();
			File dir = new File(directory);
			if (!dir.exists()) {
				if (dir.mkdirs())
					try {
						new File(directory + "/fixed.opt").createNewFile();
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).warning(x.toString());
					}
			}
			if (dir.exists()) {
				Dataset dataset = TDBFactory.createDataset(directory);
				Model ontology = dataset.getDefaultModel();
				if (init && (ontology.size() == 0)) {
					readOntologies(ontology);
					try {
						dataset.commit();
					} catch (Exception x) {
						Logger.getLogger(getClass().getName()).warning(
								x.getMessage());
					}
				}
				ontology.setNsPrefix("ot", "http://www.opentox.org/api/1.1#");
				ontology.setNsPrefix("ota",
						"http://www.opentox.org/algorithmTypes.owl#");
				ontology.setNsPrefix("otee",
						"http://www.opentox.org/echaEndpoints.owl#");
				ontology.setNsPrefix("owl", OWL.NS);
				ontology.setNsPrefix("dc", DC.NS);
				// ontology.setNsPrefix( "bx",
				// "http://purl.org/net/nknouf/ns/bibtex#" );
				// ontology.setNsPrefix( "bo",
				// "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#"
				// );
				/*
				 * if (ontology == null) ontology =
				 * TDBFactory.createModel(directory) ; if (ontology.size()==0)
				 * readOntologies();
				 */

				return dataset;
			} else
				throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
						"Failed to create folder " + dir.getName());
		} catch (ResourceException x) {
			throw x;
		} catch (Exception x) {
			throw new ResourceException(x);
		}
	}

	/*
	 * @Override protected Representation delete(Variant variant) throws
	 * ResourceException { Model ontology = createOntologyModel(false); try { if
	 * (ontology!=null) deleteModel(ontology); } catch (Exception x) {
	 * 
	 * } finally { ontology.close();
	 * 
	 * } return get(variant); }
	 */

}
