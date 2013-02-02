package org.opentox.service.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.idea.restnet.aa.opensso.OpenSSOUser;
import net.idea.restnet.c.resource.CatalogResource;

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
	public void configureTemplateMap(Map<String, Object> map) {
		super.configureTemplateMap(map);
        if (getClientInfo().getUser()!=null) {
        //	map.put("username", getClientInfo().getUser().getIdentifier());
        	try {
        		map.put("openam_token",((OpenSSOUser) getClientInfo().getUser()).getToken());  
        		System.out.println(((OpenSSOUser) getClientInfo().getUser()).getFirstName());
        	} catch (Exception x) {
        		map.remove("openam_token");
        	}
        }
        try {
        	map.put("openam_service", OntServiceOpenSSOConfig.getInstance().getOpenSSOService());
        } catch (Exception x) {
        	map.remove("openam_service");
        }
        map.put("creator","IdeaConsult Ltd.");
        map.put("ambit_root",getRequest().getRootRef().toString());		
	}

	@Override
	protected Representation getHTMLByTemplate(Variant variant) throws ResourceException {
        Map<String, Object> map = new HashMap<String, Object>();
        if (getClientInfo().getUser()!=null) 
        	map.put("username", getClientInfo().getUser().getIdentifier());
        else {
			OpenSSOUser ou = new OpenSSOUser();
			ou.setUseSecureCookie(useSecureCookie(getRequest()));
			getClientInfo().setUser(ou);
		}
        setTokenCookies(variant, useSecureCookie(getRequest()));
        configureTemplateMap(map);
        return toRepresentation(map, getTemplateName(), MediaType.TEXT_PLAIN);
	}

}
