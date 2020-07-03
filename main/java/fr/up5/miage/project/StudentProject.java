package fr.up5.miage.project;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.configuration.NotationConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.sonarqube.SonarServerHttpErrorException;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import fr.up5.miage.sonarqube.SonarqubeWeb;
import fr.up5.miage.utility.UtilCalculGrade;

/**
 * This class represents a student project
 */
public class StudentProject extends Project{

	/**
	 * Represents the logger for this class
	 */
	private static LoggerConfig logStudentProject;

	/**
	 * Initialization of static variable
	 */
	static{
		logStudentProject = new LoggerConfig("StudentProject.class",Level.INFO,false);
		logStudentProject.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * This map stores the unfulfilled rules by the project
	 */
	private HashMap<String,Integer> mapIssues;

	/**
	 * Represents the notation configuration to apply on this ProjectStudent instance
	 */
	private NotationConfiguration notConfigs;

	/**
	 * Represents an instance of TeacherProject with which the grade will be rated
	 */
	private TeacherProject teacherProject;

	/**
	 * A HashMap that contains all results about every quality axis, rules and the final grade
	 */
	private HashMap<String,Float> allGrade;

	/**
	 * Constructor of class. It takes six parameters
	 * @param author is an instance of User that represents the project's author
	 * @param absolutePath is the absolute path of the current student project
	 * @param sonar is an instance of SonarqubeWeb that will be used to exchange with the Sonarqube server
	 * @param teacherProject is the teacher's project concerned
	 * @param notConfigs represents all configurations about the grading
	 */
	public StudentProject(User author, String absolutePath, SonarqubeWeb sonar, TeacherProject teacherProject, NotationConfiguration notConfigs){
		super(author, absolutePath, sonar);
		this.teacherProject = teacherProject;
		this.notConfigs = notConfigs;
	}


	/**
	 * Getter that returns all grade of the project
	 * @return a HashMap that contains all grades
	 */
	public HashMap<String,Float> getAllGrade(){
		return this.allGrade;
	}


	/**
	 * This method gets all results about all quality axis and rules, and calculates the final grade
	 * @throws IOException if the reading of the notationConfiguration.properties file has a problem
	 * @throws SQLException 
	 * @throws SonarqubeDataBaseException 
	 * @throws ClassNotFoundException 
	 */
	public void calculAllGrade(int IdModule) throws IOException, ClassNotFoundException, SonarqubeDataBaseException, SQLException{
		this.allGrade = UtilCalculGrade.getAllGrades(notConfigs.getQualityAxis(), this.mapScoreMetrics, teacherProject.getMapScoreMetrics(), notConfigs.getRulesAndValues(), this.mapIssues,this.getAuthor().getProjectName(),IdModule);
		logStudentProject.log("StudentProject.class",null,null,Level.INFO,(Message)new SimpleMessage(this.author.getProjectName()+" project results caculated."),null);
		logStudentProject.log("StudentProject.class",null,null,Level.INFO,(Message)new SimpleMessage(this.author.getProjectName()+" project results: "+this.mapScoreMetrics.toString()),null);
		logStudentProject.log("StudentProject.class",null,null,Level.INFO,(Message)new SimpleMessage("Rules not respected by "+this.author.getProjectName()+ " project: "+this.mapIssues.toString()),null);
		logStudentProject.log("StudentProject.class",null,null,Level.INFO,(Message)new SimpleMessage("All result for "+this.author.getProjectName()+ " project: "+this.allGrade.toString()),null);
	}


	/**
	 * This method obtains all not respected rules
	 * @throws SonarServerHttpErrorException if a HTTP error occurs
	 * @throws IOException if the reading of input stream has a problem
	 */
	public void obtainMapIssues() throws SonarServerHttpErrorException, IOException{
		this.mapIssues = sonar.getIssuesProject(this.author.getProjectName());
		logStudentProject.log("StudentProject.class",null,null,Level.INFO,(Message)new SimpleMessage("Recovery of "+this.author.getProjectName()+" project issues\n"+this.mapIssues),null);
	}


	/**
	 * A getter of the attribute mapIssues
	 * @return the attribute mapIssues
	 */
	public HashMap<String,Integer> getMapIssues(){
		return this.mapIssues;
	}

	@Override
	public User getAuthor() {
		return super.getAuthor();
	}
}
