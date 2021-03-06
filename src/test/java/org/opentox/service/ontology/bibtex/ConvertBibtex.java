package org.opentox.service.ontology.bibtex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ConvertBibtex {

	protected DocumentBuilder builder;
	protected static final String bibtex_xml_ns = "http://bibtexml.sf.net/";
	protected static final String bibtex_rdf_ns = "http://zeitkunst.org/bibtex/0.1/bibtex.owl#";
	
	public ConvertBibtex() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			builder = factory.newDocumentBuilder();
			
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	enum top {

		article,
		book,
		booklet,
		conference,
		inbook,
		incollection,
		inproceedings,
		manual,
		mastersthesis,
		misc,
		phdthesis,
		proceedings,
		techreport,
		unpublished;
		public String getBibTexName() {
			return Character.toUpperCase(name().charAt(0)) + name().substring(1);
		}		
		public String getURI() {
			return String.format("%s%s",bibtex_rdf_ns,getBibTexName());
		}
		public Property getProperty(Model rdf) {
			Property p = rdf.getProperty(getURI());
			if (p==null) p = rdf.createProperty(getURI());
			return p;
		}
	}
	enum tags {

		author,
		title,
		journal,
		year,
		volume,
		pages,
		isbn {
			@Override
			public String getBibTexName() {
				return "ISBN";
			}
		},
		doi,
		url {
			@Override
			public String getBibTexName() {
				return "URL";
			}			
		};
		public String getBibTexName() {
			return Character.toUpperCase(name().charAt(0)) + name().substring(1);
		}		
		public String getURI() {
			return String.format("%shas%s",bibtex_rdf_ns,getBibTexName());
		}
		public Property getProperty(Model rdf) {
			Property p = rdf.getProperty(getURI());
			if (p==null) p = rdf.createProperty(getURI());
			return p;
		}
	}
	/**
<pre>
            <bibtex:article xmlns:bibtex="http://bibtexml.sf.net/">
                <bibtex:author>Todeschini, R. and Gramatica, P.</bibtex:author>
                <bibtex:title>New 3D Molecular Descriptors: The WHIM theory and QAR Applications</bibtex:title>
                <bibtex:journal>Persepectives in Drug Discovery and Design</bibtex:journal>
                <bibtex:year>1998</bibtex:year>
                <bibtex:pages>355-380</bibtex:pages>
            </bibtex:article>
</pre>
	 * @param xml
	 * @throws Exception
	 */
	protected void parseBibteXML(Model rdf,Resource refURI,String xml) throws Exception {
		Document dom = builder.parse(new InputSource(new StringReader(xml)));
		Element article = dom.getDocumentElement();
		top toptag = null;
		try {
			toptag = top.valueOf(article.getLocalName());
		} catch (Exception x) {
			return;
		}
		
		rdf.add(refURI,RDF.type,toptag.getProperty(rdf));
		
		for (tags tag: tags.values()) {
			NodeList nodes = dom.getElementsByTagNameNS(bibtex_xml_ns, tag.name());
			for (int i=0; i < nodes.getLength();i++) {
				System.out.println(String.format("[%s] %s",tag.name(),nodes.item(i).getTextContent()));
				rdf.add(refURI,tag.getProperty(rdf),rdf.createLiteral(nodes.item(i).getTextContent()));
			}
		}
		
	}
	protected Model parseBO(InputStream boRDF) throws Exception {
			String boPrefix = "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/";
			Model rdf = ModelFactory.createDefaultModel();
			rdf.setNsPrefix("bo", boPrefix);
			rdf.read(boRDF,null);
			
			Model rdfBibtex = ModelFactory.createDefaultModel();
			rdfBibtex.setNsPrefix("bo", boPrefix);
			rdfBibtex.setNsPrefix("bibrdf",bibtex_rdf_ns);

			//Ontology o = rdf.getOntology("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/");
			//System.out.println(o);
			//rdf.enterCriticalSection(Lock.WRITE) ;
			//o.addImport(rdf.createResource(bibtex_rdf_ns));
			//rdf.leaveCriticalSection();
			
			//have to import it somehow instead
			/*
			Model bibrdf = ModelFactory.createDefaultModel();
			bibrdf.read("http://zeitkunst.org/bibtex/0.1/bibtex.owl");
			bibrdf.setNsPrefix("bibrdf",bibtex_rdf_ns);
			rdf.add(bibrdf);
			*/
			//rdf.enterCriticalSection(Lock.READ) ;
			RDFNode node = rdf.createResource("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#Reference");
			Property bibtexProp = rdf.createProperty("http://bibtexml.sf.net/entry");
			
			rdf.setNsPrefix("bibrdf",bibtex_rdf_ns);
			
			StmtIterator iter = rdf.listStatements(null,RDF.type,node);
			while (iter.hasNext()) {
				Statement st = iter.nextStatement();
				System.out.println(">>> "+st.getSubject());
				try {
					//rdf.enterCriticalSection(Lock.WRITE);
					Literal txt = st.getSubject().getProperty(bibtexProp).getLiteral();
					System.out.println(">>>> "+txt.getString());
					
					parseBibteXML(rdfBibtex,st.getSubject(),txt.getString());
				} catch (Exception x) {
					//System.out.println(bibtexProp);
				} finally {
					//rdf.leaveCriticalSection();
				}

				
			}
			iter.close();
			//
			return rdfBibtex;

	}
	public static void main(String[] args) {
		String infile = args[0];
		try {
			String boPrefix = "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#";
			
			ConvertBibtex bx = new ConvertBibtex();
			File input = new File(infile);
			Model rdf = bx.parseBO(new FileInputStream(input));
			OntModel owl = ModelFactory.createOntologyModel();
			owl.add(rdf);
			owl.setNsPrefix("bibrdf",bibtex_rdf_ns);
			owl.setNsPrefix("bo", boPrefix);
			owl.setNsPrefix("", boPrefix);
			owl.setNsPrefix("bibxml","http://bibtexml.sf.net/");
			Ontology ont = owl.createOntology("http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/");
			ont.setImport(owl.createResource("http://zeitkunst.org/bibtex/0.1/bibtex.owl"));

			RDFWriter writer = owl.getWriter("RDF/XML-ABBREV");
			writer.setProperty("xmlbase",owl.getNsPrefixURI(""));
			writer.setProperty("showXmlDeclaration", Boolean.TRUE);
			writer.setProperty("showDoctypeDeclaration", Boolean.TRUE);	
			
			writer.write(owl,new FileWriter("converted.owl"),boPrefix);
			
			rdf.close();
			owl.close();
		} catch (Exception x) {
			x.printStackTrace();
		} finally {
			
		}
	}
}
