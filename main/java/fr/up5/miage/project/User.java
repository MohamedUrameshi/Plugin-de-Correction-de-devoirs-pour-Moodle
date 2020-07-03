package fr.up5.miage.project;

/**
 * This class represents an author of a project
 */
public class User{

	/**
	 * An attribute that stocks the name of the project
	 */
	private String projectName;

	/**
	 * Constructor of class
	 * @param projectName a String that represents the name of the project
	 */
	public User(String projectName){
		this.projectName = projectName;
	}

	/**
	 * Getter for the attribute projectName
	 * @return the projectName
	 */
	public String getProjectName(){
		return projectName;
	}

	/**
	 * A setter for the attribute projectName
	 * @param projectName the projectName to set
	 */
	public void setProjectName(String projectName){
		this.projectName = projectName;
	}
}
