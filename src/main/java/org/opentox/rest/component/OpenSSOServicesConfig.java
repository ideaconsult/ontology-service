package org.opentox.rest.component;

import java.util.Properties;

import org.opentox.aa.exception.AAException;
import org.opentox.service.ontology.AbstractConfig;

/**
 * Configuration for OpenSSO authn, authz and policy services
 * @author nina
 *
 */
public class OpenSSOServicesConfig extends AbstractConfig{
	
	private static OpenSSOServicesConfig ref;
	
	
	public static enum CONFIG {
		enabled {
			@Override
			public String getDescription() {
				return "AA enabled/disabled";
			}
		},
		opensso {
			@Override
			public String getDescription() {
				return "authentication";
			}
		},
		user {
			@Override
			public String getDescription() {
				return "test user";
			}
		},
		pass {
			@Override
			public String getDescription() {
				return "test user password";
			}
		},

		policy {
			@Override
			public String getDescription() {
				return "policy";
			}					
		};		

		public String getKey() {
			return String.format("aa.%s",toString());
		}
		public String getValue(Properties properties) {
			try {
				if (properties == null) return null;
				Object o = properties.get(getKey());
				return o==null?null:o.toString();
			} catch (Exception x) {
				return getDefaultValue();
			}
		}
		public String getDefaultValue() {
			return null;
		}
		public abstract String getDescription() ;

	}	
	private OpenSSOServicesConfig() throws AAException {
		super("org/opentox/config/aa.properties");
	
	}	

	public static synchronized OpenSSOServicesConfig getInstance() throws AAException  {
	    if (ref == null)
	        ref = new OpenSSOServicesConfig();		
	    return ref;
	}
	
	protected  synchronized String getConfig(CONFIG config) throws AAException  {
		return properties==null?null:config.getValue(properties);
	}	
	
	public String getTestUser() throws AAException  {
	    return getConfig(CONFIG.user);
	}	
		
	public String getTestUserPass() throws AAException  {
	    return getConfig(CONFIG.pass);
	}	
	
	public  String getOpenSSOService() throws AAException  {
	    return getConfig(CONFIG.opensso);
	}	
	public  String getPolicyService() throws AAException  {
	    return getConfig(CONFIG.policy);
	}	
	
	public  boolean isEnabled() throws AAException  {
	    String e =  getConfig(CONFIG.enabled);
	    return "true".equals(e.toLowerCase());
	}		
	
	public Object clone() throws CloneNotSupportedException {
	    throw new CloneNotSupportedException(); 
    }

}
