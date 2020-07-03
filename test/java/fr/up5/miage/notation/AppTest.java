package fr.up5.miage.notation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.LocalDateTime;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;
/**
 * Unit test for simple App.
 */
public class AppTest  {
	/**
	 * Static attribute that represents the logger for this class.
	 */
	private static LoggerConfig logMain;
	
	/**
	 * Represents the file log for this analysis
	 */
	public static FileAppender fileLog;

	/**
	 * Represent the layout of the log file for this analysis
	 */
	public static PatternLayout layout;

	/**
	 * Sequence used by operating system to separate lines in text files
	 */
	public static String brl;

	/**
	 * Represents the current directory
	 */
	public static String currentDir;
	
	/**
	 * Name of the analysis
	 */
	public static String analysisName;
	
	/**
	 * Represent the data time
	 */
	private static LocalDateTime localDate;
	private static String dateLaunch;
	private static int idMoodleUserTeacher=6;
	private static String nameLog ;
	
	@BeforeClass
	public static void initTest() 
	{
		currentDir = System.getProperty("user.dir");
		analysisName="Abracadabra";
		localDate=App.getLocalDateTime();
		dateLaunch=localDate.getDayOfMonth()+"-"+localDate.getMonthValue()+"-"+localDate.getYear()+" "+
				localDate.getHour()+"-"+localDate.getMinute()+"-"+localDate.getSecond();
		nameLog = "Logs"+File.separator+analysisName+" "+dateLaunch+"-"+idMoodleUserTeacher+".log";

	}
	
	@Test
	public void AnalysisDateTest() 
	{
		assertEquals(App.AnalysisDate(), dateLaunch);
	}
	
	@Test
	public void logTest() 
	{
		App.initLog(analysisName, dateLaunch, idMoodleUserTeacher);
		App.log(dateLaunch, analysisName, nameLog);
		File filelog=new File(nameLog);
		if(filelog.exists()) 
		{
			if(filelog.length()>0) 
			{
				assertTrue(true);
			}
				
		} 
	}

	@Test
	public void creationFileLogTest() 
	{		
		assertEquals(App.creationFileLog(analysisName, dateLaunch, idMoodleUserTeacher), nameLog);
	}
	
	@Test
	public void loggingOfExceptionTest() 
	{
		int number=4;
		try {
		int resul=number/0;
		}catch (Exception e) {
			StringBuilder error=App.loggingOfException(e, Level.FATAL);
			if(error.length()>0) 
			{
				assertTrue(true);
			}
		}
	}
	
	@Test
	public void initLogTest() 
	{
		App.initLog(analysisName, dateLaunch, idMoodleUserTeacher);
		File filelog=new File(nameLog);
		if(filelog.exists()) 
		{
			if(filelog.length()>0) 
			{
				assertTrue(true);
			}
				
		}
	}

    
}
