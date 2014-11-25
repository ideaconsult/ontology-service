package org.opentox.service.ontology;

import java.io.Serializable;
import java.util.logging.Level;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;


public class ConfigurationOntologyResource<T extends Serializable> extends TDBOntologyResource<T> {

	@Override
	protected boolean addDefaultOntologies() {
		return false;
	}
	@Override
	protected Representation post(Representation entity, Variant variant)
			throws ResourceException {
		Dataset dataset = null;
		Model ontology = null;
		try {
			dataset = createOntologyModel(true);
			ontology = dataset.getDefaultModel();
		} catch (Exception x) {
			getLogger().log(Level.WARNING,x.getMessage(),x);
		} finally {
			try {ontology.close(); } catch (Exception x) {}
			try {dataset.close(); } catch (Exception x) {}
		}
		return new StringRepresentation(String.format("%s",getRequest().getRootRef()),MediaType.TEXT_URI_LIST);
	}
	@Override
	protected Representation delete(Variant variant) throws ResourceException {
		Dataset dataset = null;
		try {
			dataset = createOntologyModel(false);
			if (dataset!=null) deleteModel(dataset);
		} catch (Exception x) {
			getLogger().log(Level.WARNING,x.getMessage(),x);
		} finally {
			try { dataset.close(); } catch (Exception x) {}

		}
		return new StringRepresentation(String.format("%s",getRequest().getRootRef()),MediaType.TEXT_URI_LIST);
	}
}
