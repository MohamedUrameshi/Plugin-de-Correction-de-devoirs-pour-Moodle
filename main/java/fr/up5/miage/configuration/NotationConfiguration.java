package fr.up5.miage.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.notation.App;

/**
 * This class will recover datas of notation configuration file
 */
public class NotationConfiguration{

	/**
	 * Represents the logger for this class
	 */
	private static LoggerConfig logNotConfs;
	
	/**
	 * Class name
	 */
	private static final String notationConfigurationClass = "NotationConfiguration.class";

	/**
	 * Initialization of static variables
	 */
	static{
		logNotConfs = new LoggerConfig(notationConfigurationClass,Level.INFO,false);
		logNotConfs.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * Stores the activated rules and their values
	 */
	private HashMap<String,ModelValue> rulesAndValues;

	/**
	 * Stores the activated quality axis and their values
	 */
	private HashMap<String,ModelValue> qualityAxis;

	/**
	 * Attribute that serves to recover the properties
	 */
	private Properties properties;
	
	/**
	 * <i>Initialization of all attributes and the inputStream of the file</i>
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public ArrayList<String> initializeAllAttributesAndInputStreamOfFile(String pathFile) throws FileNotFoundException, IOException {
		this.rulesAndValues = new HashMap<String,ModelValue>();
		this.qualityAxis = new HashMap<String,ModelValue>();
		this.properties = new Properties();
		try(FileInputStream fis = new FileInputStream(pathFile)){
			properties.load(fis);
		}
		ArrayList<String> listQualityAxis = new ArrayList<String>();
		listQualityAxis.add("TestOfTeacher");
		listQualityAxis.add("Complexity");
		listQualityAxis.add("Comments");
		listQualityAxis.add("TestOfStudent");
		
		return listQualityAxis;
	}
	
	/**
	 * <i>Initialization of rulesAndValues HashMap with checking the syntax of values</i>
	 */
	 public void initializeRulesAndValues(ArrayList<String> listQualityAxis) {
		
			String idElement;
			Enumeration<?> enumProperties = properties.propertyNames();
			while(enumProperties.hasMoreElements()){
				idElement = (String)enumProperties.nextElement();
				if (!listQualityAxis.contains(idElement))
					getPropertyFor(this.properties, idElement, 3, this.rulesAndValues);
			}

	 }
	 
	 /**
	  * <i>Initialization of qualityAxis HashMap with checking the syntax of values</i>
	  */
      public void initializeOfQualityAxis(ArrayList<String> listQualityAxis) {
  		getPropertyFor(this.properties, listQualityAxis.get(0), 1, this.qualityAxis);
  		getPropertyFor(this.properties, listQualityAxis.get(1), 2, this.qualityAxis);
  		getPropertyFor(this.properties, listQualityAxis.get(2), 2, this.qualityAxis);
  		getPropertyFor(this.properties, listQualityAxis.get(3), 2, this.qualityAxis);
      }

	/**
	 * Constructor of the class that expects one parameter. It initializes the two HashMaps attributes
	 * @param pathFile is the path of configurationNotation.properties file
	 * @throws IOException if the reading of notationConfiguration.properties file has a problem
	 */
	public NotationConfiguration(String pathFile) throws IOException{

		
		ArrayList<String> listQualityAxis =initializeAllAttributesAndInputStreamOfFile(pathFile); 

	
		initializeRulesAndValues(listQualityAxis);

		
		initializeOfQualityAxis(listQualityAxis);
	}


	/**
	 * This method recovers a property for a specific name if the syntax of its value is respected
	 * @param name is the name for the property that must be recovered
	 * @param numberDataInSyntax is the number of datas in the value to check the syntax. If it is wrong, the property won't be recovered
	 */
	private static void getPropertyFor(Properties properties, String name, int numberDataInSyntax, HashMap<String,ModelValue> mapDestination){
		String value = properties.getProperty(name);
		ModelValue modV = NotationConfiguration.checkSyntaxData(value, numberDataInSyntax);
		if (!value.isEmpty()){
			if (modV != null){
				mapDestination.put(name, modV);
				logNotConfs.log(notationConfigurationClass,null,null,Level.INFO,(Message)new SimpleMessage("Reading "+name+" in notation configuration file"),null);
			}
			else{
				logNotConfs.log(notationConfigurationClass,null,null,Level.WARN,(Message)new SimpleMessage("Property "+name+" has not been taken into account because its value is not correct: "+value),null);
			}
		}
	}


	/**
	 * This method checks the right syntax of data
	 * @param toCheck is the String that must be checked
	 * @param numberData is the number of data parts presents in the String that must be checked
	 * @return a ModelValue instance if the syntax is respected or null if the syntax is not
	 */
	private static ModelValue checkSyntaxData(String toCheck, int numberData){
		ArrayList<Float> list = new ArrayList<Float>();
		try{
			StringTokenizer to = new StringTokenizer(toCheck, "|");
			//verify if the String toCheck has not "numberData" parts and if numberData is between one and three.
			if (to.countTokens() != numberData){
				return null;
			}
			while(to.hasMoreTokens()){
				Float value = Float.parseFloat(to.nextToken());
				if (value <= 0){ //verify if each part of the String toCheck is < 0.
					return null;
				}
				else{
					list.add(value);
				}
			}
			switch(numberData){
			case 3: return new ModelValue(list.get(0), list.get(1), list.get(2));
			case 2: return new ModelValue(list.get(0), list.get(1));
			default: return new ModelValue(list.get(0));
			}
		}
		catch (Exception e){
			return null;
		}
	}


	/**
	 * Getter of rulesAndValues HashMap attribute
	 * @return the rulesAndValues HashMap that contains rules and their value
	 */
	public HashMap<String,ModelValue> getRulesAndValues(){
		return this.rulesAndValues;
	}	


	/**
	 * Getter of qualityAxis HashMap attribute
	 * @return the qualityAxis HashMap that contains quality axis and their value
	 */
	public HashMap<String,ModelValue> getQualityAxis(){
		return this.qualityAxis;
	}
}
