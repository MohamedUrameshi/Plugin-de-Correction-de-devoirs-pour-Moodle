/*
 * Links that helped for coding:
 * https://www.mkyong.com/java/how-to-execute-shell-command-from-java/
 * http://codes-sources.commentcamarche.net/faq/10905-lancement-d-un-commande-avec-runtime-exec
 * http://ydisanto.developpez.com/tutoriels/java/runtime-exec/
 * https://www.developpez.net/forums/d278580/java/general-java/debuter/java-recuperer-type-d-o-s/
 */

package fr.up5.miage.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.testsReport.TestRepport;
import fr.up5.miage.sonarqube.QualityProfile;
import fr.up5.miage.sonarqube.SonarServerHttpErrorException;
import fr.up5.miage.sonarqube.SonarqubeWeb;

/**
 * This class represents a project
 */
public class Project{

	/**
	 * Represents the logger for this class
	 */
	private static LoggerConfig logProject;

	/**
	 * Class attribute that represents the os name of the host operating system
	 */
	private static String osName;

	/**
	 * Initialization of static variables
	 */
	static{
		logProject = new LoggerConfig("Project.class",Level.INFO,false);
		logProject.addAppender(App.fileLog, Level.INFO, null);
		osName = System.getProperty("os.name");
	}

	/**
	 * Attribute which represents the project's author
	 */
	protected User author;

	/**
	 * This is a String of the absolute path of the project
	 */
	protected String absolutePath;

	/**
	 * This map stocks the metrics's score grading by the Sonarqube server
	 */
	protected HashMap<String,Float> mapScoreMetrics;

	/**
	 * This attribute stocks an instance of SonarqueWeb that will be used to communicate with the Sonarqube server
	 */
	protected SonarqubeWeb sonar;

	protected TestRepport testRepport;

	/**
	 * Constructor of class. It takes four parameters
	 * @param author is an instance of User that represents the project's author
	 * @param absolutePath is the absolute path of the project
	 * @param sonar is an instance of SonarqubeWeb that serves to exchange with the Sonarqube server
	 */
	public Project (User author, String absolutePath, SonarqubeWeb sonar){
		this.author = author;
		this.absolutePath = absolutePath;
		this.sonar = sonar;
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Creation of the project "+this.author.getProjectName()),null);
		this.testRepport = new TestRepport();
		//rename the name in pom file for for sonarqube

	}
	public Project(HashMap mapScoreMetrics)
	{
		this.mapScoreMetrics = mapScoreMetrics;
	}
	/**
	 * This method launches a "sonar analysis" of the project thanks to Maven goal "sonar:sonar" and launches a mvn test to rebuild the project before the Sonarqube analysis
	 * @param systemConfiguration is an instance of SystemConfiguration that will be used to configure the analysis launch
	 * @param qualityProfile represents the sonar quality profile that will analyze the project
	 * @return a boolean, true if the launch is a success, false if it fails
	 * @throws IOException if the reading of text result has encountered a problem
	 * @throws LaunchAnalysisException if the analysis launch has a problem
	 */
	public boolean launchAnalysis( SystemConfiguration systemConfiguration, QualityProfile qualityProfile) throws IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, ClassNotFoundException, InterruptedException {

		String mvnTest = systemConfiguration.getSystemConfigs().get("mavenBinFolder")+"mvn test -Dmaven.test.failure.ignore=true -Dmaven.failOnError=false ";

		System.out.println(mvnTest);
		StringBuilder commandMvn = new StringBuilder(mvnTest);
		System.out.println();
		File dir = new File(this.absolutePath);
		String[] command = {"", "", mvnTest};

		if (Project.osName.contains("Windows")){
			command[0] = "cmd.exe";
			command[1] = "/C";
		}
		else{
			command[0] = "/bin/sh";
			command[1] = "-c";
		}

		String qualityProfileName = "";
		if(qualityProfile != null)
			qualityProfileName = qualityProfile.getName().replace(" ","" );



		commandMvn.append("sonar:sonar -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");


		if (systemConfiguration.getSystemConfigs().get("serverSonarUrl").contains("https")){
			commandMvn.append("sonar:sonar -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");
		}
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Launching analysis for "+this.author.getProjectName()+" project"),null);
		
		command[2]=commandMvn.toString();
		String textCommand =  this.executeCommand(command, dir);
		Boolean success = textCommand.contains("BUILD SUCCESS");
		if(textCommand.contains("FAILURE!"))
		{
			this.testRepport.laucnchTestsRapport(textCommand);

		}

		if (!success){
			throw new LaunchAnalysisException("Analyze failed for "+this.author.getProjectName()+ " project, command launch has been: "+command[2]+" and the text result has been: "+textCommand);
		}
		return success;
	}

