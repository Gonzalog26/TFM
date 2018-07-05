/**
 * 
 */
package es.tekniker.eefrmwrk.config;

import java.io.IOException;

/**
 * @author  Francisco Javier Díez
 *
 */
public class ExceptionTextFile extends ConfigFile {

	/**
	 * @throws IOException 
	 */
	public ExceptionTextFile() throws IOException {
			this("exceptiontext.properties");
	}

	/**
	 * @param filePath
	 * @throws IOException 
	 */
	public ExceptionTextFile(String filePath) throws IOException{
		super(filePath);
	}
	
	public static String getExceptionMsg(String code){
		String text = getStringParam(code + "_TEXT");
		return text;
	}

}
