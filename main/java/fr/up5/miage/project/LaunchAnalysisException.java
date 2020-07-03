package fr.up5.miage.project;

/**
 * This exception is about the launching of analysis for a Maven project.
 */
public class LaunchAnalysisException extends Exception{

	/**
	 * Constructor of class that expects one parameter.
	 * @param message a String that represents a message for an exception thrown.
	 */
	public LaunchAnalysisException(String message){
		super(message);
	}
}
