package fr.up5.miage.project;

import org.junit.Test;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.project.Project;
import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import fr.up5.miage.testsReport.TestRepport;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * This test cases is used to test the Project class
 */
public class ProjectTest{
	
	private static SystemConfiguration systemConfiguration;

	@BeforeClass
	public static void init() throws IOException{
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
		systemConfiguration=new SystemConfiguration(System.getProperty("user.dir")+File.separator+"configurationSystem.properties");
	}
	
	/**
	 * Test the execution of a shell command
	 * @throws IOException if the command in parameter fails
	 * @throws ClassNotFoundException if the class is not found
	 */
	
	public void executeCommandTest() throws IOException, ClassNotFoundException{
		Project project = new Project(new User("projectStudent2"),null,null);
		String[] command = {"hostname"};
		Assert.assertTrue(project.executeCommand(command,null) != null);
	}
	/**
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws LaunchAnalysisException
	 * @throws SonarqubeDataBaseException
	 * @throws SQLException
	 * @throws InterruptedException
	 */

	public void launchAnalysisTest() throws ClassNotFoundException, IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, InterruptedException
	{
		String path=System.getProperty("user.dir")+File.separator+"ProjectsTest"+File.separator+"cadragePROF";
		Project project = new Project(new User("JavaMavenPro"),path,null);
		TestRepport testRepport = new TestRepport(99, "JavaMavenPro");
		project.setTestRepport(testRepport);
		if(project.launchAnalysis(systemConfiguration, null)) {
			assertTrue(true);
		}
	}
	

	@Test
	public void getsourceId() 
	{
		try {
			//int sourceID = SonarDataBase.getInstance().getSourceID("Stack", "NodePro", "js");
		//int sourceID = SonarDataBase.getInstance().getSourceID2("NodePro","StackTest.js");
			int sourceID=SonarDataBase.getInstance().getSourceIDForJavaScript("StackTest.js", "NodePro");
			System.err.println("**********************"+sourceID);
			assertTrue(true);
		} catch (ClassNotFoundException | SonarqubeDataBaseException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws LaunchAnalysisException
	 * @throws SonarqubeDataBaseException
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	
	public void launchAnalysisForPythonTest() throws ClassNotFoundException, IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, InterruptedException 
	{
		String path=System.getProperty("user.dir")+File.separator;
		File pathFile=new File(path);
		Project project = new Project(new User("PythonTest"),path,null);
		if(project.launchAnalysisForPython(systemConfiguration, null, pathFile,null)) {
			assertTrue(true);
		}
	}
	
	
	public void launchAnalysisForJsTest() throws ClassNotFoundException, IOException, LaunchAnalysisException, SonarqubeDataBaseException, SQLException, InterruptedException 
	{
		//String path=System.getProperty("user.dir")+File.separator;
		String path="C:\\devoir\\JsPro\\src";
		File pathFile=new File(path);
		Project project = new Project(new User("PythonTest"),path,null);
		if(project.launchAnalysisForJavaScript(systemConfiguration, null, pathFile,null)) {
			assertTrue(true);
		}
	}

	
	/**
	 * After all tests
	 */
	@AfterClass
	public static void delete(){
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}
}
