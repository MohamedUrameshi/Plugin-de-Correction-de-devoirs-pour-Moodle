/*
 * Links that helped for coding:
 * http://logging.apache.org/log4j/2.x/log4j-core/apidocs/index.html
 * http://stackoverflow.com/questions/39228697/log4j2-custom-appender-error-attempted-to-append-to-non-started-appender
 * http://stackoverflow.com/questions/6998323/log4j-dynamic-configuration
 * https://logging.apache.org/log4j/2.0/log4j-api/apidocs/index.html
 * http://stackoverflow.com/questions/13416499/how-to-configure-log4j-fileappender-for-all-classes
 * http://www.commentcamarche.net/forum/affich-1170081-java-retour-a-la-ligne
 */

package fr.up5.miage.notation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import fr.up5.miage.moodle.MoodleDataBase;
import fr.up5.miage.moodle.MoodleDataBaseAccessException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.sonarqube.QualityProfile;
import fr.up5.miage.sonarqube.SonarServerHttpErrorException;
import fr.up5.miage.utility.UtilCSVFile;
import fr.up5.miage.utility.UtilFolder;

/**
 * This class contains the main of program.
 */
public class App {
	
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
	
	static {
		
		logMain = new LoggerConfig("App.class",Level.INFO,false);
				
		
		layout = PatternLayout.newBuilder().withPattern("%d{yyy-MM-dd HH:mm:ss,SSS} %-5level [%t] %logger{36} - %msg*%n").build();

	
		brl = System.getProperty("line.separator");
			
	
		currentDir = System.getProperty("user.dir");
	}
	
	/**
	 * <i>Analysis Date</i>
	 */
	 public static String AnalysisDate() {
	    LocalDateTime localDate = getLocalDateTime();
		String dateLaunch = localDate.getDayOfMonth()+"-"+localDate.getMonthValue()+"-"+localDate.getYear()+" "+
		localDate.getHour()+"-"+localDate.getMinute()+"-"+localDate.getSecond();
		
		return dateLaunch;
	 }
	 
	 public static LocalDateTime getLocalDateTime() {
		 return LocalDateTime.now();
	 }
	 
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, MoodleDataBaseAccessException, IOException {
		try {
			String dateLaunch = AnalysisDate();
			LocalDateTime localDate = getLocalDateTime();
			
			int idMoodleUserTeacher = 0;
			int idModule = 0;
			Boolean useMoodle;
			String pathTeacherFiles = "";
			String pathFilesToAnalyze = "";
			String analysisName = "";
			boolean teacherOnly = false;
			/**
			 for only teacher analysis
			 **/
			if(args.length > 5)
			{
				teacherOnly = Boolean.valueOf(args[4]);
			}
			
			try{
				useMoodle = Boolean.valueOf(args[0]);
				if(useMoodle){
					idMoodleUserTeacher = Integer.parseInt(args[2]);
					idModule = Integer.parseInt(args[3]);
				}
				else{
					pathTeacherFiles = args[2];
					pathFilesToAnalyze = args[3];
				}
				analysisName = args[1];
				
				App.initLog(analysisName, dateLaunch, idMoodleUserTeacher);
			}
			catch(ArrayIndexOutOfBoundsException e){
			
				App.initLog(analysisName, dateLaunch, idMoodleUserTeacher);
				throw new Exception("Parameters are missing");
			}
			catch(NumberFormatException e1) {
				App.initLog(analysisName, dateLaunch, idMoodleUserTeacher);
				throw new Exception("The parameters were not recognized");
			}
			
			AnalysisLauncher al = new AnalysisLauncher(idMoodleUserTeacher, idModule, useMoodle, pathTeacherFiles, pathFilesToAnalyze, analysisName, localDate, dateLaunch,teacherOnly);
			
			try{
				al.start(args);
				System.out.println("Après appelle de la methode start de APP.java ");
				System.out.println("FINISHED_SUCCESSFULLY");

				
			} 
			catch (Exception e) {
				end(e, al.getQualityProfile(), al.getTemp(), al.getCsvFile(), al.isUseMoodle(), al.getPathTeacherFiles(), al.getAffectedDirectory(), al.getAnalysisName());
			}
		}catch(Exception e) {
			App.printLog(e, Level.ERROR);
		}
		MoodleDataBase.getInstance().closeConnexion();		
	}
	
