package fr.up5.miage.project;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import fr.up5.miage.sonarqube.SonarqubeWeb;
import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;


/**
 * This class represents a teacher project
 */
public class TeacherProject extends Project{

	/**
	 * Constructor of class
	 * @param author represents the author of the project
	 * @param absolutePath represents the absolute path of the project
	 * @param sonar is an instance of SonarqubeWeb that will be used to exchange with the Sonarqube server
	 */
	public TeacherProject(User author, String absolutePath, SonarqubeWeb sonar){
		super(author, absolutePath, sonar);
	
	}
	public TeacherProject(HashMap mapMetrics)
	{
		super(mapMetrics);
	}
}
