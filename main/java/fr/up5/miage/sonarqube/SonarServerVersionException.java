package fr.up5.miage.sonarqube;

/**
 * This exception class represents a problem with the version of the Sonarqube server.
 * It is thrown if the version is not compatible with this program.
 */
public class SonarServerVersionException extends SonarServerException{

	private static final long serialVersionUID = 739226517115647864L;

	/**
	 * Constructor of class that expects one parameter.
	 * @param message a String that represents a message for an exception thrown.
	 */
	public SonarServerVersionException(String message){
		super(message);
	}
}