	/**
	 * This method launches a "sonar analysis" of the project thanks to Maven goal "sonar:sonar" and launches a mvn test to rebuild the project before the Sonarqube analysis
	 * @param systemConfiguration is an instance of SystemConfiguration that will be used to configure the analysis launch
	 * @param qualityProfile represents the sonar quality profile that will analyze the project
	 * @return a boolean, true if the launch is a success, false if it fails
	 * @throws IOException if the reading of text result has encountered a problem
	 * @throws LaunchAnalysisException if the analysis launch has a problem
	 */
	public boolean launchAnalysisForPython( SystemConfiguration systemConfiguration, QualityProfile qualityProfile,File temp, HashMap<String,Float> map) throws IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, ClassNotFoundException, InterruptedException {
		String sonnarScannerBin = systemConfiguration.getSystemConfigs().get("sonarScannerBinFolder");
		StringBuilder commandMvn = new StringBuilder(sonnarScannerBin);
		File dir = new File(this.absolutePath);
		String[] command = {"", "", ""};

		if (Project.osName.contains("Windows")){
			command[0] = "cmd.exe";
			command[1] = "/C";
		}
		else{
			command[0] = "/bin/sh";
			command[1] = "-c";
		}
		String path = temp.getAbsolutePath()+File.separator + this.author.getProjectName();
		System.err.println(path);
		String qualityProfileName = "";
		if(qualityProfile != null)
			qualityProfileName = qualityProfile.getName().replace(" ","" );

		commandMvn.append("sonar-scanner  -Dsonar.tests= -Dsonar.sources="+path +" -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");

		if (systemConfiguration.getSystemConfigs().get("serverSonarUrl").contains("https")){
			commandMvn.append("sonar:sonar -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");
		}
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Launching analysis for "+this.author.getProjectName()+" project"),null);
		command[2]=commandMvn.toString();
		String textCommand =  this.executeCommand(command, dir);
		boolean success = textCommand.contains("EXECUTION SUCCESS");
		if(textCommand.contains("FAILURE!"))
		{
			this.testRepport.laucnchTestsRapport(textCommand);
		}

		if (!success){
			throw new LaunchAnalysisException("Analyze failed for "+this.author.getProjectName()+ " project, command launch has been: "+command[2]+" and the text result has been: "+textCommand +"BBBBBBBBBBBBBB");
		}
		return true;
	}
	
	
	public boolean launchAnalysisForJavaScript( SystemConfiguration systemConfiguration, QualityProfile qualityProfile,File temp, HashMap<String,Float> map) throws IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, ClassNotFoundException, InterruptedException {
		String sonnarScannerBin = systemConfiguration.getSystemConfigs().get("sonarScannerBinFolder"); 
		StringBuilder commandMvn = new StringBuilder(sonnarScannerBin);
		
		File dir = new File(this.absolutePath);
		String[] command = {"", "", ""};
		
		String pathNode=systemConfiguration.getSystemConfigs().get("nodeJsApplication"); 
		if (Project.osName.contains("Windows")){
			command[0] = "cmd.exe";
			command[1] = "/C";
		}
		else{
			command[0] = "/bin/sh";
			command[1] = "-c";
		}
		String path = temp.getAbsolutePath()+File.separator + this.author.getProjectName();
		System.err.println(path);
		String qualityProfileName = "";
		if(qualityProfile != null)
			qualityProfileName = qualityProfile.getName().replace(" ","" );

		commandMvn.append("sonar-scanner  -Dsonar.tests= -Dsonar.sources="+path +  " -Dsonar.nodejs.executable="+pathNode+" -Dsonar.exclusions=node_modules"+" -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");

		if (systemConfiguration.getSystemConfigs().get("serverSonarUrl").contains("https")){
			commandMvn.append("sonar:sonar -Dsonar.login="+systemConfiguration.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+systemConfiguration.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+systemConfiguration.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");
		}
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Launching analysis for "+this.author.getProjectName()+" project"),null);
		command[2]=commandMvn.toString();
		String textCommand =  this.executeCommand(command, dir);
		boolean success = textCommand.contains("EXECUTION SUCCESS");
		if(textCommand.contains("FAILURE!"))
		{
			this.testRepport.laucnchTestsRapport(textCommand);
		}

		if (!success){
			throw new LaunchAnalysisException("Analyze failed for "+this.author.getProjectName()+ " project, command launch has been: "+command[2]+" and the text result has been: "+textCommand +"BBBBBBBBBBBBBB");
		}
		return true;
	}
	
	
	/**
	 * This method launches a "sonar analysis" of the project thanks to Maven goal "sonar:sonar" and launches a mvn test to rebuild the project before the Sonarqube analysis
	 * @param sysConfs is an instance of SystemConfiguration that will be used to configure the analysis launch
	 * @param qualityProfile represents the sonar quality profile that will analyze the project
	 * @return a boolean, true if the launch is a success, false if it fails
	 * @throws IOException if the reading of text result has encountered a problem
	 * @throws LaunchAnalysisException if the analysis launch has a problem
	 */
	public boolean launchAnalysisForPHP( SystemConfiguration sysConfs, QualityProfile qualityProfile,File temp, HashMap<String,Float> map) throws IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, ClassNotFoundException, InterruptedException {

		String sonnarScannerBin = sysConfs.getSystemConfigs().get("sonarScannerBinFolder");
		//StringBuilder commandMvn = new StringBuilder(sonnarScannerBin);
		String path = temp.getAbsolutePath()+File.separator + this.author.getProjectName();
		String phpBinFolder = sysConfs.getSystemConfigs().get("phpBinFolder");
		String phpUnitBinFolder = sysConfs.getSystemConfigs().get("phpUnitBinFolder");
		String phpTest = phpBinFolder+"php " +phpUnitBinFolder +"phpunit " + path;
		StringBuilder commandPHP = new StringBuilder(sonnarScannerBin);
		File dir = new File(path);
		String[] command = {"", "", phpTest};

		if (System.getProperty("os.name").contains("Windows")){
			command[0] = "cmd.exe";
			command[1] = "/C";
		}
		else{
			command[0] = "/bin/sh";
			command[1] = "-c";
		}

		String qualityProfileName = "";
		if(qualityProfile != null)
			qualityProfileName = qualityProfile.getName().replace(" ","" );

		commandPHP.append("sonar-scanner  -Dsonar.tests= -Dsonar.sources="+path +" -Dsonar.login="+sysConfs.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+sysConfs.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+sysConfs.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");

		if (sysConfs.getSystemConfigs().get("serverSonarUrl").contains("https")){
			commandPHP.append("sonar:sonar -Dsonar.login="+sysConfs.getSystemConfigs().get("loginSonar")+" -Dsonar.password="+sysConfs.getSystemConfigs().get("passwordSonar")+" -Dsonar.host.url="+sysConfs.getSystemConfigs().get("serverSonarUrl")+" -Dsonar.projectKey="+this.author.getProjectName()+" -Dsonar.profile="+qualityProfileName+" -Dsonar.scm.disable=true");
		}
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Launching analysis for "+this.author.getProjectName()+" project"),null);
		command[2]=commandPHP.toString();
		String textCommand =  this.executeCommand(command, dir);
		boolean success = textCommand.contains("EXECUTION SUCCESS");
		if(textCommand.contains("FAILURES!") || textCommand.contains("ERRORS!"))
		{
			this.testRepport.laucnchTestsRapport(textCommand);
		}

		if (!success){
			throw new LaunchAnalysisException("Analyze failed for "+this.author.getProjectName()+ " project, command launch has been: "+command[2]+" and the text result has been: "+textCommand +"BBBBBBBBBBBBBB");
		}
		return true;
	}

	
	/**
	 * This method executes a shell command in a specific directory
	 * @param command is the command to execute
	 * @param dir is the directory where the command will be executed
	 * @return a String that represents the console text after the command execution
	 * @throws IOException if the reading of text result has encountered a problem
	 */
	public static String executeCommand(String[] command, File dir) throws IOException{
		StringBuilder output = new StringBuilder();
		String line;

		ProcessBuilder builder = new ProcessBuilder(Arrays.asList(command));
		builder.directory(dir);
		builder.redirectErrorStream(true);
		Process p = builder.start();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		// String line;
		while (true) {
			line = r.readLine();
			if (line == null) { break; }
			output.append(line+System.getProperty("line.separator"));
			System.out.println(line);
		}
		return output.toString();
	}

	/**
	 * This method obtains the score of all metrics of a project
	 * @throws IOException if the reception or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public void obtainScoreMetrics() throws SonarServerHttpErrorException, IOException{
		this.mapScoreMetrics = this.sonar.sendRequestScoreMetrics(this.author.getProjectName());
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Recovery of project "+this.author.getProjectName()+" metrics scores: "+this.mapScoreMetrics),null);
	}
	/**
	 * This method obtains the score of success units tests metric
	 * @param numberOfTestTeacher is a float that represents the number of teacher tests
	 * @throws IOException if the reception or reading has a problem
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 */
	public void obtainScoreSuccessTests(float numberOfTestTeacher) throws SonarServerHttpErrorException, IOException{
		this.mapScoreMetrics.put("test_success_density_teacher", this.sonar.sendRequestScoreSuccessTestMetric(this.author.getProjectName(), numberOfTestTeacher));
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Recovery of project "+this.author.getProjectName()+" metrics test_success_density_teacher scores: "+this.mapScoreMetrics),null);
	}

	/**
	 * This method deletes the project on Sonarqube server
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reception or reading has a problem
	 */
	public void deleteProject() throws SonarServerHttpErrorException, IOException{
		this.sonar.deleteProject(this.author.getProjectName());
		logProject.log("Project.class",null,null,Level.INFO,(Message)new SimpleMessage("Deletion of project "+this.author.getProjectName()),null);
	}

	/**
	 * A getter of the attribute mapScoreMetrics
	 * @return the attribute mapScoreMetrics
	 */
	public HashMap<String,Float> getMapScoreMetrics(){
		return this.mapScoreMetrics;
	}


	/**
	 * A getter of the attribute absolutePath
	 * @return the attribute absolutePath
	 */
	public String getAbsolutePath(){
		return this.absolutePath;
	}

	public User getAuthor() {
		return author;
	}

	public void setTestRepport(TestRepport testRepport) {
		this.testRepport = testRepport;
	}

	public TestRepport getTestRepport() {
		return testRepport;
	}
}
