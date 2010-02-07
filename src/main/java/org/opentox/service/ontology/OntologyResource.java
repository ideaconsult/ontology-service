package org.opentox.service.ontology;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.List;
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
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;


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
	
	public OntologyResource() {
		super();
	}
	

	
	@Override
	protected void doRelease() throws ResourceException {
		super.doRelease();
		/*
		if (ontology!= null) {
			ontology.close();

		}
		*/
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
		 
		  final Model ontology = createOntologyModel(true);
		  
			return new OutputRepresentation(variant.getMediaType()) {
				@Override
				public void write(OutputStream out) throws IOException {
	
					QueryExecution qe = null;
					ResultSet results = null;
					try {
						ontology.enterCriticalSection(Lock.READ) ;
					
					try {
						Query query = QueryFactory.create(queryString);
						

						// Execute the query and obtain results
						qe = QueryExecutionFactory.create(query,ontology );
						results = qe.execSelect();
	//application/sparql-results+xml

						if (getMediaType().equals(MediaType.APPLICATION_RDF_XML))
							ResultSetFormatter.outputAsRDF(out,"RDF/XML", results);
						else if (getMediaType().equals(MediaType.APPLICATION_SPARQL_RESULTS_XML))
							ResultSetFormatter.outputAsXML(out, results);
						else if (getMediaType().equals(MediaType.APPLICATION_RDF_TURTLE))
							ResultSetFormatter.outputAsRDF(out,"TURTLE", results);
						else if (getMediaType().equals(MediaType.TEXT_RDF_N3))
							ResultSetFormatter.outputAsRDF(out,"N3", results);
						else if (getMediaType().equals(MediaType.TEXT_RDF_NTRIPLES))
							ResultSetFormatter.outputAsRDF(out,"N-TRIPLE", results);
						else if (getMediaType().equals(MediaType.TEXT_CSV))
							ResultSetFormatter.outputAsCSV(out, results);
						else if (getMediaType().equals(MediaType.APPLICATION_SPARQL_RESULTS_JSON))
							ResultSetFormatter.outputAsJSON(out, results);		
						else if (getMediaType().equals(MediaType.TEXT_PLAIN))
							ResultSetFormatter.out(out, results, query);
						else { //html
							OutputStreamWriter w = new OutputStreamWriter(out);
							w.write(
									String.format(
								"<html><head><title>Search Opentox RDF</title>"+
								"<link href=\"%s/style/ambit.css\" rel=\"stylesheet\" type=\"text/css\">"+		
								"</head><body>",
							    getRequest().getRootRef()));							
							w.write(String.format("<a href='%s/query/Feature'>Features</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/query/Algorithm'>Algorithms</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/query/Model'>Models</a>&nbsp;",getRequest().getRootRef()));
							w.write(String.format("<a href='%s/query/Endpoints'>Endpoints</a>&nbsp;",getRequest().getRootRef()));
							w.write(
									"<h3>Import RDF data into Ontology service</h3>"+
									"<FORM action='' method='post'>"+
									"<FIELDSET><LEGEND>URI</LEGEND>"+
								    "<input name=\"uri\" size=\"120\" tabindex=\"4\">"+
								    "</FIELDSET>"+
								    "<INPUT name=\"run\" type=\"submit\" value=\"SUBMIT\" tabindex=\"7\">"+
									"</FORM>"
											);									
							w.write(
									String.format(
								"<h3>Ontology service&nbsp;%s triples</h3>"+											
								"<FORM action='' method='post'>"+
								"<FIELDSET><LEGEND>SPARQL</LEGEND>"+
							    "<TEXTAREA name=\"query\" rows=\"10\" cols=\"120\" tabindex=\"1\">",
							    ontology==null?0:ontology.size(),
							    getRequest().getRootRef()));
							w.flush();
							w.write(queryString);
							w.write(
							    "</TEXTAREA>"+
							    "</FIELDSET><INPUT name=\"run\" type=\"submit\" tabindex=\"2\">"
									);
							w.write(String.format(
									"<FIELDSET><LEGEND>Results</LEGEND><table bgcolor='#DDDDDD'>"));
							w.write("<tr bgcolor='#FFFFFF'>");
							List<String> vars = results.getResultVars();
							for (int i=0; i < vars.size();i++) {
								w.write("<th>");
								w.write(vars.get(i));
								w.write("</th>");
							}				
							w.write("</tr>");
							while (results.hasNext()) {
								QuerySolution s = results.next();
								w.write("<tr  bgcolor='#FFFFFF'>");
								for (int i=0; i < vars.size();i++) {
									RDFNode node = s.get(vars.get(i));
									w.write("<td>");
									w.write(PrintUtil.print(node));
									w.write("</td>");
								}
								w.write("</tr>");
							}
							w.flush();
							w.write("</table>"+
								    "</fieldset></FORM>"
										);
							
							w.write("</body>");
							
					
							w.flush();
						}
						out.flush();
					
					} catch (Exception x) {
						throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,x);
					} finally {
						try {qe.close();} catch (Exception x) {}
						try {out.close();} catch (Exception x) {}

					}
					} finally {
						ontology.leaveCriticalSection() ; 
					}
				}
		
			};
	}
	protected void readOWL(InputStream in , Model model) throws Exception {
		try {
			model.enterCriticalSection(Lock.WRITE) ;
			try {
				model.read(in,null);
				try { model.commit(); } catch (Exception x) {}
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).severe(x.toString());
			} finally {
				try { if (in != null) in.close();} catch (Exception x) {}
			}
		} catch (Exception x) {
			throw x;
		} finally {
			model.leaveCriticalSection() ; 
		}
	}
	
	protected void deleteModel(Model model) throws Exception {
		try {
			model.enterCriticalSection(Lock.WRITE) ;
			try {
				model.removeAll();
				try { model.commit(); } catch (Exception x) {}
			} catch (Exception x) {
				Logger.getLogger(getClass().getName()).severe(x.toString());
			} finally {

			}
		} catch (Exception x) {
			throw x;
		} finally {
			model.leaveCriticalSection() ; 
		}
	}	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		customizeVariants(new MediaType[] {
				MediaType.TEXT_HTML,
				MediaType.APPLICATION_SPARQL_RESULTS_XML,
				MediaType.APPLICATION_RDF_XML,
				MediaType.APPLICATION_RDF_TURTLE,
				MediaType.TEXT_RDF_N3,
				MediaType.TEXT_RDF_NTRIPLES	,
				MediaType.APPLICATION_SPARQL_RESULTS_JSON,
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
			} else {
				if (key.toString().toLowerCase().equals("endpoints")) {
					ns = "otee";
					key = "Endpoints";
					predicate = "rdfs:subClassOf";
				}
				
				query = 
					String.format(
					"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
					"PREFIX ota:<http://www.opentox.org/algorithms.owl#>\n"+
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
					String.format("PREFIX dc:<%s>\n",DC.NS)+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
					"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
					"	select ?%s ?title ?id\n"+ 
					"	where {\n"+
					"	?%s %s %s:%s.\n"+
					"   OPTIONAL {?%s dc:title ?title}.\n"+
					"   OPTIONAL {?%s dc:identifier ?id}.\n"+
					"	}\n",
					key.toString(),
					key.toString(),
					predicate,
					ns,
					key.toString(),
					key.toString(),
					key.toString())
					;
				
			
			//throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
		}
		return sparql(query,variant);		
	}

	protected void readOntologies(Model ontology) {
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
		
	}
	protected Model createOntologyModel(boolean init) throws ResourceException {
		File dir  = new File(directory);
		if (!dir.exists()) {
			dir.mkdir();
		}
		Model ontology = TDBFactory.createModel(directory) ;
		if (init && (ontology.size()==0)) readOntologies(ontology);
		ontology.setNsPrefix( "ot", "http://www.opentox.org/api/1.1#");
		ontology.setNsPrefix( "ota", "http://www.opentox.org/algorithmTypes.owl#" );
		ontology.setNsPrefix( "otee", "http://www.opentox.org/echaEndpoints.owl#" );
		ontology.setNsPrefix( "owl", OWL.NS );
		ontology.setNsPrefix( "dc", DC.NS );
		ontology.setNsPrefix( "bx", "http://purl.org/net/nknouf/ns/bibtex#" );		
		/*
		if (ontology == null) ontology = TDBFactory.createModel(directory) ;
		if (ontology.size()==0) readOntologies();
		*/
		return ontology;
	}
	@Override
	protected Representation post(Representation entity, Variant variant)
			throws ResourceException {
		String ref = "";
		Form form = new Form(entity);
		synchronized (this) {
			Model ontology= null;
			try {
				ResourceException xx = null;
				ontology = createOntologyModel(true);
				String[] uris = form.getValuesArray("uri");
				try {
					for (String search:uris) {
						if (search != null) {
							getOntology(ontology,new Reference(search));
							ref = getRequest().getRootRef().toString();
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
				try { if (ontology!=null) ontology.commit(); } catch (Exception x) {}
				try { if (ontology!=null) ontology.close(); ontology = null;} catch (Exception x) {}
			}
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
}
