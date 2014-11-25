package org.opentox.service.ontology;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import net.idea.restnet.aa.opensso.OpenSSOAuthenticator;
import net.idea.restnet.aa.opensso.users.OpenSSOUserResource;
import net.idea.restnet.c.freemarker.FreeMarkerApplication;

import org.opentox.rest.aa.opensso.users.OntologyOpenSSOResource;
import org.opentox.rest.component.OpenSSOFakeVerifier;
import org.opentox.rest.component.RESTComponent;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

/**
 * OpenTox ontology service
 * @author nina
 */

public class OntologyService extends FreeMarkerApplication<String> {
	static final String ambitProperties = "org/opentox/config/config.properties";
	protected Hashtable<String,Properties> properties = new Hashtable<String, Properties>();
	static final String version = "ambit.version";
	static final String version_build = "ambit.build";
	static final String version_timestamp = "ambit.build.timestamp";

	
	public OntologyService() {
		super();
		setName("OpenTox ontology services");
		setDescription("OpenTox ontology services");
		setOwner("Ideaconsult Ltd.");
		setAuthor("Ideaconsult Ltd.");		
		/*
		String tmpDir = System.getProperty("java.io.tmpdir");
        File logFile = new File(tmpDir,"ambit2-www.log");		
		System.setProperty("java.util.logging.config.file",logFile.getAbsolutePath());
		*/
	}
	@Override
	public Restlet createInboundRoot() {
		initFreeMarkerConfiguration();
		Router router = new MyRouter(this.getContext());
		router.attach("/", UIResource.class);
		router.attach("", UIResource.class);
		router.attach("/query", TDBOntologyResource.class);
		router.attach(String.format("/query/{%s}",TDBOntologyResource.resourceKey), TDBOntologyResource.class);	
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH); 
	    router.setRoutingMode(Router.MODE_BEST_MATCH); 
	    
 		//Restlet login = createOpenSSOLoginRouter();
		/**
		 * OpenSSO login / logout
		 * Sets a cookie with OpenSSO token
		 */
		router.attach("/"+OpenSSOUserResource.resource,OntologyOpenSSOResource.class );
		router.attach("/import",ImportResource.class );
	    
		 Directory metaDir = new Directory(getContext(), "war:///META-INF");
		 Directory jqueryDir = new Directory(getContext(), "war:///jquery");
		 Directory styleDir = new Directory(getContext(), "war:///style");
		 Directory scriptsDir = new Directory(getContext(), "war:///scripts");
		 Directory imgDir = new Directory(getContext(), "war:///images");
 		 router.attach("/meta/", metaDir);
		 
 		 router.attach("/jquery/", jqueryDir);
 		 router.attach("/style/", styleDir);
 		 router.attach("/scripts/", scriptsDir);
 		 router.attach("/images/", imgDir);
 		
		//Just sets the token, don't return error if not valid one
		Filter authn = new OpenSSOAuthenticator(getContext(),false,"opentox.org",new OpenSSOFakeVerifier(false));
		authn.setNext(router);
		
		setProfile(getMenuProfile());
		versionShort = readVersionShort();
		versionLong = readVersionLong();
 		
		return authn;
	}
	public synchronized String readVersionShort()  {
		try {
			return getProperty(version,ambitProperties);
		} catch (Exception x) {return "Unknown"; }
	}

	public synchronized String readVersionLong()  {
		try {
			String v1 = getProperty(version,ambitProperties);
			String v2 = getProperty(version_build,ambitProperties);
			String v3 = getProperty(version_timestamp,ambitProperties);
			return String.format("%s r%s built %s",v1,v2,new Date(Long.parseLong(v3)));
		} catch (Exception x) {return "Unknown"; }
	}	
	public synchronized String getMenuProfile() {
		String prefix = getProperty("ambit.profile",ambitProperties);
		if (prefix == null || "".equals(prefix) || prefix.contains("${")) prefix = "default";
		return prefix;
	}
	/**
	 * Standalone, for testing mainly
	 * @param args
	 * @throws Exception
	 */
    public static void main(String[] args) throws Exception {
        
        // Create a component
        Component component = new RESTComponent(null,new Application[]{new OntologyService()});
        final Server server = component.getServers().add(Protocol.HTTP, 8081);
        component.start();
   
        System.out.println("Server started on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        component.stop();
        System.out.println("Server stopped");
    }
    /*
	protected Restlet createOpenSSOLoginRouter() {
		Filter userAuthn = new OpenSSOAuthenticator(getContext(),true,"opentox.org",new OpenSSOFakeVerifier(false));
		userAuthn.setNext(OntologyOpenSSOResource.class);
		return userAuthn;
	}
	*/
    
	protected synchronized String getProperty(String name,String config)  {
		try {
			Properties p = properties.get(config);
			if (p==null) {
				p = new Properties();
				InputStream in = this.getClass().getClassLoader().getResourceAsStream(config);
				p.load(in);
				in.close();
				properties.put(config,p);
			}
			return p.getProperty(name);

		} catch (Exception x) {
			return null;
		}
	}	
}


/**
 * For backward compatibility with 2.0-M5 and before
 */
class MyRouter extends Router {
	public MyRouter(Context context) {
		 super(context);
	     setDefaultMatchingMode(Template.MODE_STARTS_WITH); 
	     setRoutingMode(Router.MODE_BEST_MATCH); 
	}
}