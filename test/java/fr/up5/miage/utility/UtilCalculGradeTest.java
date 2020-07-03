package fr.up5.miage.utility;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import fr.up5.miage.configuration.ModelValue;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.project.LaunchAnalysisException;
import fr.up5.miage.project.Project;
import fr.up5.miage.project.User;
import fr.up5.miage.sonarqube.*;
import fr.up5.miage.testsReport.TestRepport;

/**
 * This class of tests is used to test the UtilCalculGrade class
 */
public class UtilCalculGradeTest {

	private static HashMap<String, ModelValue> mapQuality;
	private static HashMap<String, Float> listScoreMetricStudent;
	private static HashMap<String, Float> listScoreMetricTeacher;
	private static HashMap<String, Float> listLostPoints;
	private static HashMap<String, Integer> mapIssues;
	private static HashMap<String, ModelValue> mapConf;
	private static HashMap<String, Float> listAllGrades;
	private static HashMap<String, Integer> mapIssuesEmpty;
	private static String projectName;
	private static int idModule = 99;
	private static String projectStudent;
	private static SystemConfiguration systemConfiguration;
	private static String analysisName;


	/**
	 * Before all tests
	 * 
	 * @throws InterruptedException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws LaunchAnalysisException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@BeforeClass
	public static void init() throws ClassNotFoundException, LaunchAnalysisException, SonarqubeDataBaseException,
			SQLException, InterruptedException, IOException{

		// Initializing the static attributes
		mapQuality = new HashMap<String, ModelValue>();
		listScoreMetricStudent = new HashMap<String, Float>();
		listScoreMetricTeacher = new HashMap<String, Float>();
		listLostPoints = new HashMap<String, Float>();
		mapIssues = new HashMap<String, Integer>();
		mapConf = new HashMap<String, ModelValue>();
		listAllGrades = new HashMap<String, Float>();
		mapIssuesEmpty = new HashMap<String, Integer>();

		// Filling the mapQuality with static attributes
		mapQuality.put("TestOfTeacher", new ModelValue(50f));
		mapQuality.put("TestOfStudent", new ModelValue(4f, 1f));
		mapQuality.put("Comments", new ModelValue(25f, 1.5f));
		mapQuality.put("Complexity", new ModelValue(20f, 1f));

		// Filling the listScoreMetricStudent with static attributes
		listScoreMetricStudent.put("comment_lines_density", 50f);
		listScoreMetricStudent.put("complexity", 13f);
		listScoreMetricStudent.put("test_success_density", 50f);
		listScoreMetricStudent.put("tests", 4f);
		listScoreMetricStudent.put("test_success_density_teacher", 0.75f);

		// Filling the listScoreMetricTeacher with static attributes
		listScoreMetricTeacher.put("comment_lines_density", 70f);
		listScoreMetricTeacher.put("complexity", 11f);
		listScoreMetricTeacher.put("test_success_density", 100f);
		listScoreMetricTeacher.put("tests", 14f);

		// Filling the listLostPoints with static attributes
		listLostPoints.put("TestOfTeacher", 5f);
		listLostPoints.put("Comments", 1.5f);
		listLostPoints.put("TestOfStudent", 1f);
		listLostPoints.put("Complexity", 0f);

		// Filling the mapIssues with static attributes
		mapIssues.put("squid:S106", new Integer("1"));
		mapIssues.put("squid:S1888", new Integer("3"));
		mapIssues.put("common-java:InsufficientBranchCoverage", new Integer("6"));
		mapIssues.put("squid:S199", new Integer("2"));
		mapIssues.put("squid:S1022", new Integer("1"));

		// Filling the mapConf with static attributes
		mapConf.put("squid:S106", new ModelValue(2f, 2f, 3f));
		mapConf.put("squid:S1888", new ModelValue(3f, 0.5f, 3f));
		mapConf.put("common-java:InsufficientBranchCoverage", new ModelValue(1f, 0.5f, 3f));
		mapConf.put("squid:S199", new ModelValue(1f, 0.5f, 1f));
		mapConf.put("squid:S1022", new ModelValue(1f, 0f, 4f));

		// Filling the listAllGrades with static attributes
		listAllGrades = (HashMap<String, Float>) listLostPoints.clone();
		listAllGrades.put("Rules", 3.5f);
		listAllGrades.put("FinalGrade", 9f);

		analysisName="Abracadabra";
		LocalDateTime localDate=App.getLocalDateTime();
		String dateLaunch=localDate.getDayOfMonth()+"-"+localDate.getMonthValue()+"-"+localDate.getYear()+" "+
				localDate.getHour()+"-"+localDate.getMinute()+"-"+localDate.getSecond();
		String nameLog = "Logs"+File.separator+analysisName+" "+dateLaunch+"-"+99+".log";
		App.initLog(analysisName, dateLaunch, 99);
		App.log(dateLaunch, analysisName, nameLog);
		//Configuration System
		systemConfiguration = new SystemConfiguration(System.getProperty("user.dir") + File.separator + "configurationSystem.properties");
		
		//Launch Project analyze 
		projectStudent = System.getProperty("user.dir") + File.separator + "ProjectsTest" + File.separator+ "cadragePROF";
		Project project = new Project(new User("projectStudent2"),projectStudent,null);
		TestRepport testRepport = new TestRepport(99, "projectStudent2");
		project.setTestRepport(testRepport);
		if(project.launchAnalysis(systemConfiguration, null)) {
			System.out.println("Le projet du l'etudiant a été bien analysé");
		}
		//name of project 
		projectName="projectStudent2";
	}

	
	@Test
	public void calculMetricSuperiorStudentBetterScoreTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricSuperior(new ModelValue(25f, 1f), 11f, 12f), new Float(0));
	}

	/**
	 * Test the calculMetricSuperior method in case the teacher has the best score
	 * but the student does not lose points
	 */
	@Test
	public void calculMetricSuperiorTeacherBetterScoreButNoLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricSuperior(new ModelValue(25f, 1f), 12f, 11f), new Float(0));
	}

	/**
	 * Test the calculMetricSuperior method in case the teacher has the best score
	 * and the student loses points
	 */
	@Test
	public void calculMetricSuperiorWithLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricSuperior(new ModelValue(10f, 1f), 12f, 9f), new Float(1));
	}

	/**
	 * Test the calculMetricInferior in case the student has the best score
	 */
	@Test
	public void calculMetricInferiorStudentBetterScoreTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricInferior(new ModelValue(25f, 2f), 12f, 8f), new Float(0));
	}

	/**
	 * Test the calculMetricInferior method in case the teacher has the best score
	 * but the student does not lose points
	 */
	@Test
	public void calculMetricInferiorTeacherBetterScoreButNoLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricInferior(new ModelValue(25f, 1f), 12f, 13f), new Float(0));
	}

	/**
	 * Test the calculMetricInferior method in case the teacher has the best score
	 * and the student loses points
	 */
	@Test
	public void calculMetricInferiorTeacherWithLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculMetricInferior(new ModelValue(10f, 1f), 12f, 15f), new Float(1));
	}

	/**
	 * Test the calculQualityAxis method in the case where there is no loss of
	 * points
	 */
	@Test
	public void calculQualityAxisWithNoLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculTestOfStudentQualityAxis(new ModelValue(4f, 2f), 4f), new Float(0));
	}

	/**
	 * Test the calculQualityAxis method in the case where there is loss of points
	 */
	@Test
	public void calculQualityAxisWithLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculTestOfStudentQualityAxis(new ModelValue(4f, 2f), 3f), new Float(2));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is not loss
	 * of points
	 */
	@Test
	public void calculLostPointsByOneRuleNoLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 1f, 3f), 0), new Float(0));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is loss of
	 * points
	 */
	@Test
	public void calculLostPointsByOneRuleWithLostPointsTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 1f, 3f), 1), new Float(1));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is the
	 * maximum lost of points
	 */
	@Test
	public void calculLostPointsByOneRuleWithLostPointsMaxTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 1f, 3f), 4), new Float(3));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where the minimum and
	 * the maximum have the same value
	 */
	@Test
	public void calculLostPointsByOneRuleSameMaxAndMinTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 0.5f, 1f), new Integer(1)),
				new Float(0.5));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is the
	 * maximum loss of points
	 */
	@Test
	public void calculLostPointsByOneRuleAboveMaxTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 0.5f, 3f), new Integer(5)),
				new Float(1.5));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is no lost
	 * of points
	 */
	@Test
	public void calculLostPointsByOneRuleBelowMinTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(2f, 0.5f, 3f), new Integer(1)),
				new Float(0));
	}

	/**
	 * Test the calculLostPointsByOneRule method in the case where there is lost of
	 * points, but not the minimum loss or maximum loss
	 */
	@Test
	public void calculLostPointsByOneRuleBetweenMaxMinTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByOneRule(new ModelValue(1f, 0.5f, 4f), new Integer(3)),
				new Float(1.5));
	}

	/**
	 * Test the calculLostPointsByQualityAxisTest method
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void calculLostPointsByQualityAxisTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		listLostPoints.put("TestOfTeacher", 0.0f);
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByQualityAxis(mapQuality, listScoreMetricStudent,
				listScoreMetricTeacher, projectName, idModule), listLostPoints);
	}

	/**
	 * Test the calculLostPointsByQualityAxisTest method in the case where one of
	 * metric is absent
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void calculLostPointsByQualityAxisIfOneMetricIsAbsentTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		listScoreMetricStudent.remove("test");
		listLostPoints.put("TestOfTeacher", 0.0f);
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByQualityAxis(mapQuality, listScoreMetricStudent,
				listScoreMetricTeacher, projectName, idModule), listLostPoints);
	}

	/**
	 * Test the calculLostPointsByQualityAxisTest method in the case where one of
	 * quality axis is disabled
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void calculLostPointsByQualityAxisIfOneQualityAxisIsAbsentTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.remove("Comments");
		listLostPoints.remove("Comments");
		listLostPoints.put("TestOfTeacher", 0.0f);
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByQualityAxis(mapQuality, listScoreMetricStudent,
				listScoreMetricTeacher, projectName, idModule), listLostPoints);
	}

	/**
	 * Test the calculLostPointsByRules methods
	 */
	@Test
	public void calculLostPointsByRulesTest() {
		Assert.assertEquals(UtilCalculGrade.calculLostPointsByRules(mapConf, mapIssues), new Float(3.5));
	}

	/**
	 * Test the getAllGrades with no adjustment about the TestOfTeacher qualityAxis
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithNoAdjustmentTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		listAllGrades.put("FinalGrade", 14.0f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	public boolean getAllGradesWithNoAdjustmentTest2()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		listAllGrades.put("FinalGrade", 5.0f);
		if (UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher, mapConf, mapIssues,
				projectName, idModule).get("FinalGrade") == listAllGrades.get("FinalGrade")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Test the getAllGrades with no adjustment about the TestOfTeacher qualityAxis
	 * because it is at the limit with the modification of lost points Comments
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithNoAdjustementLimitTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.put("Comments", new ModelValue(25f, 3f));
		listAllGrades.put("Comments", 3f);
		listAllGrades.put("TestOfTeacher", 0.0f);
		listAllGrades.put("FinalGrade", 12.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule), listAllGrades);
	}

	/**
	 * Test the getAllGrades with adjustment about the TestOfTeacher qualityAxis
	 * with the modification of lost points Comments
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithAdjustementTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.put("Comments", new ModelValue(25f, 5f));
		listAllGrades.put("FinalGrade", 10.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades with no adjustment about the TestOfTeacher qualityAxis
	 * with the modification of TestOfTeacher quality axis value
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithChangeOfTestOfTeacherValueAndNoAdjustementTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.put("TestOfTeacher", new ModelValue(90f));
		mapQuality.put("Comments", new ModelValue(25f, 5f));
		listAllGrades.put("FinalGrade", 10.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades with no adjustment (at the limit) about the
	 * TestOfTeacher qualityAxis with the modification of TestOfTeacher quality axis
	 * value
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithChangeOfTestOfTeacherValueAndNoAdjustementWithLimitTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.put("TestOfTeacher", new ModelValue(90f));
		mapQuality.put("Comments", new ModelValue(25f, 9f));
		listAllGrades.put("FinalGrade", 6.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades with adjustment about the TestOfTeacher qualityAxis
	 * with the modification of TestOfTeacher quality axis value
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithChangeOfTestOfTeacherValueAndAdjustementTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.put("TestOfTeacher", new ModelValue(90f));
		mapQuality.put("Comments", new ModelValue(25f, 10f));
		listAllGrades.put("FinalGrade", new Float(5.5));
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades when one of quality axis is disable
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesTestWithDisableQualityAxisTest() {
		mapQuality.remove("Comments");
		listAllGrades.put("FinalGrade", 15.5f);
		try {
			Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
					mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
		} catch (ClassNotFoundException | SonarqubeDataBaseException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Test the getAllGrades when all quality axis is disable
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithDisableAllQualityAxisTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		mapQuality.remove("Comments");
		mapQuality.remove("TestOfTeacher");
		mapQuality.remove("TestOfStudent");
		mapQuality.remove("Complexity");
		listAllGrades.put("FinalGrade", 16.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssues, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades when there are no issues so no lost points about the
	 * rules
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void getAllGradesWithNoRuleLostPointsTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		listAllGrades.put("FinalGrade", 17.5f);
		Assert.assertEquals(UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher,
				mapConf, mapIssuesEmpty, projectName, idModule).get("FinalGrade"), listAllGrades.get("FinalGrade"));
	}

	/**
	 * Test the getAllGrades when all is perfect, the student has 20 points
	 * 
	 * @throws IOException
	 * @throws SQLException
	 * @throws SonarqubeDataBaseException
	 * @throws ClassNotFoundException
	 */

	@Test
	public void getAllGradesWithPerfectGradeTest()
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		// Filling with 'perfectScore'
		listScoreMetricStudent.put("comment_lines_density", 70f);
		listScoreMetricStudent.put("complexity", 11f);
		listScoreMetricStudent.put("test_success_density", 100f);
		listScoreMetricStudent.put("tests", 4f);
		listScoreMetricStudent.put("test_success_density_teacher", 1f);
		HashMap<String, Integer> mapIssuesEmpty = new HashMap<String, Integer>();
		listAllGrades.put("FinalGrade", 20f); // changer project name par un projet Student nikel
		Assert.assertEquals(
				UtilCalculGrade.getAllGrades(mapQuality, listScoreMetricStudent, listScoreMetricTeacher, mapConf,
						mapIssuesEmpty, projectName, idModule).get("FinalGrade"),
				listAllGrades.get("FinalGrade"));
	}

	/**
	 * After each test, the map is reconditionned
	 */
	@After
	public void clean() {
		listAllGrades.put("FinalGrade", 9f);
		listScoreMetricStudent.put("comment_lines_density", 50f);
		listScoreMetricStudent.put("complexity", 13f);
		listScoreMetricStudent.put("test_success_density", 50f);
		listScoreMetricStudent.put("tests", 4f);
		listScoreMetricStudent.put("test_success_density_teacher", 0.75f);
		mapQuality.put("TestOfTeacher", new ModelValue(50f));
		mapQuality.put("TestOfStudent", new ModelValue(4f, 1f));
		mapQuality.put("Comments", new ModelValue(25f, 1.5f));
		mapQuality.put("Complexity", new ModelValue(20f, 1f));
		listAllGrades.put("Comments", 1f);
		listLostPoints.put("Comments", 1.5f);
		try {
			SonarDataBase.getInstance().deleteTestAndScoreTeacher(99);
		} catch (ClassNotFoundException | SQLException | SonarqubeDataBaseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}