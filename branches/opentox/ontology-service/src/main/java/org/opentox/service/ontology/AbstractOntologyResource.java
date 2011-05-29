package org.opentox.service.ontology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Request;
import org.restlet.data.Cookie;
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
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.DC;

public abstract class AbstractOntologyResource extends ServerResource implements IAuthToken {
	protected static String jsGoogleAnalytics = null;
	public static final String resource="/ontology";
	public static final String resourceKey="key";
	protected boolean resultsOnly = false;
	protected String title = null;
	protected static String version = null;
	
	abstract protected Model createOntologyModel(boolean init) throws ResourceException ;
	
	enum Keys {
		Algorithm {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select ?Algorithm ?Property ?Value\n"+
				"	where {\n"+
				"	   ?Algorithm rdf:type ot:Algorithm.\n"+
				"	   OPTIONAL {?Algorithm ?Property ?Value}.\n"+
				"		}\n"+
				"	order by ?Algorithm ?Property ?Value\n"+
				"limit 20");
			}
		},
		Model{
			@Override
			public String getSPARQL() {
				return
					getPrefix()+
				"select distinct ?model ?title ?creator ?trainingDataset ?algorithm\n"+
				"		where {\n"+
				"			?model rdf:type ot:Model;\n"+
				"		    OPTIONAL {?model dc:title ?title}.\n"+
				"			OPTIONAL {?model dc:creator ?creator}.\n"+
				"			OPTIONAL {?model ot:trainingDataset ?trainingDataset}.\n"+
				"			OPTIONAL {?model ot:algorithm ?algorithm }.\n"+
				"		}";

			}
		},
		Descriptors {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:Algorithm;" +
				"  rdf:type ota:DescriptorCalculation;" +
				"  bo:instanceOf ?desc ." +
				"  ?desc rdf:type bo:MolecularDescriptor." +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
		},	
		Learning {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:Algorithm;" +
				"  rdf:type ota:Learning." +
				"  OPTIONAL {?algo bo:instanceOf ?desc .}" +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
			@Override
			public String toString() {
				return name();
			}
		},		
		Classification {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:Algorithm;" +
				"  rdf:type ota:Classification." +
				"  OPTIONAL {?algo bo:instanceOf ?desc .}" +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
			@Override
			public String toString() {
				return name();
			}
		},		
		Regression {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:Algorithm;" +
				"  rdf:type ota:Regression." +
				"  OPTIONAL {?algo bo:instanceOf ?desc .}" +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
			@Override
			public String toString() {
				return "Regression";
			}
		},			
		Rules {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:Algorithm;" +
				"  rdf:type ota:Rules." +
				"  OPTIONAL {?algo bo:instanceOf ?desc .}" +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
			@Override
			public String toString() {
				return "Expert rules";
			}
		},			
		AppDomain {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"SELECT ?algo ?desc WHERE {" +
				"  ?algo rdf:type ot:AppDomain;" +
				"  rdf:type ota:Rules." +
				"  OPTIONAL {?algo bo:instanceOf ?desc .}" +
				"}\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Algorithm;
			}
			@Override
			public String toString() {
				return "Applicability domain";
			}
		},			
		Feature {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select ?Feature ?Property ?Value\n"+
				"	where {\n"+
				"	   ?Feature rdf:type ot:Feature.\n"+
				"	   OPTIONAL {?Feature ?Property ?Value}.\n"+
				"		}\n"+
				"	order by ?Feature ?Property ?Value\n"+
				"limit 20");
			}
		},
		NumericFeature {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select ?Feature ?Property ?Value\n"+
				"	where {\n"+
				"	   ?Feature rdf:type ot:NumericFeature.\n"+
				"	   OPTIONAL {?Feature ?Property ?Value}.\n"+
				"		}\n"+
				"	order by ?Feature ?Property ?Value\n"+
				"limit 20");
			}
			@Override
			public Keys parent() {
				return Feature;
			}
		},
		NominalFeature {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select ?Feature ?Property ?Value\n"+
				"	where {\n"+
				"	   ?Feature rdf:type ot:NominalFeature.\n"+
				"	   OPTIONAL {?Feature ?Property ?Value}.\n"+
				"		}\n"+
				"	order by ?Feature ?Property ?Value\n"+
				"limit 20");
			}			
			@Override
			public Keys parent() {
				return Feature;
			}
		},
		/*
		FeatureValue {
		
			@Override
			public Keys parent() {
				return DataEntry;
			}
		},
		DataEntry {
			@Override
			public Keys parent() {
				return Dataset;
			}
		},
		*/
		Dataset {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select ?Dataset ?title ?source ?license ?rightsHolder ?seeAlso ?creator\n"+
				"	where {\n"+
				"	   ?Dataset rdf:type ot:Dataset.\n"+
				"	   OPTIONAL {?Dataset dc:title ?title}.\n"+
				"	   OPTIONAL {?Dataset dc:source ?source}.\n"+
				"	   OPTIONAL {?Dataset dcterms:license ?license}.\n"+
				"	   OPTIONAL {?Dataset dcterms:rightsHolder ?rightsHolder}.\n"+
				"	   OPTIONAL {?Dataset rdfs:seeAlso ?seeAlso}.\n"+
				"	   OPTIONAL {?Dataset dc:creator ?creator}.\n"+
				"		}\n"+
				"	order by ?Dataset\n"+
				"limit 20");
			}
		},
		Validation,
		Endpoints {
			@Override
			public String getSPARQL() {
				return super.getSPARQL("otee","rdfs:subClassOf");
			}
			@Override
			public String toString() {
				return "Endpoints";
			}
		};		
		public String getPrefix() {
			return 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
			String.format("PREFIX dc:<%s>\n",DC.NS)+
			"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+ 
			"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
			"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
			"PREFIX ota:<http://www.opentox.org/algorithmTypes.owl#>\n"+
			"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
			"PREFIX toxcast:<http://www.opentox.org/toxcast.owl#>\n";
		}
		public String getSPARQL() {
			return getSPARQL("ot","rdf:type");
		}	
		public String getSPARQL(String nameSpace,String predicate) {

			return
					String.format(
					"%s\n"+
					"	select ?%s ?title\n"+ 
					"	where {\n"+
					"	?%s %s %s:%s.\n"+
					"   OPTIONAL {?%s dc:title ?title}.\n"+
					"	}\n",
					getPrefix(),
					name(),
					name(),
					predicate,
					nameSpace,
					name(),
					name())
					;
		}
		public Keys parent() {
			return null;
		}
		@Override
		public String toString() {
			return String.format("%s", name());
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
		readVersion();
		try {
			resultsOnly = getRequest().getResourceRef().getQueryAsForm().getFirstValue("results").equals("yes");
		} catch (Exception x) {
			resultsOnly = false;
		}
		try {
			title = getRequest().getResourceRef().getQueryAsForm().getFirstValue("title");
		} catch (Exception x) {
			title = null;
		}		
		try {ClientResourceWrapper.setTokenFactory(this);} catch (Exception x){}
	}
	
	@Override
	protected void doRelease() throws ResourceException {
		try {ClientResourceWrapper.setTokenFactory(null);} catch (Exception x){}
		super.doRelease();
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
				"PREFIX ota:<http://www.opentox.org/algorithmTypes.owl#>\n"+
				"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
				"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
				"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
				"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
				"select ?descriptor ?label ?definition ?requires ?category ?contributor\n"+
				"		where {\n"+
				"	        ?descriptor rdf:type bo:MolecularDescriptor.\n"+
				"   		OPTIONAL {?descriptor rdfs:label ?label}.\n"+
				"                OPTIONAL {?descriptor bo:definition ?definition}.\n"+
				"                OPTIONAL {?descriptor dc:contributor ?contributor}.\n"+
				"                OPTIONAL {?descriptor bo:isClassifiedAs ?category}.\n"+
				"                OPTIONAL {?descriptor bo:requires?requires}.\n"+
				"		}\n";

			} else try {
				query =  Keys.valueOf(key.toString()).getSPARQL();
			} catch (Exception x) {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,key.toString());
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
				"descriptors-ambit.owl",
				"toxcast.owl",
		};
		for (String owl:owls)
			try {
				readOWL(getClass().getClassLoader().getResourceAsStream(
						String.format("org/opentox/owl/%s",owl)),
				ontology);
			} catch (Exception x) {
				
			}
		
	}
	protected Model getOntology(Model model, Reference reference) throws ResourceException {
		try {
			ClientResourceWrapper client = new ClientResourceWrapper(reference);
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
					long elapsed = System.currentTimeMillis();
					QueryExecution qe = null;
					ResultSet results = null;
					try {
						ontology.enterCriticalSection(Lock.READ) ;
					
					try {
						Query query = QueryFactory.create(queryString,null,Syntax.syntaxARQ);

						// Execute the query and obtain results
						qe = QueryExecutionFactory.create(query,ontology );
						results = qe.execSelect();
						elapsed = System.currentTimeMillis()-elapsed;
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
								"<meta name=\"robots\" content=\"index,nofollow\"><META NAME=\"GOOGLEBOT\" CONTENT=\"index,NOFOLLOW\">",
							    getRequest().getRootRef()));
							w.write(String.format("<script type=\"text/javascript\" src=\"%s/jquery/jquery-1.4.2.min.js\"></script>\n",getRequest().getRootRef()));
							w.write(String.format("<script type=\"text/javascript\" src=\"%s/jquery/jquery.tablesorter.min.js\"></script>\n",getRequest().getRootRef()));

							w.write("</head><body>");
							w.write(String.format("<link rel=\"stylesheet\" href=\"%s/style/tablesorter.css\" type=\"text/css\" media=\"screen\" title=\"Flora (Default)\">",getRequest().getRootRef()));
							if (!resultsOnly) 	writehtmlheader(w,ontology,queryString,elapsed);
							else w.write(String.format("<h4>%s</h4>",title==null?"":title));
								w.write("<table class='tablesorter' id='results'>");		
							
								w.write("<thead><tr>");
								List<String> vars = results.getResultVars();
								for (int i=0; i < vars.size();i++) {
									w.write("<th>");
									w.write(vars.get(i));
									w.write("</th>");
								}				
								w.write("</tr></thead>");
								w.write("<tbody>");
								while (results.hasNext()) {
									QuerySolution s = results.next();
									w.write("<tr>");
									for (int i=0; i < vars.size();i++) {
										RDFNode node = s.get(vars.get(i));
										w.write("<td>");
										w.write(node==null?"":PrintUtil.print(node));
										w.write("</td>");
									}
									w.write("</tr>");
								}
								w.flush();
								w.write("</tbody></table>");
								
								if (!resultsOnly) {
									w.write("</fieldset></FORM>");
									
									w.write(String.format("Version:&nbsp;<a href='%s/meta/MANIFEST.MF' target=_blank alt='%s' title='Web application version'>%s</a><br>",
											getRequest().getRootRef(),
											version==null?"":version,
											version));
								}
								w.write(jsGoogleAnalytics()==null?"":jsGoogleAnalytics());
								
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
	public static String jsGoogleAnalytics() {
		if (jsGoogleAnalytics==null) try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					TDBOntologyResource.class.getClassLoader().getResourceAsStream("org/opentox/config/googleanalytics.js"))
			);
			StringBuilder b = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
          	b.append(line);
          	b.append('\n');
          }
          jsGoogleAnalytics = b.toString();
			reader.close();
			
		} catch (Exception x) { jsGoogleAnalytics = null;}
		return jsGoogleAnalytics;
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
	

	protected String readVersion() {
		if (version!=null)return version;
		final String build = "Implementation-Build:";
		Representation p=null;
		try {
			ClientResource r = new ClientResource(String.format("%s/meta/MANIFEST.MF",getRequest().getRootRef()));
			p = r.get();
			String text = p.getText();
			System.out.println(text);
			//String text = build + ":0.0.1-SNAPSHOT-r1793-1266340980278";
			int i = text.indexOf(build);
			if (i>=0) {
				version = text.substring(i+build.length());
				i = version.lastIndexOf('-');
				if (i > 0) 
					version = String.format("%s-%s", 
							version.substring(1,i),
							new Date(Long.parseLong(version.substring(i+1).trim())));
			}
		} catch (Exception x) {
			version = "Unknown";
		} finally {
			//try { p.release();} catch (Exception x) {}
		}
		return version;
	}
	protected void removeURI(Model ontology, String uri) throws Exception {
		StmtIterator iter = ontology.listStatements(new SimpleSelector(ontology.createResource(uri),null,(RDFNode)null));
		ontology.remove(iter);
	}
	public static String jsTableSorter(String tableid,String pagerid) {
		return String.format("<script type=\"text/javascript\">$(document).ready(function() {  $(\"#%s\").tablesorter({widgets: ['zebra'] }).tablesorterPager({container: $(\"#%s\")}); } );</script>",tableid,pagerid);
	}
	public void writehtmlheader(Writer w,Model ontology,String queryString,long elapsed) throws Exception {
		
		Keys qkey = null;
		try { qkey = Keys.valueOf(getRequest().getAttributes().get(resourceKey).toString()); } catch (Exception x) {}
		StringBuilder b = new StringBuilder();
		
		for (Keys key : Keys.values()) {
			Keys parent = key.parent();
			
			String sparql = String.format("<a href='%s/query/%s' title='%s'>%s</a>&nbsp;",
					getRequest().getRootRef(),
					key.name(),
					key.getSPARQL(),
					key.toString()
						
					);
			
			if (parent==null) //top level
				w.write(sparql);	
			else {
				if (parent.equals(qkey)) { b.append(sparql);	b.append("|");}
			}
		}
		if (qkey!=null)
		w.write(String.format("<br>%s&nbsp;%s&nbsp;",qkey.toString(),"\u00bb"));
		w.write(b.toString());
		w.write(
				"<FORM action='' method='post'>"+
				"<FIELDSET><LEGEND>Import RDF data into Ontology service</LEGEND>"+
				"<label for=\"uri\">URL</label>"+
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
		w.write(jsTableSorter("results","pager"));
		w.write(String.format(
				"<FIELDSET><LEGEND>Results [found in %d ms]</LEGEND>",elapsed));
	}
	
	@Override
	protected Representation delete(Variant variant) throws ResourceException {
		String ref = "";
		Form form = getRequest().getResourceRef().getQueryAsForm();
		synchronized (this) {
			Model ontology= null;
			try {
				ResourceException xx = null;
				ontology = createOntologyModel(true);
				String[] uris = form.getValuesArray("uri");
				try {
					for (String uri:uris) {
						if (uri != null) {
							removeURI(ontology,uri);
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
		return get(variant);
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
	
	
	protected String getTokenFromCookies(Request request) {
		for (Cookie cookie : request.getCookies()) {
			if ("subjectid".equals(cookie.getName()))
				return cookie.getValue();
		}
		return null;
	}
	@Override
	public String getToken() {
		String token = getHeaderValue("subjectid");
		
		if (token == null) token = getTokenFromCookies(getRequest());
		return token== null?null:token;
		 
	}
	
	private String getHeaderValue(String tag) {
		try {
			Form headers = (Form) getRequest().getAttributes().get("org.restlet.http.headers");  
			if (headers==null) return null;
			return headers.getFirstValue(tag);
		} catch (Exception x) {
			return null;
		}
	}
	
		
}
