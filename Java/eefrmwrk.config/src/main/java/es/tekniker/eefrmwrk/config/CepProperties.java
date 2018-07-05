package es.tekniker.eefrmwrk.config;

import java.io.IOException;

/**
 * @author  Alvaro García
 */
public class CepProperties extends ConfigFile {

	/**
	 * @throws IOException 
	 */
	public CepProperties() throws IOException {
			this("CEP.properties");
	}

	/**
	 * @param filePath
	 * @throws IOException 
	 */
	public CepProperties(String filePath) throws IOException{
		super(filePath);
	}
	
	public static String getProperty(String code){ 
		String prop = getStringParam(code);
		return prop;
	}

}
