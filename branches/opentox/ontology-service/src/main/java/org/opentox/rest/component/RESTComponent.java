package org.opentox.rest.component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.opentox.service.ontology.OntologyService;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

public class RESTComponent extends Component {

	public RESTComponent() {
		this(null);
	}
	public RESTComponent(Context context) {
		this(context,new Application[] {new OntologyService()});
	}
	public RESTComponent(Context context,Application[] applications) {
		super();
		this.getClients().add(Protocol.FILE);
		this.getClients().add(Protocol.HTTP);
		this.getClients().add(Protocol.HTTPS);

		for (Application application: applications) {
			application.setContext(context==null?getContext().createChildContext():context);
		    getDefaultHost().attach(application);
		}
	    getInternalRouter().attach("/",applications[0]);					
	
	}
	
	static   {

		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};

		// Install the all-trusting trust manager
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
		HttpsURLConnection.setDefaultHostnameVerifier( 
				new HostnameVerifier(){
					public boolean verify(String string,SSLSession ssls) {
						return true;
					}
				});
	}

}