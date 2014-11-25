<#include "/html.ftl">
<head>
  <#include "/header.ftl">
</head>
<body>

<div class="container" style="margin:0;padding:0;">

<!-- banner -->
<div class="row remove-bottom" id="header">
	<#include "/toplinks.ftl">
</div>
<div class="row remove-bottom">
		<#include "/logo.ftl">
		<div class="thirteen columns remove-bottom" id="query">
		<div class="alpha">
		<h3 class="remove-bottom">
				OpenTox Ontology service
		</h3>
	    <h6>Registration of OpenTox resources</h6>			
	    </div>
		</div>
</div>		
<div class="row remove-bottom" >
	  <div id="header_bottom" class="remove-bottom">&nbsp;</div>
</div>

<div class="three columns" style="padding:0 2px 2px 2px 0;margin-right:0;" >
<#if menu_profile??>
		<#include "/menu/profile/${menu_profile}/menu.ftl">
<#else>
		<#include "/menu/profile/default/menu.ftl">
</#if>
</div>

		
<!-- Page Content
================================================== -->

<div class="eleven columns" style="padding:0;" >
	<div class="row remove-bottom ui-widget-header ui-corner-top">
		Import RDF data into Ontology service
	</div>
	<form method='post' action='${ambit_root}/query'>
	<div class="ui-widget-content ui-corner-bottom">
	<div style='margin:5px;padding:5px;'>
	<div class="row">
	The Ontology service provides RDF storage and SPARQL endpoint for objects, 
	 defined in OpenTox ontology and relevant 
	(<a href='${ambit_root}/query/Algorithm'>Algorithm Types</a> , 
	 <a href='${ambit_root}'>Blue Obelisk</a>, 
	 <a href='${ambit_root}/query/Endpoints'>ECHA endpoints</a>) ontologies. 
	<br/>
	Algorithms, models, reports, validation, datasets (metadata) and features 
	(if they are dependent and/or predicted variables only) can be registered, 
	the resource owner decides which resources are published. 
	<br/>
	The Ontology service adds new information to its storage, 
	by retrieving RDF representation of OpenTox objects (e.g. Algorithm, Model, Feature, etc.) from OpenTox services. 
	<br/>
	<br/>
	Enter URL of an OpenTox resource. On <b>Submit</b> the RDF representation of the resource will be 
	retrieved and stored in the Ontology service triple storage.	
	</div>
	<div class="row">
		<div class='two columns alpha'>URL</div> 
		<input class='ten columns omega' name='uri' type='text' size='120' value=''>
		<div class='four columns omega'>
			<INPUT name='run' type='submit' value='SUBMIT'>
		</div>
		</div>
	</div>
	</div>
	</form>
</div>


<!-- Right column and footer
================================================== -->
		
<div class="two columns help" style="margin:0;padding:0;" >
<span class='ui-icon ui-icon-info' style='float: left; margin-right: .3em;'></span>
The OpenTox Ontology service provides storage and standard 
<a href='http://www.w3.org/TR/rdf-sparql-protocol' target=_blank>SPARQL endpoint</a>
 search functionality for objects, defined in OpenTox services and relevant ontologies.
 <br/>
 <span class='ui-icon ui-icon-info' style='float: left; margin-right: .3em;'></span>
 If you are not logged in, you could only register OpenTox resources which are publicly readable.
 <br/>
 <span class='ui-icon ui-icon-info' style='float: left; margin-right: .3em;'></span>
<a href='http://ambit.sourceforge.net/download_ontologyservice.html'>Help</a>
<span class='ui-icon ui-icon-info' style='float: left; margin-right: .3em;'></span>
<a href='http://ambit.sourceforge.net/api_ontology.html' target=_blank>API</a><br>
</div>

<div class='row add-bottom'>&nbsp;</div>
<#include "/footer.ftl" >
</div> <!-- container -->
</body>
</html>