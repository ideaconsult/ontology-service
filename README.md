OpenTox Ontology service
----------------------------------------------------------------------------------------------
Deployment

Copy ontology.war under webapps directory of the servlet container. The application should be accessible at

http://localhost:8080/ontology
----------------------------------------------------------------------------------------------

Build

mvn package -P release -P tdb
----------------------------------------------------------------------------------------------

Maven dependency:

<dependency>
  <groupId>org.opentox</groupId>
  <artifactId>ontology</artifactId>
  <version>0.0.1</version>
  <type>war</type>
</dependency>

----------------------------------------------------------------------------------------------
Configuration:

Does not use MySQL database. Uses Jena TDB for persistent storage (file based), which is automatically generated on deploy. 

WEB-INF/classes/org/opentox/config/config.properties
#tdb or sdb 
persistence=${jena.persistence}
#where to store TDB files. Default is java temporary directory
tdb=${tdb.folder}

WEB-INF/classes/org/opentox/config/aa.properties
aa.opensso=${aa.opensso}
aa.policy=${aa.policy}
aa.enabled=${aa.enabled}
----------------------------------------------------------------------------------------------
Query examples

Retrieve all classification algorithms:

--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Classification>.
	}
--

All regression algorithms:
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Regression>.
	}

--
All clustering algorithms
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Clustering>.
	}
--

Single target algorithms

--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#SingleTarget>.
	}
--
Lazy learning AND single target

--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#SingleTarget>.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#LazyLearning>.
	}

--
Supervised learning algorithms
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Supervised>.
	}
--

All rule-based algorithms

--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Algorithm ?x ?y
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Rules>.
	}

--

All Models, build by rule-based algorithms
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Model
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Rules>.
        ?Model rdf:type ot:Model.
        ?Model ot:algorithm ?Algorithm. 
	}
--
All models, built by clustering algorithms
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Model
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Clustering>.
        ?Model rdf:type ot:Model.
        ?Model ot:algorithm ?Algorithm. 
	}
--

All models, built by regression algorithms and display predicted variables
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Model  ?Predicted
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Regression>.
        ?Model rdf:type ot:Model.
        ?Model ot:algorithm ?Algorithm. 
        ?Model ot:predictedVariables ?Predicted.
	}
--

All models, predicting Endpoints (of certain type)
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Model ?endpoint
	where {
	?Algorithm rdf:type ot:Algorithm.
        ?Algorithm rdf:type <http://www.opentox.org/algorithmTypes.owl#Rules>.
        ?Model rdf:type ot:Model.
        ?Model ot:algorithm ?Algorithm. 
        ?Model ot:predictedVariables ?Predicted.
        ?Predicted owl:sameAs ?endpoint.
        ?endpoint rdf:type otee:Endpoints.
	}


---
Select models, predicting Eye irritation/corrosion
---

PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select DISTINCT  ?Model
	where {
        ?Model rdf:type ot:Model.
        ?Model ot:predictedVariables ?Predicted.
        ?Predicted owl:sameAs <http://www.opentox.org/echaEndpoints.owl#Eye_irritation/corrosion>.

	}


---
Top level of endpoints ontology
---

PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Endpoints 
	where {
	?Endpoints rdfs:subClassOf otee:Endpoints
	}


---
Human health endpoints
---

PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
	select ?Endpoints 
	where {
	?Endpoints rdfs:subClassOf otee:HumanHealthEffects 
	}


--

All features same as http://www.opentox.org/echaEndpoints.owl#Endpoint Carcinogenicity  
--
PREFIX ot:<http://www.opentox.org/api/1.1#>
PREFIX ota:<http://www.opentox.org/algorithms.owl#>
PREFIX owl:<http://www.w3.org/2002/07/owl#>
PREFIX dc:<http://purl.org/dc/elements/1.1/#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX otee:<http://www.opentox.org/echaEndpoints.owl#>
select ?Feature ?SameAS
		where {
	        ?Feature owl:sameAs otee:Carcinogenicity
		}


--- 
Blue Obelisk ontology
All Molecular descriptors
---

PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX bo:<http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#>
select ?x 
		where {
	        ?x rdf:type bo:MolecularDescriptor
		}


OpenTox ontology service

Imported from 
https://ambit.svn.sourceforge.net/svnroot/ambit/branches/opentox/ontology-service

Running at http://apps.ideaconsult.net:8080/ontology
