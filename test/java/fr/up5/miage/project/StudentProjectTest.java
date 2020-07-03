package fr.up5.miage.project;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.BeforeClass;

import fr.up5.miage.configuration.NotationConfiguration;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.sonarqube.SonarqubeWeb;

public class StudentProjectTest {

	
	private static User author;
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
	private static TeacherProject teacherProject;

	/**
	 * A HashMap that contains all results about every quality axis, rules and the final grade
	 */
	private HashMap<String,Float> allGrade;
	
	private static SonarqubeWeb sonar;
	
	private static SystemConfiguration systemConfiguration ;
	
	
	@BeforeClass
	public static void init() throws IOException{
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
		author=new User("ndione");
		sonar=new SonarqubeWeb(systemConfiguration);
		teacherProject =new TeacherProject(new User(""), "j", sonar);
		
		//systemConfiguration=new SystemConfiguration(System.getProperty("user.dir")+File.separator+"configurationSystem.properties");
		//StudentProject(User author, String absolutePath, SonarqubeWeb sonar, TeacherProject teacherProject, NotationConfiguration notConfigs){

	}
}
