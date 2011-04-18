package org.opentox.service.ontology;
import org.opentox.rest.component.OpenSSOAuthenticator;
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

public class OntologyService extends Application {


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
		Router router = new MyRouter(this.getContext());
		router.attach("", TDBOntologyResource.class);
		router.attach("/", TDBOntologyResource.class);
		router.attach(String.format("/query/{%s}",TDBOntologyResource.resourceKey), TDBOntologyResource.class);	
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH); 
	    router.setRoutingMode(Router.MODE_BEST_MATCH); 
	    
		 Directory metaDir = new Directory(getContext(), "war:///META-INF");
		 Directory jqueryDir = new Directory(getContext(), "war:///jquery");
		 Directory styleDir = new Directory(getContext(), "war:///style");
 		 router.attach("/meta/", metaDir);
		 
 		 router.attach("/jquery/", jqueryDir);
 		router.attach("/style/", styleDir); 	
 		
		//Just sets the token, don't return error if not valid one
		Filter authn = new OpenSSOAuthenticator(getContext(),false,"opentox.org",new OpenSSOFakeVerifier(false));
		authn.setNext(router);
 		
		return authn;
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