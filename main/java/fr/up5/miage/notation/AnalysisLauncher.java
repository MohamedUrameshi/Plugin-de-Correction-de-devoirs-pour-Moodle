package fr.up5.miage.notation;

import fr.up5.miage.configuration.NotationConfiguration;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.moodle.MoodleDataBase;
import fr.up5.miage.moodle.MoodleDataBaseAccessException;
import fr.up5.miage.moodle.MoodleUser;
import fr.up5.miage.project.*;
import fr.up5.miage.sonarqube.QualityProfile;
import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import fr.up5.miage.sonarqube.SonarqubeWeb;
import fr.up5.miage.testsReport.TestRepport;
import fr.up5.miage.utility.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The class launches the analysis for the teacher's program and the stundents'
 * projects
 */
public class AnalysisLauncher {
	/**
	 * Static attribute that represents the logger for this class.
	 */
	private static LoggerConfig logLauncher;
	/**
	 * Class name
	 */
	private static final String analysisLauncherClass = "AnalysisLauncher.class";

	/**
	 * Initialization of static variables
	 */
	static {

		logLauncher = new LoggerConfig(analysisLauncherClass, Level.INFO, false);
		logLauncher.addAppender(App.fileLog, Level.INFO, null);
	}

	private SystemConfiguration sysConfs;

	private NotationConfiguration notConfs;

	private SonarqubeWeb sonar;

	private QualityProfile qualityProfile;

	private String pathFilesToAnalyze;

	private String pathTeacherFiles;

	private TeacherProject teacherProject;

	private StudentProject studentProject;

	private File temp;
	private File unzipteacher;

	private UtilCSVFile csvFile;

	private boolean useMoodle;

	private MoodleDataBase moodleDB;

	private File moodleDataDirectory;

	private File affectedDirectory;

	private int idMoodleUserTeacher;

	private int idAssignment;

	private int idModule;

	private User teacher;

	private String analysisName;

	private LocalDateTime localDate;

	private String dateLaunch;
	private boolean teacherOnly;

	private String nameModule;

	/**
	 * <i>Constructor</i>
	 * 
	 * @param idMoodleUserTeacher2
	 * @param idModule2
	 * @param useMoodle2
	 * @param pathTeacherFiles2
	 * @param pathFilesToAnalyze2
	 * @param analysisName2
	 * @param localDate2
	 * @param dateLaunch2
	 */
	public AnalysisLauncher(int idMoodleUserTeacher2, int idModule2, Boolean useMoodle2, String pathTeacherFiles2,
			String pathFilesToAnalyze2, String analysisName2, LocalDateTime localDate2, String dateLaunch2,
			boolean teacherOnly) {
		sysConfs = null;
		notConfs = null;
		sonar = null;
		qualityProfile = null;
		pathFilesToAnalyze = pathFilesToAnalyze2;
		pathTeacherFiles = pathTeacherFiles2;
		teacherProject = null;
		studentProject = null;
		temp = null;
		csvFile = null;
		useMoodle = useMoodle2;
		moodleDB = null;
		moodleDataDirectory = null;
		affectedDirectory = null;
		idMoodleUserTeacher = idMoodleUserTeacher2;
		idAssignment = 0;
		idModule = idModule2;
		teacher = null;
		analysisName = analysisName2;
		localDate = localDate2;
		dateLaunch = dateLaunch2;
		this.teacherOnly = teacherOnly;
	}

