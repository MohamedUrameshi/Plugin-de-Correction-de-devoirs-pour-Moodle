package fr.up5.miage.sonarqube;

/**
 * This exception class is about every exception concerning the Sonarqube server.
 */
public class SonarServerException extends Exception{

	/**
	 * Constructor of class that expects one parameter.
	 * @param message a String that represents a message for an exception thrown.
	 */
	public SonarServerException(String message){
		super(message);
	}
}
