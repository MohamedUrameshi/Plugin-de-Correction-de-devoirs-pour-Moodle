package fr.up5.miage.moodle;

/**
 * This class represents the exception when there is an unknown Object for Moodle database.
 */
public class MoodleDataBaseUnknownObjectException extends Exception{
	/**
	 * Class constructor
	 * @param message : the message to throw
	 */
	public MoodleDataBaseUnknownObjectException(String message){
		super(message);
	}
}
