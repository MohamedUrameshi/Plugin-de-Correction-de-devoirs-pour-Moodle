package fr.up5.miage.utility;

public class WrongPermissionsException extends Exception{

	private static final long serialVersionUID = 1623392347370680307L;

	/**
	 * Constructor of the exception
	 * @param message a String that represents a message for the exception thrown
	 */
	public WrongPermissionsException(String message){
		super(message);
	}

}
