package fr.up5.miage.moodle;

import fr.up5.miage.project.User;

/**
 * This class represents a Moodle user
 */
public class MoodleUser extends User{
	
	/**
	 * This attribute stocks the id (in Moodle's database) of the Moodle user
	 */
	private int idUser;

	/**
	 * This attribute stocks the last name of the user
	 */
	private String lastName;
	
	/**
	 * This attribute stocks the user name of the user
	 */
	private String userName;
	
	/**
	 * This attribute stocks the project name of the user
	 */
	private String projectName;
	
	/**
	 * Constructors of class
	 * @param idUser : the identifier of the user
	 * @param lastName the last name of the user
	 * @param projectName : the project of the user (for only one assignment)
	 * @param userName : the login of the user
	 */
	public MoodleUser(int idUser, String userName, String lastName, String projectName) {
		super(projectName);
		this.idUser = idUser;
		this.lastName = lastName;
		this.userName = userName;
		this.projectName = projectName;
	}

	/**
	 * Getter
	 * @return the idUser
	 */
	public int getIdUser(){
		return idUser;
	}

	/**
	 * @param idUser the idUser to set
	 */
	public void setIdUser(int idUser){
		this.idUser = idUser;
	}

	/**
	 * @return the lastName
	 */
	public String getLastName(){
		return lastName;
	}

	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName){
		this.lastName = lastName;
	}

	/**
	 * @return the userName
	 */
	public String getUserName(){
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName){
		this.userName = userName;
	}

	/**
	 * @return the projectName
	 */
	@Override
	public String getProjectName(){
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	@Override
	public void setProjectName(String projectName){
		this.projectName = projectName;
	}

	@Override
	public String toString() {
		return "MoodleUser [idUser=" + idUser + ", lastName=" + lastName + ", userName=" + userName + ", projectName="
				+ projectName + "]";
	}
	
	
}