	/**
	 * <i>Logging of configurations for grading taken into account for the
	 * analysis.</i>
	 */
	public void loggingConfigurationIntoAccountAnalysis() {

		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage(
						"Quality axis and their value used for this analysis: " + notConfs.getQualityAxis().toString()),
				null);
		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage(
						"Rules and their value used for this analysis: " + notConfs.getRulesAndValues().toString()),
				null);
	}

	/**
	 * <i>Log if the success and print it in the console</i>
	 */
	public void logConsole(String projectStudent) {

		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage(projectStudent + " has been analyzed with success"), null);
		System.out.println(projectStudent + " has been analyzed with success");
	}

	/**
	 * <i>Creating the temporary file used during the analysis</i>
	 * 
	 * @throws Exception
	 */
	public void creatingTemporaryFileDuringAnalysis() throws Exception {
		String temporayFolderName = "Temp-" + System.getProperty("user.name") + "_" + analysisName + "_"
				+ localDate.getNano() + "_" + idMoodleUserTeacher + "_" + dateLaunch;
		temporayFolderName = temporayFolderName.replaceAll("_", "_");
		temporayFolderName = temporayFolderName.replaceAll(" ", "_");

		if (!UtilFolder.createFolder(sysConfs.getSystemConfigs().get("pathTemporaryFolder"), temporayFolderName)) {
			throw new Exception("The creation of the temporary file encountered a problem");
		}
		temp = new File(sysConfs.getSystemConfigs().get("pathTemporaryFolder"), temporayFolderName);
	}

	/**
	 * <i>Creating the temporary file used during the analysis</i>
	 * 
	 * @throws Exception
	 */
	public void creatingTeacherPath(String nameModule) throws Exception {

		String temporayFolderName = System.getProperty("user.dir") + File.separator + "pathUnzipedTeacherFolder"
				+ File.separator + nameModule;
		unzipteacher = new File(temporayFolderName);
		if (!unzipteacher.exists()) {
			if (!UtilFolder.createFolder(System.getProperty("user.dir") + File.separator + "pathUnzipedTeacherFolder",
					nameModule)) {
				throw new Exception("The creation of the temporary file encountered a problem");
			}
			unzipteacher = new File(temporayFolderName);
		}
	}

	/*
	 * <i>Creation of qualityProfile "AutomaticNotation" following by the user name
	 * on Sonarqube server, and configuration of it</i>
	 * 
	 * @throws Exception
	 */
	public void creationQualityProfileAndConfiguration() throws Exception {
		qualityProfile = new QualityProfile("AN-" + System.getProperty("user.name") + "-" + analysisName + "-"
				+ localDate.getNano() + "-" + idMoodleUserTeacher + 0, notConfs.getRulesAndValues(), sonar);
		qualityProfile.configurationRules();
	}

	/**
	 * <i>Stocks the hash path of a project for an user name for the id of the
	 * assignment</i>
	 * 
	 * @throws MoodleDataBaseAccessException
	 */
	public Map<String, String> stockPathProjectUserId(MoodleUser moodleUser) throws MoodleDataBaseAccessException {
		Map<String, String> projectsHashPath = new HashMap<String, String>();
		String projectHashPathName = moodleDB.getProjectHashPath(idAssignment, moodleUser.getIdUser());
		if (projectHashPathName != null && !projectHashPathName.equals(""))
			projectsHashPath.put(moodleUser.getProjectName(), projectHashPathName);

		return projectsHashPath;
	}

	/**
	 * Launch Sonarqube analysis and waiting two seconds for treatment
	 * 
	 * @throws LaunchAnalysisException is the analysis encountered a problem
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void launchSonarAlalysis() throws IOException, LaunchAnalysisException, InterruptedException,
			ClassNotFoundException, SonarqubeDataBaseException, SQLException {
		studentProject.launchAnalysis(sysConfs, qualityProfile);
		Thread.sleep(2000);
	}

	/**
	 * Project The main methods of the program.
	 * 
	 * @param args the argument when the launch of the program.
	 * @throws Exception
	 */
	public void start(String[] args) throws Exception {

		int userID = Integer.parseInt(args[5]);
		File folderProjectTeacher = getConfigurationsAndSetDirectories(userID);
		sonar = new SonarqubeWeb(sysConfs);

		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage("The Sonarqube server's version is: " + sonar.getVersion().toString()),
				null);

		creationQualityProfileAndConfiguration();

		if (teacherOnly) {
			lauchTeacherAnalyses(folderProjectTeacher);
			if (teacherProject == null) {
				throw new EmptyFolderException("The folder that must contain the teacher's project: "
						+ folderProjectTeacher.getAbsolutePath() + " is empty or missing project teacher zip");
			}
		} else {
			MoodleUser moodleUser = setStudentDirectorie(userID);
			lauchStudentAnalyses(userID, moodleUser, folderProjectTeacher, nameModule);
		}

		// We end the programm and log what's necessary
		// lauchStudentAnalyses(moodleUser.getProjectName(),userID);
		// We end the programm and log what's necessary
		App.end(null, qualityProfile, temp, null, useMoodle, pathTeacherFiles, affectedDirectory, analysisName);

	}

	/**
	 * recovery the configuration system file and affect the directory of mooddle
	 * data
	 */
	public File getConfigurationsAndSetDirectories(int userID) throws Exception {
		sysConfs = new SystemConfiguration("configurationSystem.properties");

		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage("System configuration used for analysis: " + sysConfs.getSystemConfigs()),
				null);
		if (useMoodle) {
			moodleDataDirectory = new File(sysConfs.getSystemConfigs().get("moodleDataRoot"));
			affectedDirectory = new File(moodleDataDirectory.getAbsolutePath() + File.separator + "repository"
					+ File.separator + idMoodleUserTeacher);
			System.out.println(affectedDirectory);
			logLauncher
					.log(analysisLauncherClass, null, null, Level.INFO,
							(Message) new SimpleMessage(
									"The path of the affected directory is: " + affectedDirectory.getAbsolutePath()),
							null);

			if (!affectedDirectory.exists() || !affectedDirectory.isDirectory()) {
				throw new Exception("Sorry, you don't have an affected directory. Contact an administrator");
			} else {
				moodleDB = MoodleDataBase.getInstance();
				idAssignment = moodleDB.getRealModuleId(idModule);
				nameModule = moodleDB.getModuleName(idAssignment);
				pathTeacherFiles = affectedDirectory.getAbsolutePath() + File.separator + "uploadedFiles"
						+ File.separator + nameModule;
				pathFilesToAnalyze = System.getProperty("user.home") + File.separator + "projectsStudent";
			}
		}
		File folderProjectTeacher = new File(pathTeacherFiles);
		if (!UtilFolder.checkPermissionsFolder(folderProjectTeacher.getAbsolutePath()).contains("rw")) {
			throw new WrongPermissionsException(
					"Sorry, the program can't read and/or write in the project teacher directory: "
							+ folderProjectTeacher.getAbsolutePath());
		}
		notConfs = new NotationConfiguration(pathTeacherFiles + File.separator + "configurationNotation.properties");

		creatingTeacherPath(nameModule + "_" + idModule);
		loggingConfigurationIntoAccountAnalysis();
		if (useMoodle) {

			teacher = new MoodleUser(idMoodleUserTeacher, "", "", "");

			if (!sysConfs.getSystemConfigs().get("prefix").equals("mdl_"))
				MoodleDataBase.setPrefix(sysConfs.getSystemConfigs().get("prefix"));

			if (!UtilFolder.checkPermissionsFolder(moodleDataDirectory.getAbsolutePath()).contains("rw")) {
				throw new WrongPermissionsException(
						"Sorry, the program can't read and/or write in the Moodle's data directory: "
								+ moodleDataDirectory.getAbsolutePath());
			}

			nameModule = moodleDB.getModuleName(idAssignment);

			if (!teacherOnly) {

			}
		} else {
			teacher = new User("");
		}
		return folderProjectTeacher;
	}

	public MoodleUser setStudentDirectorie(int userID) throws Exception {

		MoodleUser moodleUser = moodleDB.getAssignmentParticipants(idAssignment, userID, idModule);
		String nameModule = moodleDB.getModuleName(idAssignment);
		pathFilesToAnalyze += "" + File.separator + nameModule + "_" + idAssignment + File.separator;
		Map<String, String> projectsHashPath = stockPathProjectUserId(moodleUser);

		// Source :
		// http://stackoverflow.com/questions/4898/how-to-efficiently-iterate-over-each-entry-in-a-map
		Iterator<?> entriesHashPath = projectsHashPath.entrySet().iterator();

		Entry<?, ?> thisEntry = (Entry<?, ?>) entriesHashPath.next();
		String userName = (String) thisEntry.getKey();
		String projectHashPathName = (String) thisEntry.getValue();

		String realPath = moodleDataDirectory.getAbsolutePath();
		realPath += File.separator + "filedir" + File.separator;
		realPath += projectHashPathName.substring(0, 2) + File.separator;
		realPath += projectHashPathName.substring(2, 4) + File.separator;
		realPath += projectHashPathName;

		UtilFolder.createFolder(pathFilesToAnalyze, "");
		UtilFile.copyFile(realPath, pathFilesToAnalyze);
		UtilFile.renameFile(pathFilesToAnalyze + UtilFile.getFileSeparator() + projectHashPathName, userName);
		return moodleUser;
	}

	public void lauchTeacherAnalyses(File folderProjectTeacher) throws Exception {

		String[] listProjectTeacher = folderProjectTeacher.list();
		logLauncher.log(analysisLauncherClass, null, null, Level.INFO,
				(Message) new SimpleMessage("The Sonarqube server's version is: " + sonar.getVersion().toString()),
				null);
		String language = "java";
		for (String projectTeacher : listProjectTeacher) {
			if (projectTeacher.contains(".zip")) {
				System.out.println(System.getProperty("user.dir") + File.separator + projectTeacher);

				UtilFile.unzipFile(
						folderProjectTeacher.getAbsolutePath() + UtilFile.getFileSeparator() + projectTeacher,
						unzipteacher.getAbsolutePath());
				projectTeacher = projectTeacher.replaceAll(".zip", "");
				// my analyzing part
				if (UtilFolder.isMavenProject(
						unzipteacher.getAbsolutePath() + UtilFile.getFileSeparator() + projectTeacher)) {
					System.out.println("MAVEN  PROJECT   DETECTED  !!!");
					System.out.println("[START TEACHER ANALYSIS]");
					teacherProject = new TeacherProject(new User(projectTeacher),
							unzipteacher.getAbsolutePath() + UtilFolder.getFileSeparator() + projectTeacher, sonar);

					teacherProject.launchAnalysis(sysConfs, qualityProfile);
					AnalysisLauncher.waitingForServerProcessing(sonar);
					teacherProject.obtainScoreMetrics();
					SonarDataBase.getInstance().deleteSonarMetrics(idModule);
					for (String i : teacherProject.getMapScoreMetrics().keySet()) {
						SonarDataBase.getInstance().insertSonarQualityTeacher(idModule, i,
								teacherProject.getMapScoreMetrics().get(i));
					}

					TestRepport testRepportForTeacher = new TestRepport();
					teacherProject.setTestRepport(testRepportForTeacher);
					testRepportForTeacher.findAllMethods(teacherProject.getAbsolutePath(), idModule);
				} else if (UtilFolder.isAJavaProject(
						unzipteacher.getAbsolutePath() + UtilFile.getFileSeparator() + projectTeacher)) {
					System.out.println("JAVA  PROJECT   DETECTED  !!!");
					UtilFolder.convertJavaToMaven(projectTeacher);

					UtilFolder.moveConvertedMVNIntoFolder(
							unzipteacher.getAbsolutePath() + File.separator + projectTeacher);
					// now we reinitialize the maven standard projectFolder
					UtilFolder.reinitializeMVNStandardFolder();
					File pomFile = new File(unzipteacher.getAbsolutePath() + UtilFolder.getFileSeparator()
							+ projectTeacher + "/pom.xml");
					System.out.println(pomFile.getAbsolutePath());
					if (pomFile.exists()) {
						System.out.println("pom file exists");
					}

					System.out.println("[START TEACHER ANALYSIS]");
					teacherProject.launchAnalysis(sysConfs, qualityProfile);
					Thread.sleep(2000);

					AnalysisLauncher.waitingForServerProcessing(sonar);

					teacherProject.obtainScoreMetrics();
				} else if (UtilFolder.isAPythonProject(
						unzipteacher.getAbsolutePath() + UtilFile.getFileSeparator() + projectTeacher)) {
					System.err.println("je suis dans le cas d'un projet python pour teacher");
					System.out.println("[STARTING ANALYSIS FOR TEACHER PYTHON PROJECT]");
					teacherProject = new TeacherProject(new User(projectTeacher),
							unzipteacher.getAbsolutePath() + UtilFolder.getFileSeparator() + projectTeacher, sonar);

					TestRepport testRepportForTeacher = new TestRepport();
					teacherProject.setTestRepport(testRepportForTeacher);

					String path = unzipteacher.getAbsolutePath() + File.separator + projectTeacher;
					String commandExecution = getPythonTests(path);
					testRepportForTeacher.findAllMethodsForPython(idModule, commandExecution);
					teacherProject.launchAnalysisForPython(sysConfs, qualityProfile, unzipteacher,
							teacherProject.getMapScoreMetrics());
					AnalysisLauncher.waitingForServerProcessing(sonar);
					teacherProject.obtainScoreMetrics();
					HashMap<String, Float> map = teacherProject.getMapScoreMetrics();
					int projectID = SonarDataBase.getInstance().getProjectID(projectTeacher);
					TestRepport.createTestMetricsForPython(commandExecution, map, projectID, idModule);
					SonarDataBase.getInstance().deleteSonarMetrics(idModule);
					language = "python";
					for (String i : teacherProject.getMapScoreMetrics().keySet()) {
						SonarDataBase.getInstance().insertSonarQualityTeacher(idModule, i,
								teacherProject.getMapScoreMetrics().get(i));
					}
				} else if (SonarDataBase.getInstance().getLanguage(idModule).toLowerCase().contains("php")) {
					System.err.println("je suis dans le cas d'un projet PHP pour teacher");
					System.out.println("[STARTING ANALYSIS FOR TEACHER PHP PROJECT]");
					teacherProject = new TeacherProject(new User(projectTeacher),
							unzipteacher.getAbsolutePath() + UtilFolder.getFileSeparator() + projectTeacher, sonar);
					String path = unzipteacher.getAbsolutePath() + File.separator + projectTeacher;
					String command = getPHPTests(path, 0);
					TestRepport testRepportForTeacher = new TestRepport();
					testRepportForTeacher.findAllMethodsForPHP(idModule, command);
					teacherProject.launchAnalysisForPHP(sysConfs, qualityProfile, unzipteacher,
							teacherProject.getMapScoreMetrics());
					AnalysisLauncher.waitingForServerProcessing(sonar);
					teacherProject.obtainScoreMetrics();
					HashMap<String, Float> map = teacherProject.getMapScoreMetrics();
					int projectID = SonarDataBase.getInstance().getProjectID(projectTeacher);
					command = getPHPTests(path, 1);
					TestRepport.createTestMetricsForPHP(command, map, projectID, idModule);
					SonarDataBase.getInstance().deleteSonarMetrics(idModule);
					language = "php";
					for (String i : teacherProject.getMapScoreMetrics().keySet()) {
						SonarDataBase.getInstance().insertSonarQualityTeacher(idModule, i,
								teacherProject.getMapScoreMetrics().get(i));
					}
				} else if (SonarDataBase.getInstance().getLanguage(idModule).toLowerCase().contains("js")) {
					System.err.println("je suis dans le cas d'un projet JavaScript pour teacher");
					System.out.println("[STARTING ANALYSIS FOR TEACHER JavaScript PROJECT]");
					teacherProject = new TeacherProject(new User(projectTeacher),
							unzipteacher.getAbsolutePath() + UtilFolder.getFileSeparator() + projectTeacher, sonar);
					String path = unzipteacher.getAbsolutePath() + File.separator + projectTeacher;
				
					String pathMocha=sysConfs.getSystemConfigs().get("pathmocha");
					String resultFile = getJavaScriptTest(path,pathMocha);
					TestRepport testRepportForTeacher = new TestRepport();
					HashMap<String, String> globalResult = testRepportForTeacher.readXmlFileForTeacher(path + File.separator + resultFile, idModule,projectTeacher);
					teacherProject.launchAnalysisForJavaScript(sysConfs, qualityProfile, unzipteacher,
							teacherProject.getMapScoreMetrics());
					AnalysisLauncher.waitingForServerProcessing(sonar);
					teacherProject.obtainScoreMetrics();
					HashMap<String, Float> map = teacherProject.getMapScoreMetrics();
					int projectID = SonarDataBase.getInstance().getProjectID(projectTeacher);
					TestRepport.createMetricsForJs(globalResult, map, idModule, projectID);			
					SonarDataBase.getInstance().deleteSonarMetrics(idModule);
					language = "js";
					for (String i : teacherProject.getMapScoreMetrics().keySet()) {
						SonarDataBase.getInstance().insertSonarQualityTeacher(idModule, i,
								teacherProject.getMapScoreMetrics().get(i));
					}
				}
				break;
			}
		}
		if (teacherProject == null) {
			throw new EmptyFolderException("The folder that must contain the teacher's project: "
					+ folderProjectTeacher.getAbsolutePath() + " is empty or missing project teacher zip");
		}
		// SonarDataBase.getInstance().insertLanguage(idModule,language);
	}

	private void lauchStudentAnalyses(int userID, MoodleUser moodleUser, File folderProjectTeacher, String nameModule)
			throws Exception {

		/**
		 * taking teacher project and notation configuration
		 */
		// Verify permission on teacher project directory.
		if (!UtilFolder.checkPermissionsFolder(folderProjectTeacher.getAbsolutePath()).contains("rw")) {
			throw new WrongPermissionsException(
					"Sorry, the program can't read and/or write in the project teacher directory: "
							+ folderProjectTeacher.getAbsolutePath());
		}

		notConfs = new NotationConfiguration(pathTeacherFiles + File.separator + "configurationNotation.properties");
		loggingConfigurationIntoAccountAnalysis();

		String[] listProjectTeacher = folderProjectTeacher.list();

		if (listProjectTeacher.length == 0) {
			throw new EmptyFolderException("The folder that must contain the teacher projects is empty");
		}

		String teacherProjectName = "";
		for (String projectTeacher : listProjectTeacher) {
			if (projectTeacher.contains(".zip")) {
				teacherProjectName = projectTeacher.replace(".zip", "");
			}
		}

		creatingTemporaryFileDuringAnalysis();
		File folderProjectsStudents = new File(pathFilesToAnalyze);
		if (!UtilFolder.checkPermissionsFolder(folderProjectsStudents.getAbsolutePath()).contains("rw")) {
			throw new WrongPermissionsException(
					"Sorry, the program can't read and/or write in the project student directory: "
							+ folderProjectsStudents.getAbsolutePath());
		}

		String projectName = moodleUser.getProjectName();

		if (projectName.contains(".zip")) {
			UtilFile.unzipFile(folderProjectsStudents.getAbsolutePath() + UtilFile.getFileSeparator() + projectName,
					temp.getAbsolutePath());
			projectName = projectName.replaceAll(".zip", "");
			// my analyzing part
			HashMap<String, Float> mapScoreMetrics = SonarDataBase.getInstance().getTeacherMetrics(idModule);
			TeacherProject teacherProject1 = new TeacherProject(mapScoreMetrics);
			if (UtilFolder.isMavenProject((temp.getAbsolutePath() + UtilFile.getFileSeparator() + projectName))) {
				System.out.println("JAVA  PROJECT   DETECTED  !!!");

				studentProject = new StudentProject(new User(projectName),
						temp.getAbsolutePath() + UtilFolder.getFileSeparator() + projectName, sonar, teacherProject1,
						notConfs);

				TestRepport testRepport = new TestRepport(idModule, this.studentProject.getAuthor().getProjectName());
				studentProject.setTestRepport(testRepport);

				if (!UtilFolder.isMavenProject(studentProject.getAbsolutePath())) {
					throw new NotMavenProjectException(projectName + " project is not a Maven project");
				}
				teacherProject = new TeacherProject(new User(""),
						unzipteacher.getAbsolutePath() + File.separator + teacherProjectName, sonar);
				UtilFolder.placeTestFromProjectTeacherToProjectStudent(new File(studentProject.getAbsolutePath()),
						new File(teacherProject.getAbsolutePath()));

				System.out.println("[START ANALYSIS]");
				launchSonarAlalysis();
				AnalysisLauncher.waitingForServerProcessing(sonar);
				studentProject.obtainScoreMetrics();
				studentProject.obtainMapIssues();
				studentProject.obtainScoreSuccessTests(teacherProject1.getMapScoreMetrics().get("tests"));
				studentProject.calculAllGrade(idModule);
			} else if (UtilFolder
					.isAPythonProject(temp.getAbsolutePath() + UtilFile.getFileSeparator() + projectName)) {
				System.err.println("je suis dans le cas d'un projet python pour teacher");
				System.out.println("[STARTING ANALYSIS FOR TEACHER PYTHON PROJECT]");

				studentProject = new StudentProject(new User(projectName),
						temp.getAbsolutePath() + UtilFolder.getFileSeparator() + projectName, sonar, teacherProject1,
						notConfs);

				TestRepport testRepport = new TestRepport(idModule, this.studentProject.getAuthor().getProjectName());
				studentProject.setTestRepport(testRepport);

				teacherProject = new TeacherProject(new User(""),
						unzipteacher.getAbsolutePath() + File.separator + teacherProjectName, sonar);
				UtilFolder.placeTestFromProjectTeacherToProjectStudentForPythonOrPHP(
						new File(studentProject.getAbsolutePath()), new File(teacherProject.getAbsolutePath()),
						"python");
				studentProject.launchAnalysisForPython(sysConfs, qualityProfile, temp,
						teacherProject.getMapScoreMetrics());
				AnalysisLauncher.waitingForServerProcessing(sonar);
				studentProject.obtainScoreMetrics();
				HashMap<String, Float> map = studentProject.getMapScoreMetrics();
				String commandExecution = getPythonTests(temp.getAbsolutePath() + File.separator + projectName);
				int projectID = SonarDataBase.getInstance().getProjectID(projectName);
				testRepport.createTestDetailsForPython(projectID, projectName, idModule, commandExecution);
				TestRepport.createTestMetricsForPython(commandExecution, map, projectID, idModule);
			} else if (SonarDataBase.getInstance().getLanguage(idModule).toLowerCase().contains("php")) {
				System.err.println("je suis dans le cas d'un projet python pour teacher");
				System.out.println("[STARTING ANALYSIS FOR STUDENT PHP PROJECT]");
				studentProject = new StudentProject(new User(projectName),
						temp.getAbsolutePath() + UtilFolder.getFileSeparator() + projectName, sonar, teacherProject1,
						notConfs);

				TestRepport testRepport = new TestRepport(idModule, this.studentProject.getAuthor().getProjectName());
				studentProject.setTestRepport(testRepport);

				teacherProject = new TeacherProject(new User(""),
						unzipteacher.getAbsolutePath() + File.separator + teacherProjectName, sonar);
				UtilFolder.placeTestFromProjectTeacherToProjectStudentForPythonOrPHP(
						new File(studentProject.getAbsolutePath()), new File(teacherProject.getAbsolutePath()), "php");
				studentProject.launchAnalysisForPHP(sysConfs, qualityProfile, temp,
						teacherProject.getMapScoreMetrics());
				AnalysisLauncher.waitingForServerProcessing(sonar);
				studentProject.obtainScoreMetrics();
				HashMap<String, Float> map = studentProject.getMapScoreMetrics();
				String commandExecution = getPHPTests(temp.getAbsolutePath() + File.separator + projectName, 1);
				int projectID = SonarDataBase.getInstance().getProjectID(projectName);
				testRepport.createTestDetailsForPHP(projectID, projectName, idModule, commandExecution);
				//TestRepport.createTestMetricsForPython(commandExecution, map, projectID, idModule);
			} else if ((SonarDataBase.getInstance().getLanguage(idModule).toLowerCase().contains("js"))) {
				System.err.println("je suis dans le cas d'un JavaScript pour teacher");
				System.out.println("[STARTING ANALYSIS FOR STUDENT JavaScript PROJECT]");
				studentProject = new StudentProject(new User(projectName),
						temp.getAbsolutePath() + UtilFolder.getFileSeparator() + projectName, sonar, teacherProject1,
						notConfs);
				teacherProject = new TeacherProject(new User(""),
						unzipteacher.getAbsolutePath() + File.separator + teacherProjectName, sonar);
				TestRepport testRepport = new TestRepport(idModule, this.studentProject.getAuthor().getProjectName());
				studentProject.setTestRepport(testRepport);
				System.err.println("Tea "+teacherProject);
				System.err.println("Stu"+studentProject);
				UtilFolder.placeTestFromProjectTeacherToProjectStudentforJavaScript(new File(teacherProject.getAbsolutePath()),new File(studentProject.getAbsolutePath()));;
				studentProject.launchAnalysisForJavaScript(sysConfs, qualityProfile, temp,teacherProject.getMapScoreMetrics());
				AnalysisLauncher.waitingForServerProcessing(sonar);
				int projectID = SonarDataBase.getInstance().getProjectID(projectName);
				String pathMocha=sysConfs.getSystemConfigs().get("pathmocha");
				String nameFileReport = getJavaScriptTest(studentProject.getAbsolutePath(),pathMocha);
				studentProject.obtainScoreMetrics();
				HashMap<String, String> globalRes=testRepport.readXmlFileForStudent(studentProject.getAbsolutePath()+File.separator+nameFileReport, idModule,projectID);
				HashMap<String, Float> map = studentProject.getMapScoreMetrics();
				TestRepport.createMetricsForJs(globalRes, map, idModule, projectID);
			}
			studentProject.obtainScoreMetrics();
			studentProject.obtainMapIssues();
			studentProject.obtainScoreSuccessTests(teacherProject1.getMapScoreMetrics().get("tests"));
			studentProject.calculAllGrade(idModule);
			float finalGrade = studentProject.getAllGrade().get("FinalGrade");
			int id_student = userID;
			SonarDataBase.getInstance().insertGrade(nameModule, idModule, studentProject.getAuthor().getProjectName(),
					finalGrade, id_student);
			logConsole(projectName);
		}
	}

	/**
	 * This method launches a "sonar analysis" of the project thanks to Maven goal
	 * "sonar:sonar" and launches a mvn test to rebuild the project before the
	 * Sonarqube analysis
	 * 
	 * @return a boolean, true if the launch is a success, false if it fails
	 * @throws IOException             if the reading of text result has encountered
	 *                                 a problem
	 * @throws LaunchAnalysisException if the analysis launch has a problem
	 */
	public String getPythonTests(String path) throws IOException {
		String pythonTest = "python -m unittest discover " + path + " -v";
		StringBuilder commandPython = new StringBuilder(pythonTest);
		File dir = new File(path);
		String[] command = { "", "", pythonTest };

		if (System.getProperty("os.name").contains("Windows")) {
			command[0] = "cmd.exe";
			command[1] = "/C";
		} else {
			command[0] = "/bin/sh";
			command[1] = "-c";
		}

		command[2] = commandPython.toString();
		String textCommand = Project.executeCommand(command, dir);
		return textCommand;
	}

	/**
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public String getJavaScriptTest(String path,String pathMocha) throws IOException {
		// String reportName=
		//String pathNode=systemConfiguration.getSystemConfigs().get("nodeJsApplication"); 

		String pythonTest = pathMocha+" test --reporter mocha-junit-reporter --reporter-options mochaFile=./report.xml ";
		StringBuilder commandPython = new StringBuilder(pythonTest);
		File dir = new File(path);
		String[] command = { "", "", pythonTest };

		if (System.getProperty("os.name").contains("Windows")) {
			command[0] = "cmd.exe";
			command[1] = "/C";
		} else {
			command[0] = "/bin/sh";
			command[1] = "-c";
		}
		System.err.println(dir.getAbsolutePath());
		command[2] = commandPython.toString();
		System.err.println(command[2]);
		String textCommand = Project.executeCommand(command, dir);
		return "report.xml";
	}

	/**
	 * 
	 * @param path
	 * @param type
	 * @return
	 * @throws IOException
	 */
	public String getPHPTests(String path, int type) throws IOException {
		String phpBinFolder = sysConfs.getSystemConfigs().get("phpBinFolder");
		String phpUnitBinFolder = sysConfs.getSystemConfigs().get("phpUnitBinFolder");
		String phpTest = "";
		if (type == 0) {
			phpTest = phpBinFolder + "php " + phpUnitBinFolder + "phpunit --list-tests " + path;
		} else {
			phpTest = phpBinFolder + "php " + phpUnitBinFolder + "phpunit " + path;
		}
		StringBuilder commandPHP = new StringBuilder(phpTest);
		File dir = new File(path);
		String[] command = { "", "", phpTest };

		if (System.getProperty("os.name").contains("Windows")) {
			command[0] = "cmd.exe";
			command[1] = "/C";
		} else {
			command[0] = "/bin/sh";
			command[1] = "-c";
		}

		command[2] = commandPHP.toString();
		String textCommand = Project.executeCommand(command, dir);
		return textCommand;
	}

	/**
	 * This method asks the queue state of Sonarqube server
	 * 
	 * @param sonar is the SonarqubeWeb instance use for exchange with Sonarqube
	 *              server
	 * @throws Exception if an Exception is thrown
	 */
	public static void waitingForServerProcessing(SonarqubeWeb sonar) throws Exception {
		int counter;
		for (counter = 0; counter < 200 && sonar.getQueueAnalysisReports().equals("false"); counter++) {
			Thread.sleep(1500);
		}
		if (counter == 200) {
			throw new Exception("The queue of Compute Engine seems to be frozen.");
		}
	}

	public SonarqubeWeb getSonar() {
		return sonar;
	}

	public void setSonar(SonarqubeWeb sonar) {
		this.sonar = sonar;
	}

	public QualityProfile getQualityProfile() {
		return qualityProfile;
	}

	public File getTemp() {
		return temp;
	}

	public void setTemp(File temp) {
		this.temp = temp;
	}

	public UtilCSVFile getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(UtilCSVFile csvFile) {
		this.csvFile = csvFile;
	}

	public boolean isUseMoodle() {
		return useMoodle;
	}

	public User getTeacher() {
		return teacher;
	}

	public void setTeacher(User teacher) {
		this.teacher = teacher;
	}

	public String getAnalysisName() {
		return analysisName;
	}

	public String getPathTeacherFiles() {
		return pathTeacherFiles;
	}

	public File getAffectedDirectory() {
		return affectedDirectory;
	}
}
