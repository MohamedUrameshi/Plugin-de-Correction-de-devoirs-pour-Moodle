package fr.up5.miage.sonarqube;

/**
 * This exception class is about HTTP errors.
 */
public class SonarServerHttpErrorException extends SonarServerException{

	/**
	 * Constructor of class that expects one parameter.
	 * @param message a String that represents a message for an exception thrown.
	 */
	public SonarServerHttpErrorException(String message){
		super(message);
	}
}
