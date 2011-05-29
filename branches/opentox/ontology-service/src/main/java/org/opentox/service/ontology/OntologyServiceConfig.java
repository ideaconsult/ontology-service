package org.opentox.service.ontology;

import java.util.Properties;

import org.opentox.aa.exception.AAException;


public class OntologyServiceConfig  extends AbstractConfig {
	enum Persistence {
		tdb,
		sdb
	}
	

	public static enum CONFIG {
		persistence {
			@Override
			public String getDescription() {
				return "Jena persistence";
			}
		},
	
		tdb {
			@Override
			public String getDescription() {
				return "TDB folder";
			}					
		};		

		public String getKey() {
			return String.format("%s",toString());
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
	
	private static OntologyServiceConfig ref;
	private OntologyServiceConfig() throws AAException {
		super("org/opentox/config/config.properties");
	}

	public static synchronized OntologyServiceConfig getInstance() throws AAException  {
	    if (ref == null)
	        ref = new OntologyServiceConfig();		
	    return ref;
	}
	
	protected  synchronized String getConfig(CONFIG config) throws AAException  {
		return properties==null?null:config.getValue(properties);
	}		
	/**
	 * TDB folder.  Default is temp directory, retrieved via System.getProperty("java.io.tmpdir")
	 * @return
	 * @throws AAException
	 */
	public  String getTDBDirectory() throws AAException  {
	    String dir =  getConfig(CONFIG.tdb);
	    if ((dir == null) || "".equals(dir.trim()) || "${tdb.folder}".equals(dir)) {
	    	dir = String.format("%s/tdb",System.getProperty("java.io.tmpdir"));
	    	properties.put(CONFIG.tdb,dir);
	    	return dir;
	    } else return dir.trim();
	}	
	/**
	 * 
	 * @return 
	 * @throws AAException
	 */
	public  Persistence getPersistence() throws AAException  {
		try {
			String persistence = getConfig(CONFIG.persistence);
			if ("${jena.persistence}".equals(persistence)) return Persistence.tdb;
			return Persistence.valueOf(persistence);
		} catch (Exception x) {
			return Persistence.tdb;
		}
	}	
}
