package fr.up5.miage.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.notation.App;

/**
 * This class will recover datas of system file configuration
 */
public class SystemConfiguration{

	/**
	 * Represents the logger for this class
	 */
	private static LoggerConfig logSysConfs;
	
	/**
	 * Initialization of static variables
	 */
	static{
		logSysConfs = new LoggerConfig("SystemConfiguration.class",Level.INFO,false);
		logSysConfs.addAppender(App.fileLog, Level.INFO, null);
	}
	
	/**
	 * Attribute that stocks the name of the properties and their values
	 */
	private HashMap<String,String> systemConfigs;

	/**
	 * Attribute that serves to recover the properties in the concerned file
	 */
	private Properties properties;
	
	/**
	 * <i>Initialization of all attributes and the inputStream of the file.</i>
	 * @throws IOException 
	 * @throws FileNotFoundException if the file wasn't found
	 */
     public void initializeOfAllAttributesAndInputStream(String pathFile) throws FileNotFoundException, IOException {
    		
 		this.systemConfigs = new HashMap<String,String>();
 		this.properties = new Properties();
 		try(FileInputStream fis = new FileInputStream(pathFile)){
 			properties.load(fis);
 		}
     }
     
     /**
      * <i>Recover the properties of the file.</i>
      */
      public void recoverPropertiesOfFile() {
    		
  		Enumeration<?> enumProperties = properties.propertyNames();
  		while(enumProperties.hasMoreElements()){
  			String nameProperty = (String)enumProperties.nextElement();
  			systemConfigs.put(nameProperty, properties.getProperty(nameProperty));
  			logSysConfs.log("SystemConfiguration.class",null,null,Level.INFO,(Message)new SimpleMessage("Reading "+nameProperty+" in system configuration file"),null);
  		}
      }
     
	/**
	 * Constructor of class that expects one parameter
	 * @param pathFile is the absolute path of the systemConfiguration.properties file
	 * @throws IOException if the reading of systemConfiguration.properties file has a problem
	 */
	public SystemConfiguration(String pathFile) throws IOException{
		
		 initializeOfAllAttributesAndInputStream(pathFile);
		
		
		 recoverPropertiesOfFile();

		
		if (systemConfigs.get("passwordTrustStore")==null){
			systemConfigs.put("passwordTrustStore", " ");
		}
	}


	/**
	 * Getter that returns the HashMap of properties
	 * @return a HashMap that contains the properties
	 */
	public HashMap<String,String> getSystemConfigs(){
		return this.systemConfigs;
	}
}
