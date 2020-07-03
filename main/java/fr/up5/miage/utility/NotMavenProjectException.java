package fr.up5.miage.utility;

/**
 * In case the project is a maven project
 */
public class NotMavenProjectException extends Exception{
	
	private static final long serialVersionUID = 3112299564396523052L;

	/**
	 * Constructor of the exception
	 * @param message a String that represents a message for the exception thrown
	 */
	public NotMavenProjectException(String message){
		super(message);
	}

}
