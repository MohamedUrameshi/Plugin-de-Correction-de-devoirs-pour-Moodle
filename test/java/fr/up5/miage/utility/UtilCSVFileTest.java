/*Links that helped for coding:
 * http://opencsv.sourceforge.net/apidocs/com/opencsv/CSVReader.html
 * http://howtodoinjava.com/3rd-party/parse-read-write-csv-files-opencsv-tutorial/
 */

package fr.up5.miage.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.notation.App;

/**
 * This class of tests is used to test the CSVFile class
 */
public class UtilCSVFileTest {

	/**
	 * Class attribute that represents a CSV file
	 */
	private static UtilCSVFile fileCSV;

	/**
	 * Attribute that represents the folder where the CSV file will be created
	 */
	String homeUserDirectory = System.getProperty("user.home");

	/**
	 * to complete
	 */
	String fileName = "Resultat.csv";

	/**
	 * Class attribute that represents a HashSet that contains the parameters for the header
	 */
	private static HashSet<String> hashSetHeader;

	/**
	 * Before all tests
	 */
	@BeforeClass
	public static void initLog(){
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
	}

	/**
	 * Initialize all attributes and create the CSV file
	 * @throws IOException if the creation of the file has a problem
	 * @throws BadNameCsvFileException if the fileName is not long enough or is not well written
	 */
	@Before
	public void init() throws IOException, BadNameCsvFileException{
		hashSetHeader = new HashSet<String>();
		hashSetHeader.add("Complexity");
		fileCSV = new UtilCSVFile(".", fileName, hashSetHeader);
	}


	/**
	 * Test when we create a new CSV file that the header is the one we expect
	 * @throws IOException if the creation of the file has a problem
	 * @throws BadNameCsvFileException if the fileName is not long enough or is not well written
	 */
	@Test
	public void CSVInitializationTest() throws IOException, BadNameCsvFileException{
		//The String that represents the String in the CSV file.
		String header;
		//The String that the header of the file must return when we create a CSV file.
		String headerTest = "\"Name of Project\",\"Complexity\",\"Rules\",\"Final Grade\"";
		//The reader of the CSV file.
		try (BufferedReader brTest = new BufferedReader(new FileReader(fileName))){
			//The String which receives the header of the CSV file.
			header = brTest.readLine();
		}
		Assert.assertEquals(header, headerTest);
	}
	
	/**
	 * Test when we write a new line that all expected parameters are there
	 * @throws IOException if the creation of the file has a problem
	 */
	@Test
	public void writeLineTest() throws IOException{
		HashMap<String,Float> map = new HashMap<String,Float>();
		//The String that represents the String in the CSV file.
		String header;
		
		//Filling the map.
		map.put("Complexity", 2f);
		map.put("Rules", 5f);
		map.put("FinalGrade", 13f);
		fileCSV.writeLine(map, "projectTest");
		try (BufferedReader brTest = new BufferedReader(new FileReader(fileName))){
			//The String which receives the header of the CSV file.
			brTest.readLine();
			header = brTest.readLine();
		}
		Assert.assertEquals(header, "\"projectTest\",\"2\",\"5\",\"13\"");
	}

	/**
	 * Test when we give a bad file name that an expection is thrown
	 * @throws IOException if the creation of the file has a problem
	 * @throws BadNameCsvFileException if the fileName is not long enough or is not well written
	 */
	@Test(expected=BadNameCsvFileException.class)
	public void createCsvFileWithBadNameTest() throws IOException, BadNameCsvFileException{
		new UtilCSVFile(".", "Resultat", hashSetHeader);
	}
	
	/**
	 * Test that there is only the project name on the line if there is a probleme with
	 * the line which must be written
	 * @throws IOException if the creation of the file has a problem
	 */
	@Test
	public void writeLineEmptyMapTest() throws IOException{
		//The String that represents the String in the CSV file.
		String header;

		fileCSV.writeLine(null, "projectTest");
		try (BufferedReader brTest = new BufferedReader(new FileReader(fileName))){
			//The String which receives the header of the CSV file.
			brTest.readLine();
			header = brTest.readLine();
		}
		Assert.assertEquals(header, "\"projectTest\"");
	}
	
	
	/**
	 * Delete the CSV file after all tests are over
	 */
	@After
	public void finish(){
		File file = new File(fileName);
		if(file.exists()){
			file.delete();
		}
	}
	
	/**
	 * After all tests
	 */
	@AfterClass
	public static void deletaionFile(){
		
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}	
}
