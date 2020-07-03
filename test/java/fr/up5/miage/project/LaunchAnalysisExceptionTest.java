package fr.up5.miage.project;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;

/**
 * This exception is about the launching of analysis for a Maven project and for python project 
 */
public class LaunchAnalysisExceptionTest {
	
	private static SystemConfiguration systemConfiguration;
	
	@BeforeClass
	public static void init() throws IOException{
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
		systemConfiguration=new SystemConfiguration(System.getProperty("user.dir")+File.separator+"configurationSystem.properties");
	}
	
	@Test
	public void  launchAnalysisExceptionMavenTest() 
	{
		
		String path=System.getProperty("user.dir")+File.separator+"PathNotExist";
		Project project = new Project(new User("projectStudent2"),path,null);
		try {
			try {
				if(project.launchAnalysis(systemConfiguration, null)) {
					
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SonarqubeDataBaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch ( LaunchAnalysisException e) {
				assertTrue(true);
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void  launchAnalysisExceptionPythonTest() 
	{
		String path=System.getProperty("user.dir")+File.separator+"PathNotExist";
		File pathFile=new File(path);
		Project project = new Project(new User("PythonTest"),path,null);
		try {
			if(project.launchAnalysisForPython(systemConfiguration, null, pathFile,null)) {
				assertTrue(true);
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LaunchAnalysisException e) {
				assertTrue(true);
			e.printStackTrace();
		} catch (SonarqubeDataBaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
