/* 
 * Links that help for coding:

 * http://stackoverflow.com/questions/29887351/java-the-method-encodebase64
 * http://stackoverflow.com/questions/16965484/java-util-concurrentmodificationexception-with-iterator
 * http://stackoverflow.com/questions/10479434/server-returned-http-response-code-401-for-url-https
 * https://www.mkyong.com/java/java-https-client-httpsurlconnection-example/
 */

package fr.up5.miage.sonarqube;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.utility.UtilJSON;

/**
 * This class will exchange with the Sonarqube server
 */
public class SonarqubeWeb{

	/**
	 * Represent the logger for this class
	 */
	private static LoggerConfig logSonar;

	/**
	 * Initialization of static variable
	 */
	
	static{
		logSonar = new LoggerConfig("SonarqubeWeb.class",Level.INFO,false);
		logSonar.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * A map that stocks all APIWEB that the program needs
	 */
	private static HashMap<String,String> apiWeb;

	/**
	 * A constant variable String that contains "https"
	 */
	private static final String HTTPS;

	/**
	 * Bloc called when the class is charged. Initialization of class attributes
	 */
	static{
		HTTPS = "https";
		apiWeb = new HashMap<String,String>();
		apiWeb.put("listIssues", "/api/issues/search");
		apiWeb.put("listMetrics", "/api/measures/component");
		apiWeb.put("createQualityProfile", "/api/qualityprofiles/create");
		apiWeb.put("deleteQualityProfile", "/api/qualityprofiles/delete");
		apiWeb.put("versionServer", "/api/server/version");
		apiWeb.put("activateRule", "/api/qualityprofiles/activate_rule");
		apiWeb.put("idQualityProfile", "/api/qualityprofiles/search");
		apiWeb.put("deleteProject", "/api/projects/delete");
		apiWeb.put("queueAnalysisReports", "/api/analysis_reports/is_queue_empty");	
	}

	/**
	 * Private attribute that stocks the system configuration to use for configure the connection
	 */
	private SystemConfiguration systemConfiguration;

	/**
	 * Attribute that contains the Sonarqube server URL from the system configuration file
	 */
	private String serverSonarUrl;


	/**
	 * Constructor of SonarqubeWeb class. Initialization of instance attributes
	 * @param systemConfiguration is an instance of SystemConfiguration class used to get the datas to configure connection
	 */
	public SonarqubeWeb(SystemConfiguration systemConfiguration){
		this.systemConfiguration = systemConfiguration;
		this.serverSonarUrl = systemConfiguration.getSystemConfigs().get("serverSonarUrl");
	}


	/**
	 * This method sends a request GET with HTTP protocol but without parameter
	 * @param urlString is the URL which is the destination of the request
	 * @return the response of the request in a StringBuilder
	 * @throws IOException if the reception of input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestGetHttp(String urlString) throws SonarServerHttpErrorException, IOException{
		StringBuilder response; 
		URL url = stringToUrl(urlString);
	
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		connection.setRequestMethod("GET");
		logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("Sending: "+urlString),null);

		if (String.valueOf(connection.getResponseCode()).startsWith("2")){
			response = receiveInputStream(connection.getInputStream());
			logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("The HTTP code number "+connection.getResponseCode()+" has been received with as content: "+response.toString()),null);
		}
		else{
			throw new SonarServerHttpErrorException("Problem, reception of code HTTP "+connection.getResponseCode()+" : "+connection.getResponseMessage()+" for "+urlString);
		}
		return response;
	}


	/**
	 * This method sends a request GET with parameter using HTTP protocol
	 * @param urlString is the URL which is the destination of the request
	 * @param urlParameter that composes the URL like "param1=xxx&param2=xxx"
	 * @return the response of the request in a StringBuilder
	 * @throws IOException if the reception of the input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestGetHttp(String urlString, String urlParameter) throws SonarServerHttpErrorException, IOException{
		return this.sendRequestGetHttp(urlString+"?"+urlParameter);
	}


	/**
	 * This method sends a request POST with HTTP protocol
	 * @param urlString is the URL which is the destination of the request
	 * @param loginPasswordEncoded is the login and password encoded already in BASE64 with form "login:password"
	 * @param requestEncoded is the request to send encoded already in UTF-8
	 * @return the response of the request in a StringBuilder
	 * @throws IOException if the reception or reading has a problem
	 * @throws UnsupportedEncodingException if the encoding is not supported
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestPostHttp(String urlString, String loginPasswordEncoded, String requestEncoded) throws SonarServerHttpErrorException, IOException{
		URL url = stringToUrl(urlString);
		StringBuilder response; 
		
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
	
		connection.setRequestMethod("POST");
		
		setRequestProperty(loginPasswordEncoded,connection);

		logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("Sending: "+urlString+" with request: "+requestEncoded),null);
		try (DataOutputStream output = new DataOutputStream(connection.getOutputStream())){  
			output.writeBytes(requestEncoded); 
		}
		if (String.valueOf(connection.getResponseCode()).startsWith("2")){
			response = receiveInputStream(connection.getInputStream());
			logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("The HTTP code number "+connection.getResponseCode()+" has been received with as content: "+response.toString()),null);
		}
		else{
			throw new SonarServerHttpErrorException("Problem reception code HTTP "+connection.getResponseCode()+" : "+connection.getResponseMessage()+" for "+urlString+" and request "+urlDecode(requestEncoded));
		}
		return response;
	}


	/**
	 * This method sends a request GET with HTTPS protocol but without parameter
	 * @param urlString is the URL which is the destination of the request
	 * @return the response of the request in a StringBuilder
	 * @throws IOException if the reception of input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestGetHttps(String urlString) throws SonarServerHttpErrorException, IOException{
		URL url = stringToUrl(urlString);
		StringBuilder response;
		
		
		
		HttpsURLConnection secureConnection = (HttpsURLConnection)url.openConnection();
	
		secureConnection.setRequestMethod("GET");

		logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("Sending: "+urlString),null);
		if (String.valueOf(secureConnection.getResponseCode()).startsWith("2")){
			response = receiveInputStream(secureConnection.getInputStream());
			logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("The HTTP code number "+secureConnection.getResponseCode()+" has been received with as content: "+response.toString()),null);
		}
		else{
			throw new SonarServerHttpErrorException("Problem reception code HTTP "+secureConnection.getResponseCode()+" : "+secureConnection.getResponseMessage()+" for "+urlString);
		}
		return response;
	}


	/**
	 * This method sends a request GET with parameter and HTTPS protocol
	 * @param urlString is the URL which is the destination of the request
	 * @param urlParameter which composes the URL
	 * @return the response of the request in a Stringbuilder
	 * @throws IOException if the reception or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestGetHttps(String urlString, String urlParameter) throws SonarServerHttpErrorException, IOException{
		return this.sendRequestGetHttps(urlString+"?"+urlParameter);
	}
	
	/**
	 * <i>Method setRequestProperty: Sets the general request property(http).</i>
	 */
	 public void setRequestProperty(String loginPasswordEncoded,HttpURLConnection connexion) {
		 connexion.setRequestProperty("Authorization", "Basic "+ loginPasswordEncoded);
		 connexion.setDoOutput(true);
		 connexion.setDoInput(true);
	 }

	/**
	 * <i>Method setRequestProperty: Sets the general request property(https).</i>
	 */
	 public void setRequestPropertyS(String loginPasswordEncoded,HttpsURLConnection secureConnection) {
		 secureConnection.setRequestProperty("Authorization", "Basic "+ loginPasswordEncoded);
		 secureConnection.setDoOutput(true);
		 secureConnection.setDoInput(true);
	 }

	/**
	 * This method sends a request POST with HTTPS protocol
	 * @param urlString is the URL which is the destination of the request
	 * @param loginPasswordEncoded are the login and the password with form "login:password" encoded already in BASE64
	 * @param requestEncoded is the request to send encoded already in UTF-8
	 * @return the response of the request in a Stringbuilder
	 * @throws IOException if the reception or reading has a problem
	 * @throws UnsupportedEncodingException if the encoding is not supported
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	private StringBuilder sendRequestPostHttps(String urlString, String loginPasswordEncoded, String requestEncoded) throws SonarServerHttpErrorException, IOException{
		URL url = stringToUrl(urlString);
		StringBuilder response; //variable that will stock sonar's response.
		//Opening the connection.
		HttpsURLConnection secureConnection = (HttpsURLConnection)url.openConnection();
	
		secureConnection.setRequestMethod("POST");

		
		
		 setRequestPropertyS(loginPasswordEncoded,secureConnection) ;

		logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("Sending: "+urlString+" with request: "+requestEncoded),null);
		try (DataOutputStream output = new DataOutputStream(secureConnection.getOutputStream())){  
			output.writeBytes(requestEncoded); 
		}
		if (String.valueOf(secureConnection.getResponseCode()).startsWith("2")){
			response = receiveInputStream(secureConnection.getInputStream());
			logSonar.log("SonarqubeWeb.class",null,null,Level.INFO,(Message)new SimpleMessage("The HTTP code number "+secureConnection.getResponseCode()+" has been received with as content: "+response.toString()),null);
		}
		else{
			throw new SonarServerHttpErrorException("Receive code HTTP:"+ secureConnection .getResponseMessage()+" for "+urlString+" with request "+urlDecode(requestEncoded));
		}
		return response;
	}


	/**
	 * This method asks to the Sonarqube server the score of four metrics for a project, 
	 * the concerned metrics are complexity, comment line density, number of unit test, success of units tests
	 * @param projectName is the name of concerned project
	 * @return a HashMap that contains the metrics names and their values
	 * @throws IOException if the reception input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public HashMap<String,Float> sendRequestScoreMetrics(String projectName) throws SonarServerHttpErrorException, IOException{
		String parameters = "componentKey="+projectName+"&metricKeys=comment_lines_density,complexity,tests,test_success_density";
		if (serverSonarUrl.contains(HTTPS)){
			return UtilJSON.getValueOfAllMetrics(this.sendRequestGetHttps(serverSonarUrl+apiWeb.get("listMetrics"), parameters));
		}
		else{
			return UtilJSON.getValueOfAllMetrics(this.sendRequestGetHttp(serverSonarUrl+apiWeb.get("listMetrics"), parameters));
		}
	}


	/**
	 * This method asks to the Sonarqube server the score of the success of units tests
	 * @param projectName is the name of the concerned project
	 * @param numberOfTestTeacher is the number of tests submitted by the teacher
	 * @return a Float that represents the success units tests value, compared to the number of tests submitted by the teacher
	 * @throws IOException if the reception input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public Float sendRequestScoreSuccessTestMetric(String projectName, float numberOfTestTeacher) throws SonarServerHttpErrorException, IOException{
		HashMap<String,Float> mapResult;
		String parameters = "componentKey="+projectName+"&metricKeys=tests,test_success_density";
		if (serverSonarUrl.contains(HTTPS)){
			mapResult = UtilJSON.getValueOfAllMetrics(this.sendRequestGetHttps(serverSonarUrl+apiWeb.get("listMetrics"), parameters));
		}
		else{
			mapResult = UtilJSON.getValueOfAllMetrics(this.sendRequestGetHttp(serverSonarUrl+apiWeb.get("listMetrics"), parameters));
		}
		try{
			return (mapResult.get("tests")*(mapResult.get("test_success_density")/100))/numberOfTestTeacher;
		}
		catch (NullPointerException e){
			return 0f;
		}
	}


	/**
	 * This method creates a quality profile on the Sonarqube server and returns the id of new quality profile
	 * @param profileName is the name of quality profile to create
	 * @return a String that represents the id of the new quality profile
	 * @throws IOException if the reception of input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public String createProfile(String profileName) throws SonarServerHttpErrorException, IOException{
		String requestEncoded = urlEncodeParameter("profileName="+profileName)+"&"+urlEncodeParameter("language=java");
		if (serverSonarUrl.contains(HTTPS)){
			return UtilJSON.getIdQualityProfile(this.sendRequestPostHttps(serverSonarUrl+apiWeb.get("createQualityProfile"), this.getLoginPasswordSonarEncoded(), requestEncoded));
		}
		else{
			return UtilJSON.getIdQualityProfile(this.sendRequestPostHttp(serverSonarUrl+apiWeb.get("createQualityProfile"), this.getLoginPasswordSonarEncoded(), requestEncoded));
		}
	}


	/**
	 * This method deletes a quality profile on the Sonarqube server
	 * @param profileName is the name of quality profile to delete
	 * @return a StringBuilder that represents the response of the Sonarqube server
	 * @throws IOException if the reception of input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public StringBuilder deleteProfile(String profileName) throws SonarServerHttpErrorException, IOException {
		String requestEncoded = urlEncodeParameter("profileName="+profileName)+"&"+urlEncodeParameter("language=java");
		if (serverSonarUrl.contains(HTTPS)){
			return this.sendRequestPostHttps(serverSonarUrl+apiWeb.get("deleteQualityProfile"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
		else{
			return this.sendRequestPostHttp(serverSonarUrl+apiWeb.get("deleteQualityProfile"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
	}


	/**
	 * This class method turns a String into a URL object
	 * @param stringUrl is the String to modify
	 * @return an instance of URL class
	 * @throws MalformedURLException if the URL is malformed
	 */
	private static URL stringToUrl(String stringUrl) throws MalformedURLException{
		return new URL(stringUrl);
	}


	/**
	 * This class method returns a String encoded in BASE64
	 * @param stringToEncode the String that will be encoded
	 * @return the String encoded
	 */
	private static String encodeBase64(String stringToEncode){
		return Base64.getEncoder().encodeToString(stringToEncode.getBytes());	
	}


	/**
	 * This method collects the login and password of system configuration file and encodes them in BASE64
	 * @return the encoded of "login:password" in String
	 */
	private String getLoginPasswordSonarEncoded(){
		
		String loginPassword = systemConfiguration.getSystemConfigs().get("loginSonar")+":"+systemConfiguration.getSystemConfigs().get("passwordSonar"); 
	
		return encodeBase64(loginPassword);	
	}


	/**
	 * This method activates a rule on a profile on the Sonarqube server
	 * @param idRule is the rule ID that must be activate
	 * @param profileKey is the profile of concerned quality
	 * @return a StringBuilder that represents the response of the Sonarqube server
	 * @throws IOException if the reception of input stream or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public StringBuilder activateRule(String idRule, String profileKey) throws SonarServerHttpErrorException, IOException{
		String requestEncoded = urlEncodeParameter("profile_key="+profileKey)+"&"+urlEncodeParameter("rule_key="+idRule);
		if (serverSonarUrl.contains(HTTPS)){
			return this.sendRequestPostHttps(serverSonarUrl+apiWeb.get("activateRule"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
		else{
			return this.sendRequestPostHttp(serverSonarUrl+apiWeb.get("activateRule"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
	}


	/**
	 * This method asks to the version of the Sonarqube server
	 * @return a StringBuilder that represents the response of the Sonarqube server
	 * @throws SonarServerVersionException instantiate if Sonar server is not in the version 6.3
	 * @throws IOException if the reception or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public StringBuilder getVersion() throws SonarServerException, IOException{
		StringBuilder version;
		if (serverSonarUrl.contains(HTTPS)){
			version = this.sendRequestGetHttps(serverSonarUrl+apiWeb.get("versionServer"));
		}
		else{
			version = this.sendRequestGetHttp(serverSonarUrl+apiWeb.get("versionServer"));
		}
		/* TODO Required to rewrite the matching tests in SonarqubeWebTest
		 * if (!version.toString().substring(0, 3).equals("6.3")){
			throw new SonarServerVersionException("The Sonarqube server version is not compatible with the program");
		} */
		return version;
	}


