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
	    <h6>Administration</h6>			
	    </div>
		</div>
</div>		
<div class="row remove-bottom" >
	  <div id="header_bottom" class="remove-bottom">&nbsp;</div>
</div>

<div class="three columns" style="padding:0 2px 2px 2px 0;margin-right:0;" >
		<#include "/menu/profile/extended/menu.ftl">
</div>

		
<!-- Page Content
================================================== -->

<div class="eleven columns" style="padding:0;" >
	<div class="row" style="padding:5;" >
		<a href='${ambit_root}/admin'>Browse Triple storage content</a>
	</div>
	<hr/>
	<div class="row" style="padding:5;" >
		<form action='${ambit_root}/admin?method=POST' method='POST' autocomplete='off'>
			<label class='three columns alpha'>Add default ontologies</label>
			<span class='thirteen columns omega'>
			<input id='submit' type='submit' value='Submit' tabindex='2'>
			</span>
		</form>
	</div>
	<hr/>
	<div class="row" style="padding:5;" >
		<form action='${ambit_root}/admin?method=DELETE' method='POST' autocomplete='off'>
			<label class='three columns alpha'>Clear all content</label>
			<span class='thirteen columns omega'>
			<input id='submit' type='submit' title='Clear all triples' value='Submit' tabindex='2'>
			</span>
		</form>
	</div>
	<!--
	<hr/>
	<div class="row" style="padding:5;" >
		<form action='${ambit_root}/query?method=POST' id='algorithms' method='POST' autocomplete='off'>
			<span class='row'>
			<label class='three columns alpha'>AMBIT URI</label>
			<input class='eight columns omega' name='uri' type='text' size='120' value=''>
			<span class='five columns omega'>
			<input id='submit' type='submit' value='Import algorithms' tabindex='2'>
			</span>
			</span>
		</form>
	</div>			
	<hr/>
	<div class="row" style="padding:5;" >
		<form action='${ambit_root}/query?method=POST' id='models' method='POST' autocomplete='off'>
			<span class='row'>
			<label class='three columns alpha'>AMBIT URI</label>
			<input class='eight columns omega' name='uri' type='text' size='120' value=''>
			<span class='five columns omega'>
			<input id='submit' type='submit' value='Import models' tabindex='2'>
			</span>
			</span>
		</form>
	</div>	
	-->
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