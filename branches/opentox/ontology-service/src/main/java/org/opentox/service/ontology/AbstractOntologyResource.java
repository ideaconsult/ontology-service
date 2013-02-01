package org.opentox.service.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.opentox.dsl.aa.IAuthToken;
import org.opentox.dsl.task.ClientResourceWrapper;
import org.opentox.service.ontology.tools.DownloadTool;
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
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFReader;
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
	
	private final static String[] js = new String[] {
		"<script type='text/javascript' src='%s/jquery/jquery-1.7.1.min.js'></script>\n",
		"<script type='text/javascript' src='%s/jquery/jquery-ui-1.8.18.custom.min.js'></script>\n",
		"<script type='text/javascript' charset='utf8' src='%s/jquery/jquery.dataTables-1.9.0.min.js'></script>\n",
		//"<script type='text/javascript' src='%s/scripts/jopentox.js'></script>\n",

	};
	private final static String[] css = new String[] {
		"<link href=\"%s/style/base.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<link href=\"%s/style/skeleton.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<link href=\"%s/style/layout.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<link href=\"%s/style/ambit2.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<!--[if lt IE 9]><script src='http://html5shim.googlecode.com/svn/trunk/html5.js'></script><![endif]-->",
		"<link href=\"%s/style/jquery-ui-1.8.18.custom.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<link href=\"%s/style/jquery.dataTables.css\" rel=\"stylesheet\" type=\"text/css\">\n",
		"<link href=\"%s/images/favicon.ico\" rel=\"shortcut icon\" type=\"image/ico\">\n"
	};
	public static String sparql_ToxcastAssayTarget = 

				"	select *\n"+
				"	where {\n"+
				"	?Feature rdf:type ot:Feature.\n"+
				"   {?Feature dc:title ?title}.\n"+
				"   {?Feature ot:hasSource ?OpentoxDataset}.\n"+					
				"   {?Feature owl:sameAs ?assay}.\n"+
				"   {?assay toxcast:gene ?geneid}.\n"+
				"   {?assay toxcast:hasProperty ?species}.\n"+
				"   {?species rdf:type toxcast:SPECIES}.\n"+
				"   {?assay toxcast:hasProperty ?target_source}.\n"+
				"   {?target_source rdf:type toxcast:ASSAY_TARGET_SOURCE}\n"+
				"   {?assay toxcast:hasProperty ?target_family}.\n"+
				"   {?target_family rdf:type toxcast:ASSAY_TARGET_FAMILY}.\n"+
				"   {?assay toxcast:hasProperty ?target}.\n"+
				"   {?target rdf:type toxcast:ASSAY_TARGET}.\n"+
				"   {?assay toxcast:hasProperty toxcast:%s}.\n"+
				"}\n"+
				"order by ?feature ?assay ?target\n";		
	
	enum Keys {
		Algorithm {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
				"select distinct ?OpenToxAlgorithm ?label ?doi ?Reference ?OntologyEntry\n"+
				"		where {\n"+
				"			   ?OpenToxAlgorithm rdf:type ot:Algorithm.\n"+
				"			   ?OpenToxAlgorithm dc:title ?label.\n"+
				"		   OPTIONAL {\n"+
				"	               ?OpenToxAlgorithm bo:instanceOf ?OntologyEntry.\n"+
				"	               OPTIONAL {\n"+
				"	                   ?OntologyEntry bo:cites ?Reference.\n"+
				"		                   OPTIONAL {?Reference bo:DOI ?doi.}\n"+
				"	               }\n"+
				"			}\n"+
				"		}\n"+
				"	order by ?OpenToxAlgorithm\n"
				);

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
				" {{?desc rdf:type bo:Algorithm} UNION {?desc rdf:type bo:MolecularDescriptor}}."+
				"}\n"
				);
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
				"}\n"
				);
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
				"}\n"
				);
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
				"}\n"
				);
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
				"}\n"
				);
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
				"}\n"
				);
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
			@Override
			public String getHint() {
				return "ECHA classification of toxicological endpoints http://www.opentox.org/echaEndpoints.owl";
			}
		},	
		BODO {
			@Override
			public String getSPARQL() {
				return
					"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
					"PREFIX ota:<http://www.opentox.org/algorithmTypes.owl#>\n"+
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
					"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
					"PREFIX bibrdf:<http://zeitkunst.org/bibtex/0.1/bibtex.owl#>\n"+
					"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
					"select ?descriptor ?label ?cites ?doi ?definition ?requires ?category ?contributor\n"+
					"		where {\n"+
					"	        {{?descriptor rdf:type bo:Algorithm} UNION {?descriptor rdf:type bo:MolecularDescriptor}}.\n"+
					"   		OPTIONAL {?descriptor rdfs:label ?label}.\n"+
					"                OPTIONAL {?descriptor bo:definition ?definition}.\n"+
					"                OPTIONAL {?descriptor dc:contributor ?contributor}.\n"+
					"                OPTIONAL {?descriptor bo:isClassifiedAs ?category}.\n"+
					"                OPTIONAL {?descriptor bo:requires?requires}.\n"+
					"                OPTIONAL {\n" +
					"                    ?descriptor bo:cites ?cites.\n" +
					"                    ?cites bibrdf:hasTitle ?title.\n" +					
					"					 OPTIONAL {?cites bo:DOI ?doi.}\n" +
					"                }.\n"+
					"		}\n";
			}
			@Override
			public String toString() {
				return "Ontology";
			}
			@Override
			public String getHint() {
				return "Blue Obelisk Ontology of cheminformatic algorithms (extended)";
			}
		},
		ToxCast {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
						"	select *\n"+
						"	where {\n"+
						"	?Feature rdf:type ot:Feature.\n"+
						"   {?Feature dc:title ?title}.\n"+
						"   {?Feature owl:sameAs ?assay}.\n"+
						"   {?Feature ot:hasSource ?OpentoxDataset}.\n"+	
						"   {?assay toxcast:gene ?geneid}.\n"+
						"   {?assay toxcast:hasProperty ?species}.\n"+
						"   {?species rdf:type toxcast:SPECIES}.\n"+
						"   {?assay toxcast:hasProperty ?target_source}.\n"+
						"   {?target_source rdf:type toxcast:ASSAY_TARGET_SOURCE}\n"+
						"   {?assay toxcast:hasProperty ?target_family}.\n"+
						"   {?target_family rdf:type toxcast:ASSAY_TARGET_FAMILY}.\n"+
						"   {?assay toxcast:hasProperty ?target}.\n"+
						"   {?target rdf:type toxcast:ASSAY_TARGET}.\n"+
						"}\n"+
						"order by ?feature ?assay ?target\n");
						
			}
			@Override
			public String toString() {
				return "ToxCast";
			}
			@Override
			public String getHint() {
				return "ToxCast metadata converted to ontology format";
			}

		},			
		ToxCast_gene {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
						"select ?Feature ?title ?OpentoxDataset ?assay ?geneid ?genename\n"+
						"		where {\n"+
						"		?Feature rdf:type ot:Feature.\n"+
						"      {?Feature ot:hasSource ?OpentoxDataset}.\n"+						
						"	   {?Feature dc:title ?title}.\n"+
						"	   {?Feature owl:sameAs ?assay}.\n"+
						"	   {?assay toxcast:gene ?geneid}.\n"+
						"	   {?assay toxcast:hasProperty ?genename}.\n"+
						"	   {?genename rdf:type toxcast:GENE_NAME}.\n"+
						"		}\n");
						
			}
			@Override
			public String toString() {
				return "ToxCast (gene)";
			}
			@Override
			public String getHint() {
				return "ToxCast assays/gene associations";
			}
			@Override
			public Keys parent() {
				return ToxCast;
			}
		},	
		
		ToxCast_ESR1 {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
						"select ?Feature ?title ?OpentoxDataset ?assay ?genename\n"+
						"		where {\n"+
						"	    ?Feature rdf:type ot:Feature.\n"+
						"       {?Feature ot:hasSource ?OpentoxDataset}.\n"+
						"	   {?Feature dc:title ?title}.\n"+
						"	   {?Feature owl:sameAs ?assay}.\n"+
						"	   {?assay toxcast:gene <http://bio2rdf.org/geneid:2099>}.\n"+
						"	   {?assay toxcast:hasProperty ?genename}.\n"+
						"	   {?genename rdf:type toxcast:GENE_NAME}.\n"+
						"		}\n"
						);
						
			}
			@Override
			public String toString() {
				return "ToxCast ESR Assays";
			}
			@Override
			public String getHint() {
				return "ToxCast assays, associated with ESR1 gene (example how to query for particular gene)";
			}
			@Override
			public Keys parent() {
				return ToxCast;
			}
		},	



		ToxCast_CYP450 {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Cytochrome_P450")	
					);
						
			}
			@Override
			public String getHint() {
				return "ToxCast assays, associated with CYP450 (example how to query for particular target family)";
			}
			@Override
			public Keys parent() {
				return ToxCast;
			}
			@Override
			public String toString() {
				return "Target family: CYP450";
			}
		},	

		ToxCast_GPCR {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"GPCR")	
					);
						
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
			@Override
			public String toString() {
				return "Target family: GPCR";
			}
			@Override
			public String getHint() {
				return "ToxCast assays, associated with GPCR (example how to query for particular target family)";
			}			
		},	

		ToxCast_Ion_Channel {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Ion_Channel")	
					);
						
			}
			@Override
			public String getHint() {
				return "ToxCast assays, associated with Ion Channel (example how to query for particular target family)";
			}	
			@Override
			public Keys parent() {
				return ToxCast;
			}
			@Override
			public String toString() {
				return "Target family: Ion Channel";
			}			
		},	
		ToxCast_Kinase {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Kinase")	
					);
						
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
			@Override
			public String toString() {
				return "Target family: Kinase";
			}			
		},		
		/*
		ToxCast_Membrane_Protein {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Membrane_Protein")	
					);
						
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
		},	
		ToxCast_Nuclear_Receptor {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Nuclear_Receptor")	
					);
						
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
		},	
		*/
		ToxCast_Hormone {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Hormone")	
					);
						
			}
			@Override
			public String toString() {
				return "Target family: Hormone";
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
		};
		/*
		Anti-Coagulation
		Apolipoprotein
		Cell_Adhesion_Molecule
		Coagulation_Factor
		Cytokine
		Cytokine_Receptor
		Hormone
		Lyase
		Oxidase
		Oxygenase
		Other
		Phosphatase
		Protease
		Reductase
		Sigma_Receptor
		Synthase
		Transferase
		Transporter
		*/
		/*
		ToxCast_CytokineReceptor {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Cytokine_Receptor")	
					);
						
			}
			
			@Override
			public Keys parent() {
				return ToxCast;
			}
		},			
		ToxCast_Esterase {
			@Override
			public String getSPARQL() {
				return String.format("%s%s",getPrefix(),
					String.format( sparql_ToxcastAssayTarget,"Esterase")	
					);
						
			}

			@Override
			public Keys parent() {
				return ToxCast;
			}
		};		
		*/	
		public String getPrefix() {
			return 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
			"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
			String.format("PREFIX dc:<%s>\n",DC.NS)+
			"PREFIX dcterms:<http://purl.org/dc/terms/>\n"+ 
			"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
			"PREFIX bo1:<http://ambit.sf.net/descriptors.owl#>\n"+
			"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
			"PREFIX ota:<http://www.opentox.org/algorithmTypes.owl#>\n"+
			"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
			"PREFIX bibrdf:<http://purl.org/net/nknouf/ns/bibtex#>\n"+
			"PREFIX toxcast:<http://www.opentox.org/toxcast#>\n";
			
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
		public String getHint() {
			return toString();
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
		String uri = form.getFirstValue("uri");
		if(query == null) {
			if (uri!=null) {
				query = String.format(
					"PREFIX ot:<http://www.opentox.org/api/1.1#>\n"+
					"PREFIX ota:<http://www.opentox.org/algorithmTypes.owl#>\n"+
					"PREFIX owl:<http://www.w3.org/2002/07/owl#>\n"+
					"PREFIX dc:<http://purl.org/dc/elements/1.1/>\n"+
					"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"+
					"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
					"PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>\n"+
					"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
					"select ?p ?o\n"+
					"		where {\n"+
					"	        <%s> ?p ?o.\n"+
					"		}",uri,uri);
			} else {
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
					"PREFIX bibrdf:<http://zeitkunst.org/bibtex/0.1/bibtex.owl#>\n"+
					"PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>\n"+
					"select ?descriptor ?label ?cites ?doi ?definition ?requires ?category ?contributor\n"+
					"		where {\n"+
					"	        {{?descriptor rdf:type bo:Algorithm} UNION {?descriptor rdf:type bo:MolecularDescriptor}}.\n"+
					"   		OPTIONAL {?descriptor rdfs:label ?label}.\n"+
					"                OPTIONAL {?descriptor bo:definition ?definition}.\n"+
					"                OPTIONAL {?descriptor dc:contributor ?contributor}.\n"+
					"                OPTIONAL {?descriptor bo:isClassifiedAs ?category}.\n"+
					"                OPTIONAL {?descriptor bo:requires?requires}.\n"+
					"                OPTIONAL {\n" +
					"                    ?descriptor bo:cites ?cites.\n" +
					"                    ?cites bibrdf:hasTitle ?title.\n" +					
					"					 OPTIONAL {?cites bo:DOI ?doi.}\n" +
					"                }.\n"+
					"		}\n";
	
				} else try {
					query =  Keys.valueOf(key.toString()).getSPARQL();
				} catch (Exception x) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,key.toString());
				}	
			}
		}
		return sparql(query,variant);		
	}

	protected void readOntologies(Model ontology) {
		String[] owls = new String[] {
				"opentox.owl",
				"bibtex-converted/descriptor-algorithms.owl",
				"echa-endpoints.owl",
				"AlgorithmTypes.owl",
				"bibtex-converted/descriptors-ambit.owl",
				"bibtex-converted/ist-algorithms.owl",
				"toxcast.owl",
				"bibtex.owl"
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
			
			
			MediaType[] mt = {
					MediaType.APPLICATION_RDF_XML,
					MediaType.TEXT_RDF_N3,					
					MediaType.TEXT_RDF_NTRIPLES,
					MediaType.APPLICATION_RDF_TURTLE,
					
			};
			HttpURLConnection cli = null;
			File tmpFile = null;
			for (MediaType m : mt) {
				InputStream in = null;
				try {
					cli = ClientResourceWrapper.getHttpURLConnection(reference.toString(), "GET", m.toString());
					cli.connect();
					if (HttpURLConnection.HTTP_OK== cli.getResponseCode()) {
						in = cli.getInputStream();
						tmpFile = File.createTempFile("ontologyservice_", ".rdf");
						tmpFile.setWritable(true);
						tmpFile.setReadable(true);
						
						DownloadTool.download(in, tmpFile);

						if (tmpFile.exists()) {
							readOWL(new File(tmpFile.getAbsolutePath()),model);
							in = null;
						}
						return model;
					} else throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
							String.format("%d %s %s",cli.getResponseCode(),cli.getResponseMessage(),reference.toString()));
				} catch (ResourceException x) {
					throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
							String.format("%s %s %s", Status.SERVER_ERROR_BAD_GATEWAY.toString(),reference,x.getMessage()),x);
				} catch (IOException x) {
					throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
							String.format("%s %s %s", Status.SERVER_ERROR_INTERNAL.toString(),reference,x.getMessage()),x);
				} catch (Exception x) {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,reference.toString(),x);
				} finally {
					try {if (in!=null) in.close();} catch (Exception x) {};
					try {if (cli!=null) cli.disconnect();} catch (Exception x) {};
					try {if (tmpFile!=null) tmpFile.delete();} catch (Exception x) {}
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
								"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"+
								"<!--[if lt IE 7 ]><html class=\"ie ie6\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> <![endif]-->"+
								"<!--[if IE 7 ]><html class=\"ie ie7\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> <![endif]-->"+
								"<!--[if IE 8 ]><html class=\"ie ie8\" lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> <![endif]-->"+
								"<!--[if (gte IE 9)|!(IE)]><!--><html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\"> <!--<![endif]-->"+											
								"<head><title>Search Opentox RDF</title>"+
								"<meta name=\"robots\" content=\"index,nofollow\"><META NAME=\"GOOGLEBOT\" CONTENT=\"index,NOFOLLOW\">" +
								"<meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1'>"
							    ));
							
							//css			
							for (String style : css ) w.write(String.format(style,getRequest().getRootRef()));
							//js
							for (String script : js ) w.write(String.format(script,getRequest().getRootRef()));

							w.write("<script>$(function() {$( \"#selectable\" ).selectable();});</script>");
							w.write("<script type='text/javascript'>function hideDiv(divId) {\n$('#'+divId).hide();}</script>\n");
							final String dtableOptions = "'bJQueryUI': true, "+
									//"'sPaginationType': 'full_numbers',"+
									"'bPaginate'      : true,"+
									//"'sDom' : '<\"remove-bottom\"i><p>Trt<lf>'";
									"\"sDom\": 'T<\"remove-bottom\"><\"fg-toolbar ui-widget-header ui-corner-tl ui-corner-tr ui-helper-clearfix\"lfr>t<\"fg-toolbar ui-widget-header ui-corner-bl ui-corner-br ui-helper-clearfix\"ip>'";

							w.write(String.format("<script>$(function() {$( \".datatable\" ).dataTable({%s });});</script>",dtableOptions));
							
							w.write("<script>$(function() {$(\"#submit\").button();});</script>");

							w.write("</head><body><div class='container' style='padding:5 5 5 5;margin-left:20px;margin-top:10px;'>\n");
							if (!resultsOnly) 	{
								w.write("<FORM>");
								writehtmlheader(w,ontology,queryString,elapsed);
							} else
								w.write(String.format("<h4>%s</h4>",title==null?"":title));
							
							//	w.write("<div class='row' id='header'><div class='ten columns'>TOP</div></div>");
								w.write("<table class='datatable row' id='results'>\n");		
							
								w.write("<thead>");
								List<String> vars = results.getResultVars();
								for (int i=0; i < vars.size();i++) {
									w.write("<th>");
									w.write(vars.get(i));
									w.write("</th>");
								}				
								w.write("</thead>");
								w.write("<tbody>");
								while (results.hasNext()) {
									QuerySolution s = results.next();
									w.write("<tr>");
									for (int i=0; i < vars.size();i++) {
										RDFNode node = s.get(vars.get(i));
										w.write("<td>");
										if (node != null) {
											String value = node.isLiteral()?((Literal)node).getString():PrintUtil.print(node);
											if ((value.indexOf("<")>=0) && (value.indexOf(">")>=0))//some xml inside
												w.write(String.format("<textarea readonly width='100%%'>%s</textarea>",value));
											else if (value.startsWith("http"))
												w.write(String.format("%s&nbsp;<a href='?uri=%s'>?</a>", value,Reference.encode(value)));
											else if ("doi".equals(vars.get(i)))
												w.write(String.format("&nbsp;<a href='http://dx.doi.org/%s'>%s</a>", value,value));														
											else	
												w.write(value);
										}
										w.write("</td>");
									}
									w.write("</tr>");
								}
								w.flush();
								w.write("</tbody></table>\n");
								

								if (!resultsOnly) {
									w.write("</FORM>");
									w.write(
											"<div class='row'>&nbsp;</div><div class='sixteen columns'>\n"+
											"<FORM action='' method='post'>"+
											"<div class='ten ui-widget-header ui-corner-top remove-bottom' >Import RDF data into Ontology service</div>"+
											"<div class='row ui-widget-content ui-corner-bottom remove-bottom'>"+
											"<div class='row'></div><div class='two columns alpha'></div>"+
											"<label for='uri' class='three columns omega'>URL</label>"+
										    "<input name='uri' class='eight columns omega'>"+
										    "<INPUT name='run' class='three columns omega' type='submit' value='SUBMIT'>"+
											"</div></div></FORM>\n"
													);		
									w.write(String.format("<div class='row add-bottom'>&nbsp;</div><div class='row'>Version:&nbsp;<a href='%s/meta/MANIFEST.MF' target=_blank alt='%s' title='Web application version'>%s</a></div>",
											getRequest().getRootRef(),
											version==null?"":version,
											version));
								}
								
								w.write(jsGoogleAnalytics()==null?"":jsGoogleAnalytics());
								w.write("</div></body>"); //container

					
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
	
	protected void readOWL(File file , Model model) throws Exception {
		FileInputStream in = new FileInputStream(file);
		readOWL(in, model);
	}	
	protected void readOWL(InputStream in , Model model) throws Exception {
		try {
			model.enterCriticalSection(Lock.WRITE) ;
			try {
				RDFReader reader = model.getReader();
				reader.setErrorHandler(new RDFErrorHandler() {
					
					@Override
					public void warning(Exception e) {
						e.printStackTrace();
						
					}
					
					@Override
					public void fatalError(Exception e) {
						e.printStackTrace();
						
					}
					
					@Override
					public void error(Exception e) {
						e.printStackTrace();
						
					}
				});
				reader.read(model, in,null);
				try { model.commit(); } catch (Exception x) {}
			} catch (Exception x) {
				x.printStackTrace();
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

	public void writehtmlheader(Writer w,Model ontology,String queryString,long elapsed) throws Exception {
		w.write("<div class='ui-widget-header row remove-bottom'>");
		
		Keys qkey = null;
		try { qkey = Keys.valueOf(getRequest().getAttributes().get(resourceKey).toString()); } catch (Exception x) {}
		StringBuilder b = new StringBuilder();
		
		for (Keys key : Keys.values()) {
			Keys parent = key.parent();
			
			String sparql = String.format("<a class='remove-bottom' href='%s/query/%s' title='%s\n\n%s'>%s</a>&nbsp;",
					getRequest().getRootRef(),
					key.name(),
					key.getHint(),
					key.getSPARQL(),
					key.toString()
						
					);
			
			if (parent==null) //top level
				w.write(sparql);	
			else {
				if (parent.equals(qkey)) { b.append(sparql);	b.append("|");}
			}
		}
		w.write("</div>");
		w.write("<div class='ui-widget-header row half-bottom'>");
		if (qkey!=null)
		w.write(String.format("<br>%s&nbsp;%s&nbsp;",qkey.toString(),"\u00BB"));
		w.write(b.toString());
		w.write("</div>");
								
		w.write(
				String.format(
			"<div class='row ui-widget-header ui-corner-top remove-bottom'>Search the Ontology service&nbsp;[%s triples]</div>"+
			"<div class='row ui-widget-content ui-corner-bottom'>"+
			"<FORM action='' method='post'>"+
		    "<TEXTAREA  class='columns sixteen' name='query' rows='10' cols='100' style='padding:10 10 10 10;'>",
		    ontology==null?0:ontology.size(),
		    getRequest().getRootRef()));
		w.flush();
		w.write(queryString);
		w.write(
		    "</TEXTAREA>"+
			"<div class='row remove-bottom' style='padding:5 5 5 5;'>"+
		    "<INPUT name=\"run\" type=\"submit\" value='Submit SPARQL' tabindex=\"2\">"
				);
		w.write("</div></div>");
	
		w.write(String.format("<div class='row'><div class='columns ten'>Results [found in %d ms]</div></div>",elapsed));

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
