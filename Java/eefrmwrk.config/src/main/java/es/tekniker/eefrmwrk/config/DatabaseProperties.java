
	package es.tekniker.eefrmwrk.config;

	import java.io.IOException;

	/**
	 * @author  Alvaro García
	 */
	public class DatabaseProperties extends ConfigFile {

		/**
		 * @throws IOException 
		 */
		public DatabaseProperties() throws IOException {
				//super("database.properties");
				this("database.properties");
		}

		/**
		 * @param filePath
		 * @throws IOException 
		 */
		public DatabaseProperties(String filePath) throws IOException{
			super(filePath);
		}
		
		static DatabaseProperties dbProp;
		private static void loadProperties(){
			if(dbProp==null){
				try { 
					dbProp=new DatabaseProperties();
					} catch (IOException e) {}
			}	
		}

		
		public static String getProperty(String code){ 
			loadProperties();
			String prop = getStringParam(code);
			return prop;
		}
		
		public static void setProperty(String code,String value){ 
			loadProperties();
			setStringParam(code,value);	
		}

	}
