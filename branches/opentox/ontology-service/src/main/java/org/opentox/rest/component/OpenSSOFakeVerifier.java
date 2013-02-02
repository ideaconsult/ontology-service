package org.opentox.rest.component;

import java.util.Hashtable;

import net.idea.restnet.aa.opensso.OpenSSOVerifier;

import org.opentox.aa.OTAAParams;
import org.opentox.aa.opensso.OpenSSOToken;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * subjectid=token , as header parameter 
 * doesn't verify the token, will just pass it to downstream services
 * @author nina
 *
 */
public class OpenSSOFakeVerifier extends OpenSSOVerifier {
	
	public OpenSSOFakeVerifier() throws Exception {
		this( OntServiceOpenSSOConfig.getInstance().isEnabled());
	}

	public OpenSSOFakeVerifier(boolean enabled) {
		super(enabled);
	}
	@Override
	public int verify(Request request, Response response) {
		try {
			
			Form headers = (Form) request.getAttributes().get("org.restlet.http.headers");
			String token = null;
			if (headers!=null) 
				token = headers.getFirstValue(OTAAParams.subjectid.toString());
	
			if (token == null) //backup, check cookies
				token = getTokenFromCookies(request);
			
			if (token==null) { //still nothing  
				request.getCookies().removeAll("subjectid");
				return enabled?Verifier.RESULT_MISSING:Verifier.RESULT_VALID;
		    } else token = token.trim();
			
			
			if ((token != null) && (!"".equals(token))) {
				OpenSSOToken ssoToken = new OpenSSOToken(OntServiceOpenSSOConfig.getInstance().getOpenSSOService());
				ssoToken.setToken(token);
				try {
					//if (ssoToken.isTokenValid()) {
						setUser(ssoToken, request);
						return enabled?Verifier.RESULT_INVALID:Verifier.RESULT_VALID;
						/*
					} else {
						request.getCookies().removeAll("subjectid");
						return enabled?Verifier.RESULT_INVALID:Verifier.RESULT_VALID;
					}
					*/
				} catch (Exception x) {
					x.printStackTrace(); //TODO
					return enabled?Verifier.RESULT_MISSING:Verifier.RESULT_VALID;
				}
			} else {
				request.getCookies().removeAll("subjectid");
				return enabled?Verifier.RESULT_MISSING:Verifier.RESULT_VALID;
			}
		
		}catch (Exception x) {
			x.printStackTrace();
			return Verifier.RESULT_MISSING;
		}

	}

	@Override
	protected User createUser(OpenSSOToken ssoToken, Request request)
			throws Exception {

		User user = super.createUser(ssoToken, request);
		Hashtable<String,String> results = new Hashtable<String, String>();
				
		try {
			ssoToken.getAttributes(new String[] {"uid"}, results);

			user.setIdentifier(results.get("uid"));} 
		catch (Exception x) {
			x.printStackTrace();
		}
		return user;
	}
		
}
