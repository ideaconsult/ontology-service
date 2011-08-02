package org.opentox.rest.component;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;

/**
 * Implements authentication via OpenSSO service:
 * Verifier should be set to {@link OpenSSOFakeVerifier}	
 * @author nina
 *
 */
public class OpenSSOAuthenticator extends ChallengeAuthenticator {

	public OpenSSOAuthenticator(Context context, boolean optional, String realm) throws Exception  {
		this(context,optional,realm,new OpenSSOFakeVerifier());
	}
	public OpenSSOAuthenticator(Context context, boolean optional, String realm,OpenSSOFakeVerifier verifier) {
		super(context, optional, getOpenSSOChallengeScheme(), realm);
		setVerifier(verifier);
	}

    public static ChallengeScheme getOpenSSOChallengeScheme() {
    	return OpenSSOChallengeScheme.OpenSSO;
    }

    @Override
    protected ChallengeRequest createChallengeRequest(boolean stale) {
    	return super.createChallengeRequest(stale);
    }
    
    @Override
    protected boolean authenticate(Request request, Response response) {
    	return super.authenticate(request, response);
    }

}
