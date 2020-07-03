package fr.up5.miage.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.notation.App;

/**
 * This test cases is used to test the SystemConfiguration class
 */
public class SystemConfigurationTest{

	/**
	 * Static attribute that stocks an instance of HashMap that will contain the properties
	 */
	private static HashMap<String,String> mapProperties;

	/**
	 * Static attribute that stocks an instance of File class for the systemConfigurationTest.properties file
	 */
	private static File file;


	/**
	 * Before the first test this method initializes a file on which the tests will apply
	 * @throws IOException if the initialization has a problem
	 */
	@BeforeClass
	public static void initFileForTest() throws IOException{
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
		
		file = new File("systemConfigurationTest.properties");
		mapProperties = new HashMap<String,String>();
		try (FileWriter out = new FileWriter(file)){
			out.write("serverSonarUrl=http://serverSonarUrl:8080\npathData=/etc/data\npathTemporaryFolder=/temp/");
		}
	}


	/**
	 * Test if all the properties are properly recovered
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void systemConfigurationTest() throws IOException{
		mapProperties.put("passwordTrustStore", " ");
		mapProperties.put("pathData", "/etc/data");
		mapProperties.put("pathTemporaryFolder", "/temp/");
		mapProperties.put("serverSonarUrl", "http://serverSonarUrl:8080");
		Assert.assertEquals(new SystemConfiguration("systemConfigurationTest.properties").getSystemConfigs(), mapProperties);
	}


	/**
	 * Test if an IOException is thrown when there is no systemConfiguration file to read
	 * @throws IOException if there is no file to read
	 */
	@Test(expected = IOException.class)
	public void systemConfigurationNoFileTest() throws IOException{
		new SystemConfiguration("aFileThatDoesNotExist");
	}


	/**
	 * After the last tests, the file used for the unit tests is deleted
	 */
	@AfterClass
	public static void deleteFileForTest(){
		if (file.exists()){
			file.delete();
		}
		
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}
}
