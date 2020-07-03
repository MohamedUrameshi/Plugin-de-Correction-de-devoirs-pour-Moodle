package fr.up5.miage.moodle;

/**
 * This class represents the exception when there is a mistake in the access in the Moodle database.
 */
public class MoodleDataBaseAccessException extends Exception{
	/**
	 * Class constructor
	 * @param message : the message to throw
	 */
	public MoodleDataBaseAccessException(String message){
		super(message);
	}
}
