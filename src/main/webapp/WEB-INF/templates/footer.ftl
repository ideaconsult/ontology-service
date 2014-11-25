<div class="sixteen  columns add-bottom">&nbsp;</div>

<div class='row add-bottom' >
	<div class="five columns alpha ">&nbsp;</div>
	<div class="ten columns omega ui-state-error " id='log' style='display:none;' >&nbsp;</div> 
</div>

		<div id='footer-out' class="sixteen columns">
		<div id='footer-in'>
		<div id='footer'>
			<a class='footerLink' href='http://www.ideaconsult.net/' title='Developed By'>IdeaConsult Ltd.</a> 
		</div>
		</div>
		</div>
		
		
<#if username??>
		<form id='logoutForm' name='logoutForm' action='${ambit_root}/openssouser?method=DELETE' method='POST'><input type='hidden' value='${username}'></input></form>
</#if>
				
<script type="text/javascript">
	var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
	document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
		
