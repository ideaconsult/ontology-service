package org.opentox.rest.aa.opensso.users;

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.idea.modbcum.i.exceptions.AmbitException;
import net.idea.modbcum.i.processors.IProcessor;
import net.idea.restnet.aa.opensso.OpenSSOUser;
import net.idea.restnet.aa.opensso.users.OpenSSOUserHTMLReporter;
import net.idea.restnet.aa.opensso.users.OpenSSOUserResource;
import net.idea.restnet.aa.opensso.users.OpenSSOUsersURIReporter;
import net.idea.restnet.c.StringConvertor;
import net.idea.restnet.i.freemarker.IFreeMarkerApplication;

import org.opentox.aa.exception.AAException;
import org.opentox.rest.component.OntServiceOpenSSOConfig;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

public class OntologyOpenSSOResource extends OpenSSOUserResource {

	public OntologyOpenSSOResource() {
		super();
		setHtmlbyTemplate(true);
	}
	@Override
	public String getTemplateName() {
		return "login_opensso.ftl";
	}
	@Override
	public IProcessor<Iterator<OpenSSOUser>, Representation> createConvertor(
			Variant variant) throws AmbitException, ResourceException {

		if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
			return new StringConvertor(
					new OpenSSOUserHTMLReporter(getRequest(),getDocumentation(),getHTMLBeauty()),MediaType.TEXT_HTML);
		} else if (variant.getMediaType().equals(MediaType.TEXT_URI_LIST)) {
			return new StringConvertor(	new OpenSSOUsersURIReporter(getRequest(),getDocumentation()) {
				@Override
				public void processItem(OpenSSOUser src, Writer output) {
					super.processItem(src, output);
					try {
					output.write('\n');
					} catch (Exception x) {}
				}
			},MediaType.TEXT_URI_LIST);
			
		} else //html 	
			return new StringConvertor(
					new OpenSSOUserHTMLReporter(getRequest(),getDocumentation(),getHTMLBeauty()),MediaType.TEXT_HTML);
		
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
        if (getClientInfo().getUser()!=null) 
        	map.put("username", getClientInfo().getUser().getIdentifier());
        else {
			OpenSSOUser ou = new OpenSSOUser();
			getClientInfo().setUser(ou);
		}
        setTokenCookies(variant, useSecureCookie(getRequest()));
        configureTemplateMap(map,getRequest(),(IFreeMarkerApplication)getApplication());
        return toRepresentation(map, getTemplateName(), MediaType.TEXT_PLAIN);
	}
	@Override
	protected String getOpenSSOService() throws AAException {
		return OntServiceOpenSSOConfig.getInstance().getOpenSSOService();
	}
}
