package org.opentox.service.ontology;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.logging.Logger;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.vocabulary.DC;


/**
 * Can be queried by ?subject=""&predicate=""&object=""
 * @author nina
 *
 * @param <T>
 */
public class OntologyResource<T extends Serializable> extends ServerResource {
	public static final String resource="/ontology";
	public static final String resourceKey="key";
	protected String directory = String.format("%s/tdb",System.getProperty("java.io.tmpdir")); 
	protected Model ontology;
	public OntologyResource() {
		super();
	}
	
	@Override
	protected void doRelease() throws ResourceException {
		super.doRelease();
		if (ontology!= null) {
			ontology.close();
		}
	}
	
	protected Model getOntology(Model model, Reference reference) throws ResourceException {
		try {
			ClientResource client = new ClientResource(reference);
			MediaType[] mt = {
					MediaType.APPLICATION_RDF_XML,
					MediaType.TEXT_RDF_N3,
					MediaType.TEXT_RDF_NTRIPLES,
					MediaType.APPLICATION_RDF_TURTLE,
					
			};
			for (MediaType m : mt) {
				Representation r = null;
				try {
					r = client.get(m);
					if (client.getStatus().equals(Status.SUCCESS_OK)) {
						readOWL(r.getStream(),model);
						return model;
					} else throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
							String.format("%s %s",client.getStatus().toString(),reference.toString()));
				} catch (ResourceException x) {
					throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
							String.format("%s %s %s", Status.SERVER_ERROR_BAD_GATEWAY.toString(),reference,x.getMessage()),x);
				} catch (Exception x) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,reference.toString(),x);
				} finally {
					try {r.release();} catch (Exception x) {};
				}
			}
		} catch (ResourceException x) {
			throw x;
		} catch (Exception x) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,x.getMessage(),x);
		}
		return model;
	}
	
		
	protected Representation sparql(final String queryString, Variant variant) throws ResourceException {
		 
		  ontology = createOntologyModel();
		  
			return new OutputRepresentation(variant.getMediaType()) {
				@Override
				public void write(OutputStream out) throws IOException {
	
					QueryExecution qe = null;
					try {
						Query query = QueryFactory.create(queryString);
				
						// Execute the query and obtain results
						qe = QueryExecutionFactory.create(query,ontology );
						ResultSet results = qe.execSelect();
	
						if (getMediaType().equals(MediaType.APPLICATION_RDF_XML))
							ResultSetFormatter.outputAsRDF(out,"RDF/XML", results);
						else if (getMediaType().equals(MediaType.APPLICATION_RDF_TURTLE))
							ResultSetFormatter.outputAsRDF(out,"TURTLE", results);
						else if (getMediaType().equals(MediaType.TEXT_RDF_N3))
							ResultSetFormatter.outputAsRDF(out,"N3", results);
						else if (getMediaType().equals(MediaType.TEXT_RDF_NTRIPLES))
							ResultSetFormatter.outputAsRDF(out,"N-TRIPLE", results);
						else if (getMediaType().equals(MediaType.TEXT_CSV))
							ResultSetFormatter.outputAsCSV(out, results);
						else if (getMediaType().equals(MediaType.APPLICATION_JSON))
							ResultSetFormatter.outputAsJSON(out, results);		
						else if (getMediaType().equals(MediaType.TEXT_XML))
							ResultSetFormatter.outputAsXML(out, results);
						else if (getMediaType().equals(MediaType.TEXT_PLAIN))
							ResultSetFormatter.out(out, results, query);
						else if (getMediaType().equals(MediaType.TEXT_HTML)) {
							OutputStreamWriter w = new OutputStreamWriter(out);
							w.write(String.format("<a href='%s/Feature'>Features</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/Algorithm'>Algorithms</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/Model'>Models</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/Endpoints'>Endpoints</a>&nbsp;",getRequest().getRootRef()));
							w.write(
								"<h3>Query Ontology service</h3>"+
								"<html><head><title>Search Opentox RDF</title></head><body>"+
								"<FORM action='' method='post'>"+
								"<FIELDSET><LEGEND>SPARQL</LEGEND>"+
							    "<TEXTAREA name=\"query\" rows=\"10\" cols=\"120\" tabindex=\"1\">");
							w.flush();
							w.write(queryString);
							w.write(
							    "</TEXTAREA>"+
							    "</FIELDSET><INPUT name=\"run\" type=\"submit\" tabindex=\"2\">"
									);
							w.write(
									"<FIELDSET><LEGEND>Results</LEGEND>"+
								    "<TEXTAREA name=\"results\" rows=\"10\" cols=\"120\" tabindex=\"3\">");
							w.flush();
							ResultSetFormatter.out(out, results, query);
							w.write("</TEXTAREA>"+
								    "</FIELDSET></FORM>"
										);
							
							//post
							
							w.write(
									"<h3>Import RDF data into Ontology service</h3>"+
									"<FORM action='' method='post'>"+
									"<FIELDSET><LEGEND>Models URI</LEGEND>"+
								    "<input name=\"model_uri\" size=\"80\" tabindex=\"4\">"+
								    "</FIELDSET>"+
								    "<FIELDSET><LEGEND>Algorithms URI</LEGEND>"+
								    "<input name=\"algorithm_uri\"  size=\"80\" tabindex=\"5\">"+
								    "</FIELDSET>"+
								    "<FIELDSET><LEGEND>Features URI</LEGEND>"+
								    "<input name=\"feature_uris\" size=\"80\" tabindex=\"6\">"+
								    "</FIELDSET>"+
								    "<INPUT name=\"run\" type=\"submit\" value=\"SUBMIT\" tabindex=\"7\">"+
									"</FORM></body>"
											);							
							w.flush();
						} else {
							
							ResultSetFormatter.outputAsRDF(out,"RDF/XML-ABBREV", results);
						}
						out.flush();
					
					} catch (Exception x) {
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,x);
					} finally {
						try {qe.close();} catch (Exception x) {}
					}
				}
		
			};
	}
	protected void readOWL(InputStream in , Model model) throws Exception {
		try {
			model.read(in,null);
		} catch (Exception x) {
			Logger.getLogger(getClass().getName()).severe(x.toString());
		} finally {
			try { if (in != null) in.close();} catch (Exception x) {}
		}
	}
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		customizeVariants(new MediaType[] {
				
				MediaType.APPLICATION_RDF_XML,
				MediaType.APPLICATION_RDF_TURTLE,
				MediaType.TEXT_RDF_N3,
				MediaType.TEXT_RDF_NTRIPLES	,
				MediaType.TEXT_XML,
				MediaType.APPLICATION_JSON,
				MediaType.TEXT_CSV,
				MediaType.TEXT_PLAIN,
				MediaType.TEXT_URI_LIST,
				MediaType.TEXT_HTML
				
				});		
		

	}
	protected void customizeVariants(MediaType[] mimeTypes) {
        for (MediaType m:mimeTypes) getVariants().add(new Variant(m));
	}
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		Form form = getRequest().getResourceRef().getQueryAsForm();
		String query = form.getFirstValue("query");
		if(query == null) {
			String ns = "ot";
			String predicate = "rdf:type";
			Object key = getRequest().getAttributes().get(resourceKey);
			if (key==null) { 
				query = 
				"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
				"PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"+
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
				"PREFIX dc:<http://purl.org/dc/elements/1.1/#>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
				"select ?Feature ?SameAS\n"+
				"		where {\n"+
				"	        ?Feature owl:sameAs ?SameAS\n"+
				"		}\n";
			} else if (key.toString().toLowerCase().equals("endpoints")) {
				ns = "otee";
				key = "Endpoints";
				predicate = "rdfs:subClassOf";
				
				query = 
					String.format(
					"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
					"PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"+
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
					String.format("PREFIX dc:<%s#>\n",DC.NS)+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
					"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
					"	select ?%s ?title\n"+ 
					"	where {\n"+
					"	?%s %s %s:%s\n"+
				//	"   ?x dc:title ?title.\n"+
					"	}\n",
					key.toString(),
					key.toString(),
					predicate,
					ns,
					key.toString())
					;
				
			}
			//throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		return sparql(query,variant);		
	}

	protected void readOntologies() {
		String[] owls = new String[] {
				"opentox.owl",
				"descriptor-algorithms.owl",
				"echa-endpoints.owl",
				"AlgorithmTypes.owl",
		};
		for (String owl:owls)
			try {
				readOWL(getClass().getClassLoader().getResourceAsStream(
						String.format("org/opentox/owl/%s",owl)),
				ontology);
			} catch (Exception x) {
				
			}
		try { ontology.commit(); } catch (Exception x) {}
		try { ontology.close(); ontology = null;} catch (Exception x) {}			
	}
	protected Model createOntologyModel() throws ResourceException {
		File dir  = new File(directory);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (ontology == null) ontology = TDBFactory.createModel(directory) ;
		if (ontology.size()==0) readOntologies();
		return ontology;
	}
	@Override
	protected Representation post(Representation entity, Variant variant)
			throws ResourceException {
		String ref = "";
		Form form = new Form(entity);
		try {
			ResourceException xx = null;
			ontology = createOntologyModel();
			String[] uris = new String[] {"model_uri","algorithm_uri","feature_uris"};
			try {
				for (String uri:uris) {
					String search = form.getFirstValue(uri);
					if (search != null) {
						getOntology(ontology,new Reference(search));
						ref = getRequest().getOriginalRef().getBaseRef()+"/Model";
					}
				}
			} catch(ResourceException x) {
				xx = x;
			} finally {}	
			if (xx!=null)	throw xx;
		} catch (ResourceException x) {
			throw x;
		} catch(Exception x) {
			throw new ResourceException(x);
		} finally {
			try { ontology.commit(); } catch (Exception x) {}
			try { ontology.close(); ontology = null;} catch (Exception x) {}
		}
	
		try {
			String query = form.getFirstValue("query");
			if(query != null) {
				return sparql(query,variant);
			} else {
				getResponse().setLocationRef(ref);
				return get(variant);
			}
		} catch (Exception x) {
			throw new ResourceException(x);
		} finally {
			
		}
	}
}
