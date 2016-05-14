package org.opentox.service.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.idea.restnet.aa.opensso.OpenSSOUser;
import net.idea.restnet.c.resource.CatalogResource;
import net.idea.restnet.i.freemarker.IFreeMarkerApplication;

import org.opentox.rest.component.OntServiceOpenSSOConfig;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

public class ImportResource extends CatalogResource<String> {
	
	private List<String> query;
	public ImportResource() {
		super();
		query = new ArrayList<String>();
		query.add("import");
		setHtmlbyTemplate(true);
		
	}
	@Override
	public String getTemplateName() {
		return "import.ftl";
	}
	@Override
	protected Iterator<String> createQuery(Context context, Request request,
			Response response) throws ResourceException {
		return query.iterator();
	}
	
	@Override
	public void configureTemplateMap(Map<String, Object> map, Request request,
			IFreeMarkerApplication app) {
		super.configureTemplateMap(map,request,app);
        if (getClientInfo().getUser()!=null) {
        	map.put("username", getClientInfo().getUser().getIdentifier());
        	try {
        		map.put("openam_token",((OpenSSOUser) getClientInfo().getUser()).getToken());  
        	} catch (Exception x) {
        		map.remove("openam_token");
        	}
        } else {
        	OpenSSOUser ou = new OpenSSOUser();
			getClientInfo().setUser(ou);
        }
        try {
        	map.put("openam_service", OntServiceOpenSSOConfig.getInstance().getOpenSSOService());
        } catch (Exception x) {
        	map.remove("openam_service");
        }
        map.put("creator","IdeaConsult Ltd.");
        map.put("ambit_root",getRequest().getRootRef().toString());
        map.put("ambit_version_short",app.getVersionShort());
	    map.put("ambit_version_long",app.getVersionLong());
	    //map.put(AMBITConfig.googleAnalytics.name(),app.getGACode());
	    map.put("menu_profile",app.getProfile());        
	}

	@Override
	protected Representation getHTMLByTemplate(Variant variant) throws ResourceException {
        Map<String, Object> map = new HashMap<String, Object>();
        setTokenCookies(variant, useSecureCookie(getRequest()));
        configureTemplateMap(map,getRequest(),(IFreeMarkerApplication)getApplication());
        return toRepresentation(map, getTemplateName(), MediaType.TEXT_PLAIN);
	}

}
