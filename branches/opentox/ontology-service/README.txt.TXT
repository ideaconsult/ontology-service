OpenTox Ontology service

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

--