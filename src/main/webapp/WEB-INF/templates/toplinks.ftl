	<ul class='topLinks remove-bottom'>

	<li class='topLinks'>|</li>
	<#if username??>
		<li class='topLinks'>
		<a class='topLinks login' title='You are currently logged in as "${username}". Click here to log out.' href='#' onClick='document.forms["logoutForm"].submit(); return false;'>Log out</a>&nbsp;
		<a class='topLinks login' title='My profile' href='${ambit_root}/openssouser'>[<b>${username}</b>]</a>
		</li>
	<#else>
		<li class='topLinks'>
			<a class='topLinks login' title='OpenTox Log in' href='${ambit_root}/openssouser'>Log in</a>
		</li>						
		<li class='topLinks'>|</li>
		<li class='topLinks'>
		<a class='topLinks register' title='Register' target=_blank href='http://opentox.org/join_form'>Register</a></li>				
		</#if>				
	</ul>