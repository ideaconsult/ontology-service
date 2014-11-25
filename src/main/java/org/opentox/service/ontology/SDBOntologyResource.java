package org.opentox.service.ontology;

import org.restlet.resource.ResourceException;

import com.hp.hpl.jena.query.Dataset;

/**
 * http://www.devx.com/semantic/Article/40247/1954
 * http://harshitkumar.wordpress.com/2010/06/29/joseki-with-sdb-and-mysql/
 * http://ankitjain.info/ankit/2009/06/21/configure-jena-sdb-mysql-ubuntu/
 * http://www.2wav.com/node/56
 * @author nina
 <pre>
 CREATE DATABASE rdf2wav CHARACTER SET UTF8;
 GRANT ALL ON sdb.* TO 'sdbuser' IDENTIFIED BY 'secret';
 </pre>
 */
public class SDBOntologyResource extends AbstractOntologyResource {

	@Override
	protected Dataset createOntologyModel(boolean init) throws ResourceException {
		/*
		File dir  = new File(directory);
		if (!dir.exists()) {
			dir.mkdir();
			try {
				new File(directory+"/fixed.opt").createNewFile();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		Model ontology = SDB.
		if (init && (ontology.size()==0)) readOntologies(ontology);
		ontology.setNsPrefix( "ot", "http://www.opentox.org/api/1.1#");
		ontology.setNsPrefix( "ota", "http://www.opentox.org/algorithmTypes.owl#" );
		ontology.setNsPrefix( "otee", "http://www.opentox.org/echaEndpoints.owl#" );
		ontology.setNsPrefix( "owl", OWL.NS );
		ontology.setNsPrefix( "dc", DC.NS );
		ontology.setNsPrefix( "bx", "http://purl.org/net/nknouf/ns/bibtex#" );
		ontology.setNsPrefix( "bo", "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#" );	
		*/	
		/*
		if (ontology == null) ontology = TDBFactory.createModel(directory) ;
		if (ontology.size()==0) readOntologies();
		*/
		//return ontology;
		return null;
	}
}
