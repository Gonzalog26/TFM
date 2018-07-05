/**
 * 
 */
package es.tekniker.eefrmwrk.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Francisco Javier Díez
 *
 */
public class ConfigFile {

	private static Properties props = new Properties();
	private static String filePath = "service.properties";
	private static Log log =
	    LogFactory.getLog(es.tekniker.eefrmwrk.config.ConfigFile.class);
	
	protected ConfigFile() {
		log.debug("ConfigFile default constructor");

	}
	
	public ConfigFile(String filePath) throws IOException{	
		log.debug("ConfigFile with file constructor");
		setFilePath(filePath);
	}
	
	protected static void setFilePath(String defaultFilePath) throws IOException {
		ConfigFile.filePath = defaultFilePath;
		log.debug("Loading properties for file " + filePath); 

        ClassLoader loader = ConfigFile.class.getClassLoader();
        if(loader==null)
          loader = ClassLoader.getSystemClassLoader();

        java.net.URL url = loader.getResource(filePath);
		props.load(url.openStream());		
		log.debug("Properties loaded"); 
	}

	protected static String getFilePath() {
		return filePath;
	}

	public static String getStringParam(String paramName){
		String value = props.getProperty(paramName);
		log.debug("Param String " + paramName + " read. value: " + value);
		return value;
	}
	
	public static void setStringParam(String paramName,String value){
		props.setProperty(paramName, value);
		log.debug("Param String " + paramName + " writed. value: " + value);
	}

	public long getLongParam(String paramName){
		String value = props.getProperty(paramName);
		log.debug("Param Long " + paramName + " read. value: " + value);
		if ( value == null){
			return 0;
		}
		else {
			return Long.parseLong(value);
		}
	}
	
	public int getIntParam(String paramName){
		String value = props.getProperty(paramName);
		log.debug("Param Integer " + paramName + " read. value: " + value);
		if ( value == null){
			return 0;
		}
		else {
			return Integer.parseInt(value);
		}
	}
	
	public List<String> getListStringParam(String paramName){
		List<String> values = new ArrayList<String>(); 
		boolean ultimo = false;
		int contador = 0;
		while (!ultimo){
			String value = props.getProperty(paramName+contador);
			if (value == null){
				ultimo = true;
			}
			else {
				values.add(value);
				contador++;
			}
		}
		log.debug("Array Param String " + paramName + " read " + contador + " values");
		return values;
	}

	public List<Integer> getListIntegerParam(String paramName){
		List<Integer> values = new ArrayList<Integer>(); 
		boolean ultimo = false;
		int contador = 0;
		while (!ultimo){
			String value = props.getProperty(paramName+contador);
			if (value == null){
				ultimo = true;
			}
			else {
				values.add(Integer.parseInt(value));
				contador++;
			}
		}
		log.debug("Array Param String " + paramName + " read " + contador + " values");
		return values;
	}

	public List<Long> getListLongParam(String paramName){
		List<Long> values = new ArrayList<Long>(); 
		boolean ultimo = false;
		int contador = 0;
		while (ultimo){
			String value = props.getProperty(paramName+contador);
			if (value == null){
				ultimo = false;
			}
			else {
				values.add(Long.parseLong(value));
				contador++;
			}
		}
		log.debug("Array Param String " + paramName + " read " + contador + " values");
		return values;
	}

	public HashMap<String, String> getMapStringParam(String paramName){
		HashMap<String, String> values = new HashMap<String, String>(); 
		boolean ultimo = false;
		int contador = 1;
		while (!ultimo){
			String value = props.getProperty(paramName+contador);
			if (value == null){
				ultimo = true;
			}
			else {
				values.put(paramName+contador, value);
				contador++;
			}
		}
		log.debug("Map Param String " + paramName + " read " + contador + " values");
		return values;
	}


}
