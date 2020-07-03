package fr.up5.miage.configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.notation.App;

/**
 * This test cases is used to test the NotationConfiguration class
 */
public class NotationConfigurationTest{

	/**
	 * Static attribute that stocks an instance of File for the notationConfigurationTest.properties file
	 */
	private static File file;
	
	/**
	 * Static attribute that stocks an instance of NotationConfiguration for the notationConfigurationTest.properties file
	 */
	private static NotationConfiguration notConfs;
	
	/**
	 * Before the first test this method initializes a file on which the tests will apply
	 * @throws IOException if the file initialization has a problem
	 */
	@BeforeClass
	public static void initFileForTest() throws IOException{
		
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
		
		file = new File("notationConfigurationTest.properties");
		try (FileWriter out = new FileWriter(file)){
			out.write("TestOfTeacher=50\nTestOfStudent=3|4\nComments=4\nComplexity=\n"
					+ "common-java\\:FailedUnitTests=|7|7|7|7\nsquid\\:S2095=5|7|8\nsquid\\:S2094=\n"
					+ "squid\\:S2096=5|7\nsquid\\:S2333=5|-7|8\nsquid\\:S2666=5|2|0\nsquid\\:S2999=2|2.5|word\n");
		}
		notConfs = new NotationConfiguration("notationConfigurationTest.properties");
	}

	/**
	 * Test if all rules with a good syntax value have been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesGoodTest() throws IOException{
		Assert.assertEquals(notConfs.getRulesAndValues().size(), 1);
		Assert.assertEquals((notConfs.getRulesAndValues().get("squid:S2095")).getFirstValue(), new Float (5));
		Assert.assertEquals(notConfs.getRulesAndValues().get("squid:S2095").getSecondValue(), new Float (7));
		Assert.assertEquals(notConfs.getRulesAndValues().get("squid:S2095").getThirdValue(), new Float (8));
	}

	/**
	 * Test if a rule with no value has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesEmptyTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("squid:S2094"));
	}

	/**
	 * Test if a rule with a value that does not have the correct syntax (Too much values) has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesBadTooMuchValuesTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("common-java:FailedUnitTests"));
	}

	/**
	 * Test if a rule with a value that does not have the correct syntax (NotEnoughValues) has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationCOnfigurationRulesAndValuesNotEnoughValuesTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("squid:S2096"));
	}
	
	/**
	 * Test if a rule with one of its values negative, has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesNegativeTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("squid:2333"));
	}
	
	/**
	 * Test if a rule with one of its values equals zero has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesZeroTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("squid:2666"));
	}

	/**
	 * Test if a rule with one of its values that is a word has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationRulesAndValuesWithWordTest() throws IOException{
		Assert.assertFalse(notConfs.getRulesAndValues().containsKey("squid:2999"));
	}
	
	/**
	 * Test if the TestOfTeacher quality axis has been recovered
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationQualityAxisTestOfTeacherTest() throws IOException{
		Assert.assertEquals(notConfs.getQualityAxis().get("TestOfTeacher").getFirstValue(), new Float(50));
	}

	/**
	 * Test if the TestOfStudent quality axis has been recovered
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationQualityAxisTestOfStudentTest() throws IOException{
		Assert.assertEquals(notConfs.getQualityAxis().get("TestOfStudent").getFirstValue(), new Float(3));
		Assert.assertEquals(notConfs.getQualityAxis().get("TestOfStudent").getSecondValue(), new Float(4));
	}

	/**
	 * Test if a quality axis with a value that does not have the correct syntax has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationQualityAxisCommentTest() throws IOException{
		Assert.assertFalse(notConfs.getQualityAxis().containsKey("Comments"));
	}
	
	/**
	 * Test if a quality axis with no value has not been retrieved
	 * @throws IOException if the reading of the file has a problem
	 */
	@Test
	public void notationConfigurationQualityAxisComplexityTest() throws IOException{
		Assert.assertFalse(notConfs.getQualityAxis().containsKey("Complexity"));
	}

	/**
	 * Test if an IOException is thrown when there is no notationConfiguration file to read
	 * @throws IOException if there is no file to read
	 */
	@Test(expected = IOException.class)
	public void notationConfigurationNoFileTest() throws IOException{
		new NotationConfiguration("aFileThatDoesNotExist");
	}

	/**
	 * After the last tests, the file used for the unit tests is deleted
	 */
	@AfterClass
	public static void deleteFile(){
		if (file.exists()){
			file.delete();
		}
		
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}
}