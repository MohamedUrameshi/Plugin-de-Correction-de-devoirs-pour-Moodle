package fr.up5.miage.utility;

/**
 * Thrown when the folder containing the students' projects or the teacher's repository is empty
 */
public class EmptyFolderException extends Exception{
	
	private static final long serialVersionUID = -5092110166688582356L;

	/**
	 * Constructor of the exception
	 * @param message a String that represents a message for the exception thrown
	 */
	public EmptyFolderException(String message) {
		super(message);
	}

}