	/**
	 * <i>Log in console</i>
	 */
     public static void log(String dateLaunch,String analysisName,String nameLog) {
    	
 		logMain.addAppender(App.fileLog, Level.INFO, null);
 		
 		logMain.log("App.class",null,null,Level.INFO,(Message)new SimpleMessage("**********************New analysis launched at: "+dateLaunch+"**********************"),null);
 		System.out.println(App.brl+"**********************New analysis launched at: "+dateLaunch+"**********************"+App.brl);
 		
 		
 		logMain.log("App.class",null,null,Level.INFO,(Message)new SimpleMessage("Analysis name is: "+analysisName),null);
 		System.out.println("Analysis name is: "+analysisName);
 		
 		
 		logMain.log("App.class",null,null,Level.INFO,(Message)new SimpleMessage("Logs file created at: "+App.currentDir+"Logs"+File.separator+nameLog),null);
 		System.out.println("Logs file created at: "+App.currentDir+File.separator+nameLog);
     }
	/**
	 * File the log before ending the program
	 * @param e 
	 * @param qualityProfile is the program's profile on SonarQube
	 * @param temp is the temporary file with the projects
	 * @param csvFile is the file containing the results, deleted if the function is called from an exception
	 * @param useMoodle is true if the programm has been launched from Moodle
	 * @param pathTeacherFiles is the path to the teacher's uploaded files
	 * @param affectedDirectory is the path to the teacher's Moodle directory
	 */
	public static void end(Exception e, QualityProfile qualityProfile, File temp, UtilCSVFile csvFile, boolean useMoodle, String pathTeacherFiles, File affectedDirectory, String analysisName) {
		
		
		if (e != null)
		{
			App.printLog(e, Level.FATAL);
		}

		try{
			
			if (qualityProfile != null){
				
				qualityProfile.deleteProfile();
			}
		}
		catch(SonarServerHttpErrorException | IOException e1){
		
			App.printLog(e1, Level.ERROR);
		}
		try{
			
			if (temp != null && temp.exists()){
				
				UtilFolder.deleteFolder(temp.getAbsolutePath());
			}
		}
		catch(FileNotFoundException e1){
		
			App.printLog(e1, Level.ERROR);
		}
		
	
		if (csvFile != null)
		{
			csvFile.deleteCsvFile();
		}
			
		
	
		logMain.log("App.class",null,null,Level.INFO,(Message)new SimpleMessage("************************************End of analysis "+analysisName+" ************************************"),null);

	
		System.out.println(App.brl+"************************************End of analysis "+analysisName+"************************************");
	}

    /**
     * <i>Creation of file log for this analysis</i>
     */
	  public static String creationFileLog(String analysisName, String dateLaunch, int idMoodleUserTeacher) {
		  String nameLog = "Logs"+File.separator+analysisName+" "+dateLaunch+"-"+idMoodleUserTeacher+".log";
		  App.fileLog = FileAppender.newBuilder().withFileName(nameLog).withName("automaticNotation").withLayout(App.layout).build();
		  
		  //System.out.println(nameLog);
		  return nameLog;
	  }
	  
	/**
	 * This method initializes the log for one analysis
	 * @param analysisName is the analysisName
	 * @param dateLaunch represents the launch date of program
	 * @param idMoodleUserTeacher is the if of MoodleUserTeacher, it's equal 0 if Moodle is not used
	 */
	public static void initLog(String analysisName, String dateLaunch, int idMoodleUserTeacher){
	
		 String nameLog = creationFileLog(analysisName,dateLaunch,idMoodleUserTeacher);
		
	
		App.fileLog.start();
		
         log(dateLaunch,analysisName,nameLog);
	}

	/**
	 *<i>Logging of the exception's informations that has been catched.</i>
	 */
     public static StringBuilder loggingOfException(Exception e, Level level) {
    	 StringBuilder logEx = new StringBuilder(e.getClass().toString()+" "+e.getMessage()+" at:"+App.brl);
 		 for (StackTraceElement stack : e.getStackTrace()) logEx.append("\t"+stack+App.brl);
 		 logMain.log("App.class",null,null,level,(Message)new SimpleMessage(logEx),null);
    	 
 		 return logEx;
     }
     
	/**
	 * This method log and print in console
	 * @param e is the exception to log and to print in console
	 * @param level is the level of the log
	 */
	public static void printLog(Exception e, Level level){

	
		StringBuilder logEx = loggingOfException(e,level);

	
		System.out.println(logEx);
	}
}
