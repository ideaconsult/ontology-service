package org.opentox.service.ontology;

import java.io.InputStream;
import java.util.Properties;

import org.opentox.aa.exception.AAException;
import org.opentox.aa.exception.AAPropertiesException;

public class AbstractConfig {
	protected Properties properties;
	
	public AbstractConfig(String propertiesFile) throws AAException {
		properties = new Properties();
		InputStream in = getClass().getClassLoader().getResourceAsStream(propertiesFile);
		try {
			properties.load(in);
		} catch (Exception x) {
			throw new AAPropertiesException(x);
		} finally {
			try {in.close();} catch (Exception x) {}
		}		
	}	
	

}
