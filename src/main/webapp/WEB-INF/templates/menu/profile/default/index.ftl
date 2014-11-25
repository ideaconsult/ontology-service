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
	    <h6>Welcome</h6>			
	    </div>
		</div>
</div>		
<div class="row remove-bottom" >
	  <div id="header_bottom" class="remove-bottom">&nbsp;</div>
</div>

<div class="three columns" style="padding:0 2px 2px 2px 0;margin-right:0;" >
	<#include "/menu/profile/default/menu.ftl">
</div>

		
<!-- Page Content
================================================== -->

<div class="eleven columns" style="padding:0;" >
	Provides storage and standard SPARQL endpoint search functionality for objects, defined in OpenTox services and relevant ontologies.
	<br/>
	<a href="http://ambit.sourceforge.net/api_ontology.html" class='qxternal'>Documentation</a>
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
<br/>
<a href='http://www.opentox.org'><img src=${ambit_root}/images/logo.png border='0' width='115' height='60'></a>
</div>

<div class='row add-bottom'>&nbsp;</div>

<#include "/footer.ftl" >
</div> <!-- container -->
</body>
</html>