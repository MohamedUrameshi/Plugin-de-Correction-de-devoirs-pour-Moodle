package fr.up5.miage.utility;

/**
 * This exception class is about all exception concerning the CSV File
 */
public class BadNameCsvFileException extends Exception{

	private static final long serialVersionUID = 1645371222061717793L;

	/**
	 * Constructor of class that expects one parameter
	 * @param message a String that represents a message for the exception thrown
	 */
	public BadNameCsvFileException(String message){
		super(message);
	}
}
