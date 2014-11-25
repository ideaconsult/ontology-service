package org.opentox.service.ontology;

import java.util.HashMap;
import java.util.Map;

import net.idea.restnet.aa.opensso.OpenSSOUser;
import net.idea.restnet.c.freemarker.FreeMarkerResource;
import net.idea.restnet.i.freemarker.IFreeMarkerApplication;

import org.opentox.rest.component.OntServiceOpenSSOConfig;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

public class UIResource extends FreeMarkerResource {
	private static final String key = "key";
	protected pages page = pages.index;
	private enum pages { 
			index {
				@Override
				public void setCacheHeaders(Response response) {
					response.getCacheDirectives().add(CacheDirective.publicInfo());
				}
			},
			model
			;
			public boolean enablePOST() {
				return false;
			}
			public void setCacheHeaders(Response response) {
				response.getCacheDirectives().add(CacheDirective.noCache());
			}
		}
	public UIResource() {
		super();
		setHtmlbyTemplate(true);
	}
	@Override
	public boolean isHtmlbyTemplate() {
		return true;
	}
	
	
	
	@Override
	public String getTemplateName() {
		//Object ui = getRequest().getAttributes().get(key);
		try {
			//page = pages.valueOf(ui.toString());
			//return ui==null?"index.ftl":String.format("%s.ftl", page.name());
			switch (page) {
			case index : {
				return String.format("menu/profile/%s/index.ftl",((IFreeMarkerApplication)getApplication()).getProfile());
			}
			default: {
				return String.format("%s.ftl", page.name());
			}
			}
			
		} catch (Exception x) {
			return String.format("%s.ftl", page.name());
		}
	}
	
	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
		try {
			Object ui = getRequest().getAttributes().get(key);
			page = pages.valueOf(ui.toString());
		} catch (Exception x) {
			page = pages.index;
		}
        
	}
	protected Representation getHTMLByTemplate(Variant variant) throws ResourceException {
        Map<String, Object> map = new HashMap<String, Object>();

		if (getClientInfo()!=null) {
			if (getClientInfo().getUser()!=null)
				map.put("username", getClientInfo().getUser().getIdentifier());
			if (getClientInfo().getRoles()!=null) {

			}
		}
		
        setTokenCookies(variant, useSecureCookie(getRequest()));
        configureTemplateMap(map,getRequest(),(IFreeMarkerApplication)getApplication());
        return toRepresentation(map, getTemplateName(), MediaType.TEXT_PLAIN);
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
			ou.setUseSecureCookie(useSecureCookie(getRequest()));
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
	protected void setCacheHeaders() {
		if (page!=null) page.setCacheHeaders(getResponse());
		else
			super.setCacheHeaders();	
		
	}
	@Override
	protected Representation getRepresentation(Variant variant)
			throws ResourceException {
		switch (page) {
		
		}
		return super.getRepresentation(variant);
	}
	@Override
	protected Representation post(Representation entity, Variant variant)
			throws ResourceException {
		Object ui = getRequest().getAttributes().get(key);
		try {
			pages page= pages.valueOf(ui.toString()); 
			if (page.enablePOST()) {
				return null;
			} 
		} catch (Exception x) { 
			x.printStackTrace();
		}
		throw new ResourceException(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);

	}
		
}