	/**
	 * This method asks to the Sonarqube server if it is analyzing a report
	 * @return a String that can be "true" or "false"
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception of input stream or reading has a problem
	 */
	public String getQueueAnalysisReports() throws SonarServerHttpErrorException, IOException{
		if (serverSonarUrl.contains(HTTPS)){
			return this.sendRequestGetHttps(serverSonarUrl+apiWeb.get("queueAnalysisReports")).toString();
		}
		else{
			return this.sendRequestGetHttp(serverSonarUrl+apiWeb.get("queueAnalysisReports")).toString();
		}
	}


	/**
	 * This method deletes a project on the Sonarqube server
	 * @param projectKey is the key of the project
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception of input stream or reading has a problem
	 */
	public void deleteProject(String projectKey) throws SonarServerHttpErrorException, IOException{
		String requestEncoded = urlEncodeParameter("key="+projectKey);
		if (serverSonarUrl.contains(HTTPS)){
			this.sendRequestPostHttps(serverSonarUrl+apiWeb.get("deleteProject"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
		else{
			this.sendRequestPostHttp(serverSonarUrl+apiWeb.get("deleteProject"), this.getLoginPasswordSonarEncoded(), requestEncoded);
		}
	}


	/**
	 * This class method encodes a String in UTF-8
	 * @param toEncode is the String that have to be encoded
	 * @return an encoded String in UTF-8
	 * @throws UnsupportedEncodingException if the encoded character is not supported
	 */
	private static String urlEncode(String toEncode) throws UnsupportedEncodingException{
		return URLEncoder.encode(toEncode, "UTF-8");
	}


	/**
	 * This class method encodes a String like parameter "xxxxx=xxxxx" in UTF-8
	 * @param parameter is a String that have to be encoded
	 * @return an encoded String in UTF-8
	 * @throws UnsupportedEncodingException if the encoded character is not supported
	 */
	private static String urlEncodeParameter(String parameter) throws UnsupportedEncodingException{
		int equal = parameter.indexOf("=");
		return urlEncode(parameter.substring(0, equal))+"="+urlEncode(parameter.substring(equal+1));
	}


	/**
	 * This class method decodes a String from UTF-8 encoding
	 * @param toDecode is the String that have to be decoded
	 * @return an encoded String from UTF-8
	 * @throws UnsupportedEncodingException if the encoded character is not supported
	 */
	private static String urlDecode(String toDecode) throws UnsupportedEncodingException{
		return URLDecoder.decode(toDecode, "UTF-8");
	}


	/**
	 * This method recovers the data of an input stream
	 * @param input the input stream
	 * @return a StringBuilder that represents the data
	 * @throws IOException if the reception or reading has a problem
	 */
	private static StringBuilder receiveInputStream(InputStream input) throws IOException{
		StringBuilder response = new StringBuilder();
		int i;
		while((i = input.read())!= -1){//stock the inputstream
			response.append((char)i);  
		}
		return response;
	}
 
	/**
	 * <i>Create Hashmap from the list,  with in key field the idRules and in</i>
	 * <i>value field the number of time that the rule is not respected.</i>
	 */
	  public void rouleTest(Iterator<String> it,HashMap<String,Integer> mapIssuesProject, String idRule) {
			while (it.hasNext()){
				idRule = it.next();
				if (mapIssuesProject.containsKey(idRule)){
					mapIssuesProject.put(idRule, mapIssuesProject.get(idRule)+1);
				}
				else{
					mapIssuesProject.put(idRule, new Integer(1));
				}
			}
		   }
	/**
	 * This method recovers all issues of one project
	 * @param projectName is the name of the concerned project
	 * @return a HashMap with in key field the idRules and in value field the number of time that the rule is not respected
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception of stream or reading has a problem
	 */
	public HashMap<String,Integer> getIssuesProject(String projectName) throws SonarServerHttpErrorException, IOException{
		HashMap<String,Integer> mapIssuesProject = new HashMap<String,Integer>();
		Iterator<String> it;
		String idRule="";
		//Recovers an iterator from a list containing all the outputs of a project.
		if (serverSonarUrl.contains(HTTPS)){
			it = UtilJSON.getValueOfAllSpecificField(this.sendRequestGetHttps(serverSonarUrl.concat(apiWeb.get("listIssues")), "componentKeys=".concat(projectName)+"&language=java&resolved=false&ps=500"), "rule").iterator();
		}
		else{
			it = UtilJSON.getValueOfAllSpecificField(this.sendRequestGetHttp(serverSonarUrl.concat(apiWeb.get("listIssues")), "componentKeys=".concat(projectName)+"&language=java&resolved=false&ps=500"), "rule").iterator();
		}
	    rouleTest( it,mapIssuesProject,	idRule);
		return mapIssuesProject;
	}


	/**
	 * This method checks if a quality profile is or is not already present on the Sonarqube server
	 * @param profileName is the name of the quality profile to check
	 * @return true if it is already present and false if it is not
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception of the stream or reading has a problem
	 */
	public boolean getIdQualityProfile(String profileName) throws SonarServerHttpErrorException, IOException{
		String parameters = "language=java";
		if (serverSonarUrl.contains(HTTPS)){
			return this.sendRequestGetHttps(serverSonarUrl.concat(apiWeb.get("idQualityProfile")), parameters).toString().contains(profileName);
		}
		else{
			return this.sendRequestGetHttp(serverSonarUrl.concat(apiWeb.get("idQualityProfile")), parameters).toString().contains(profileName);
		}
	}
}