package fr.up5.miage.sonarqube;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.configuration.ModelValue;
import fr.up5.miage.notation.App;

/**
 * This class represents a quality profile on the Sonarqube server
 */
public class QualityProfile{

	/**
	 * Static attribute that represents the logger for this class
	 */
	private static LoggerConfig logQualityProfile;

	/**
	 * Initialization of static variable
	 */
	static{
		logQualityProfile = new LoggerConfig("QualityProfile.class",Level.INFO,false);
		logQualityProfile.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * This String attribute stocks the name of the profile
	 */
	private String profileName;

	/**
	 * This String attribute stocks the key of the profile
	 */
	private String profileKey;

	/**
	 * Map that contains the whole rules to activate
	 */
	private Set<String> rulesActivated;

	/**
	 * A instance of SonarqubeWeb used to exchange with the Sonarqube server
	 */
	private SonarqubeWeb sonarqubeWeb;


	/**
	 * Constructor of class that expects three parameters. It creates a quality profile
	 * on the Sonarqube server
	 * @param profileName is the name to give at the new quality profile
	 * @param rulesActivated is the HashMap that contains the whole rules to activate
	 * @param sonarqubeWeb is an instance of SonarqubeWeb used to exchange with the Sonarqube server
	 * @throws IOException if the reception or reading has a probleme
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public QualityProfile (String profileName, Map<String,ModelValue> rulesActivated, SonarqubeWeb sonarqubeWeb) throws SonarServerHttpErrorException, IOException{
		this.profileName = profileName;
		this.rulesActivated = rulesActivated.keySet();
		this.sonarqubeWeb = sonarqubeWeb;
		int i = 1;
		while (this.sonarqubeWeb.getIdQualityProfile(this.profileName)){
			this.profileName=this.profileName.substring(0, this.profileName.length()-1)+i;
			i++;
		}
		this.profileKey = this.sonarqubeWeb.createProfile(this.profileName);
		logQualityProfile.log("QualityProfile.class",null,null,Level.INFO,(Message)new SimpleMessage("Creation of quality profile "+this.profileName+" on Sonarqube server"),null);
	}


	/**
	 * This method activates, on the quality profile concerned, the rules which are contained in the HashMap
	 * @throws IOException if the reception or reading has a problem
	 */
	public void configurationRules() throws IOException{
		logQualityProfile.log("QualityProfile.class",null,null,Level.INFO,(Message)new SimpleMessage("Beginning of configuration of quality profile "+this.profileName),null);
		for (String ruleId : this.rulesActivated){
			try{
				this.sonarqubeWeb.activateRule(ruleId, this.profileKey);
				logQualityProfile.log("QualityProfile.class",null,null,Level.INFO,(Message)new SimpleMessage("Enable the "+ruleId+" rule on "+this.profileName+" quality profile"),null);
			}
			catch (SonarServerHttpErrorException e){
				logQualityProfile.log("QualityProfile.class",null,null,Level.WARN,(Message)new SimpleMessage("Enable the "+ruleId+" rule on "+this.profileName+" quality profile encountered a problem: "+e.getMessage()),null);
			}
		}
	}


	/**
	 * Getter of the attribute profileName
	 * @return a String that represents the name of the profile
	 */
	public String getName(){
		return this.profileName;
	}


	/**
	 * Getter of the attribute profileKey
	 * @return a String that represents the key of the profile
	 */
	public String getProfileKey(){
		return this.profileKey;
	}

	/**
	 * This method deletes the quality profile on the Sonarqube server
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception or reading has a problem
	 */
	public void deleteProfile() throws SonarServerHttpErrorException, IOException{
		this.sonarqubeWeb.deleteProfile(this.profileName);
		logQualityProfile.log("QualityProfile.class",null,null,Level.INFO,(Message)new SimpleMessage("Deletion of quality profile "+this.profileName),null);
	}
}

