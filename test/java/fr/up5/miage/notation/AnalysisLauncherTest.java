package fr.up5.miage.notation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.moodle.MoodleDataBaseAccessException;
import fr.up5.miage.moodle.MoodleUser;
import fr.up5.miage.project.User;
import fr.up5.miage.sonarqube.SonarqubeWeb;
import java.nio.file.Files;

public class AnalysisLauncherTest {
	
	//public AnalysisLauncher(int idMoodleUserTeacher2, int idModule2, Boolean useMoodle2, String pathTeacherFiles2, String pathFilesToAnalyze2, String analysisName2, LocalDateTime localDate2, String dateLaunch2, boolean teacherOnly ) {

	//AnalysisLauncher analyse=new AnalysisLauncher(0);
	private static LocalDateTime date;
	private static AnalysisLauncher analyse;
	private static String analysisName;
	private static SystemConfiguration systemConfiguration;
	private static SonarqubeWeb sonar;
	
	@BeforeClass
	public static void initTest() throws Exception {
		//currentDir = System.getProperty("user.dir");
		analysisName="Abracadabra";
		LocalDateTime localDate=App.getLocalDateTime();
		String dateLaunch=localDate.getDayOfMonth()+"-"+localDate.getMonthValue()+"-"+localDate.getYear()+" "+
				localDate.getHour()+"-"+localDate.getMinute()+"-"+localDate.getSecond();
		String nameLog = "Logs"+File.separator+analysisName+" "+dateLaunch+"-"+99+".log";
		App.initLog(analysisName, dateLaunch, 99);
		App.log(dateLaunch, analysisName, nameLog);
		systemConfiguration=new SystemConfiguration(System.getProperty("user.dir")+File.separator+"configurationSystem.properties");
		date=App.getLocalDateTime();
		analyse=new AnalysisLauncher(2,2,true,"d","h",analysisName,date,dateLaunch,true);	
		analyse.getConfigurationsAndSetDirectories(2);
		sonar=new SonarqubeWeb(systemConfiguration);
		analyse.setSonar(sonar);

	}
	public static String readFile(String path, Charset encoding) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	@Test
	public void loggingConfigurationIntoAccountAnalysisTest()
	{
		String content;
		String message1="Quality axis and their value used for this analysis: ";
		String message2="Rules and their value used for this analysis:";
		
		try {
			analyse.loggingConfigurationIntoAccountAnalysis();
			content=readFile(App.fileLog.getFileName(), StandardCharsets.UTF_8);
			if(content.contains(message1) && content.contains(message1)) 
			{
				assertTrue(true);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
		
	}
	@Test
	public void logConsoleTest() 
	{	 String content;
		 String message="has been analyzed with success";
		 try {
			content=readFile(App.fileLog.getFileName(), StandardCharsets.UTF_8);
			//analyse.getConfigurationsAndSetDirectories(2);
			if(content.contains(message)) 
			{
				assertTrue(true);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.out.println(e1.getMessage());
			e1.printStackTrace();
		}
		 
	}
	
	public void creatingTemporaryFileDuringAnalysisTest() 
	{
		File pathTemp= new File(systemConfiguration.getSystemConfigs().get("pathTemporaryFolder"));
		String nameBegin="Temp-"+System.getProperty("user.name");
		String rep[]=pathTemp.list();

		try {
			analyse.creatingTemporaryFileDuringAnalysis();
			String item;
			for(int i=0;i<rep.length;i++)					
			{
				item=rep[i]; 
				if(item.startsWith(nameBegin))
				{
					assertTrue(true);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void creatingTeacherPathTest() throws Exception 
	{
		File resultPath=new File(System.getProperty("user.dir") + File.separator +"pathUnzipedTeacherFolder" + File.separator + analysisName);
		analyse.creatingTeacherPath(analysisName);
		if(resultPath.exists()) 
		{
			assertTrue(true);
		}
	}
	
	
	public void creationQualityProfileAndConfigurationTest() 
	{
		try {
			analyse.creationQualityProfileAndConfiguration();
			assertTrue(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void stockPathProjectUserIdTest() 
	{
		MoodleUser user= new MoodleUser(4, "student", " ", "ndioneBug");
		try {
			if(analyse.stockPathProjectUserId(user).isEmpty())
			{
				assertFalse(false);
			}
			else 
			{
				assertTrue(true);
			}
		} catch (MoodleDataBaseAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	


	
	public void getConfigurationsAndSetDirectories() throws Exception 
	{
		File result=analyse.getConfigurationsAndSetDirectories(2);
		if(result.exists())
		{
			assertTrue(true);
		}
	}
	
	/**
	 * user doit exister dans la base de donnée de moodle
	 */
	
	public void setStudentDirectorie() 
	{
		try {
			analyse.setStudentDirectorie(4);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	
	public void launchTeacherAnalysesTest() 
	{
		//String projectTeacherZip=
		File pathTeacher= new File(System.getProperty("user.dir")+File.separator+"ProjectsTest"+File.separator);
		try {
			analyse.lauchTeacherAnalyses(pathTeacher);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void launchTeacherAnalysesForJSTest() 
	{
		//String projectTeacherZip=
		File pathTeacher= new File(System.getProperty("user.dir")+File.separator+"ProjectsTest"+File.separator);
		try {
			//analyse.la
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public void  getPythonTestsTest() throws IOException 
	{
		String res=analyse.getPythonTests(System.getProperty("user.dir")+File.separator+"ProjectsTest"+File.separator+"PythonTest"+File.separator);
		if(res.contains("OK")) 
		{
			assertTrue(true); ////C:\NodePro
		}
	}
	
	@Test
	public void  getJsTest() throws IOException 
	{
		String res=analyse.getJavaScriptTest("C:\\NodePro"+File.separator,"mocha");
		System.out.println(res);
			assertTrue(true); ////C:\NodePro
		
	}
	@Test
	public void getSonar() {
		
		assertEquals(sonar, analyse.getSonar());
	}
	@Test
	public void setSonar() {
		analyse.setSonar(sonar);
		assertEquals(sonar, analyse.getSonar());
	}


	@Test
	public void getAnalysisName() {
		assertEquals(analysisName, analyse.getAnalysisName());
	}


}
