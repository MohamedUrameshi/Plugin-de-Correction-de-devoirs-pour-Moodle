package fr.up5.miage.sonarqube;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.moodle.MoodleDataBase;
import fr.up5.miage.testsReport.TestRepport;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SonarDataBase {
	public static SonarDataBase INSTANCE;
	private Connection connection;
	private static PreparedStatement pstGetProjectID;
	private static PreparedStatement pstGetSourceID;
	private static PreparedStatement pstGetSourceIDforJavaScript;
	private static PreparedStatement pstGetTestsResultID;
	private static PreparedStatement pstInsertTestResults;
	private static PreparedStatement pstInsertTestDetails;
	private static PreparedStatement pstUpdateTestResults;
	private static PreparedStatement pstDeleteTestDetails;
	private static PreparedStatement pstDeleteTestTeacher;
	private static PreparedStatement pstDeleteTeacherMetrics;
	private static PreparedStatement pstInsertTestTeacher;
	private static PreparedStatement pstTestScore;
	private static PreparedStatement pstInsertGrade;
	private static PreparedStatement pstInsertSonarQualityTeacher;
	private static PreparedStatement pstUpdateSonarQualityTeacher;
	private static PreparedStatement pstSelectSonarQualityTeacher;
	private static PreparedStatement pstInsertTestDetails2;
	private static PreparedStatement pstGetSorceID;
	private static PreparedStatement pstUpdateGrade;
	private static PreparedStatement pstInsertLanguage;
	private static PreparedStatement pstUpdateLanguage;
	private static PreparedStatement pstInsertTestAndScoreTeacher;
	private static PreparedStatement pstDeleteTestAndScoreTeacher;
	private static PreparedStatement pstSelectLanguageModule;
	private ResultSet res;

	/**
	 * This method is used to make a connection with the sonar database, and browse
	 * configuration file to get the id in sonar database
	 *
	 * @throws SonarqubeDataBaseException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static SonarDataBase getInstance()
			throws SonarqubeDataBaseException, SQLException, IOException, ClassNotFoundException {
		if (INSTANCE == null) {
			INSTANCE = new SonarDataBase();
		}
		return INSTANCE;
	}

	private SonarDataBase() throws SonarqubeDataBaseException, SQLException, ClassNotFoundException, IOException {

		SystemConfiguration systemConfiguration = new SystemConfiguration("configurationSystem.properties");
		Class.forName(systemConfiguration.getSystemConfigs().get("DriverJDBC"));
		String url = systemConfiguration.getSystemConfigs().get("sonarDatabaseUrl");
		String login = systemConfiguration.getSystemConfigs().get("loginSonarDB");
		String pass = systemConfiguration.getSystemConfigs().get("passwordSonarDB");
		try {
			connection = DriverManager.getConnection(url, login, pass);
		} catch (SQLException exp) {
			throw new SonarqubeDataBaseException(
					"The connection to Moodle database failed for this cause : " + exp.getCause().getMessage());
		}

		String req = "SELECT id FROM projects p WHERE  p.kee = ?";
		String req1 = "SELECT p1.id FROM projects p1 JOIN projects p2 WHERE p1.name = ? AND p1.project_uuid = p2.project_uuid AND p2.kee =  ?";
		String req2 = "SELECT r.id FROM test_results r JOIN projects p WHERE  p.name = ?";
		String req3 = "INSERT INTO test_results (module_id,project_id, run, faillure, error,  skipped) VALUES  (?,?,?,?,?,?)";
		String req4 = "INSERT INTO test_details (module_id, project_id, source_id,  method,   expected, result) VALUES  (?,?,?,?,?,?)";
		String req5 = "UPDATE  test_results SET run=?, faillure=?, error=?, skipped=? WHERE project_id = ?";
		String req6 = "DELETE FROM  test_details WHERE source_id =?";
		String req7 = "INSERT INTO test_teacher (module_id, source_path, method) VALUES  (?,?,?)";
		String req8 = "SELECT * FROM sonar_quality_teacher WHERE  module_id = ?";
		String req9 = "INSERT INTO grade_history (module_id,project_name,grade,module_name,id_student) VALUES (?,?,?,?,?)";
		String req10 = "SELECT SUM(t2.score) FROM test_teacher t2 JOIN test_details d on t2.method = d.method AND d.module_id=t2.module_id AND d.project_id = ? AND d.module_id=?";
		String req11 = "INSERT INTO sonar_quality_teacher (module_id,key_metric,value) VALUES (?,?,?)";
		String req12 = "UPDATE sonar_quality_teacher SET value=? WHERE module_id=? AND key_metric=?";
		String req13 = "DELETE FROM  test_teacher WHERE module_id =?";
		String req14 = "DELETE FROM  sonar_quality_teacher WHERE module_id =?";
		String req15 = "INSERT INTO test_details (module_id, project_id, source_id,  method,   message) VALUES  (?,?,?,?,?)";
		String req16 = "SELECT id   FROM projects  WHERE  project_uuid = (SELECT  project_uuid FROM projects WHERE kee =? )AND path =?";
		String req17 = "UPDATE grade_history SET grade=? WHERE module_id=? AND id_student=?";
		String req19 = "UPDATE module_languages SET lang=? WHERE module_id=?";
		String req18 = "INSERT INTO module_languages (module_id,lang) VALUES (?,?)";
		String req20 = "INSERT INTO test_teacher (module_id, source_path, method,score) VALUES  (?,?,?,?)";
		String req21="DELETE FROM  test_teacher WHERE module_id =?";
		String req22  = "SELECT lang FROM module_languages WHERE  module_id = ?";
		String req23="SELECT id From projects WHERE name=? AND project_uuid IN (SELECT project_uuid FROM projects WHERE kee=? ) ";
		
		pstGetProjectID = connection.prepareStatement(req);
		pstGetTestsResultID = connection.prepareStatement(req1);
		pstGetSourceID = connection.prepareStatement(req2);
		pstInsertTestResults = connection.prepareStatement(req3);
		pstInsertTestDetails = connection.prepareStatement(req4);
		pstUpdateTestResults = connection.prepareStatement(req5);
		pstDeleteTestDetails = connection.prepareStatement(req6);
		pstInsertTestTeacher = connection.prepareStatement(req7);
		pstTestScore = connection.prepareStatement(req10);
		pstInsertGrade = connection.prepareStatement(req9);
		pstInsertSonarQualityTeacher = connection.prepareStatement(req11);
		pstUpdateSonarQualityTeacher = connection.prepareStatement(req12);
		pstSelectSonarQualityTeacher = connection.prepareStatement(req8);
		pstDeleteTestTeacher = connection.prepareStatement(req13);
		pstDeleteTeacherMetrics = connection.prepareStatement(req14);
		pstInsertTestDetails2 = connection.prepareStatement(req15);
		pstGetSourceID = connection.prepareStatement(req16);
		pstUpdateGrade = connection.prepareStatement(req17);
		pstInsertLanguage = connection.prepareStatement(req18);
		pstUpdateLanguage = connection.prepareStatement(req19);
		pstInsertTestAndScoreTeacher = connection.prepareStatement(req20);
		pstDeleteTestAndScoreTeacher=connection.prepareStatement(req21);
		pstSelectLanguageModule = connection.prepareStatement(req22);
		pstGetSourceIDforJavaScript=connection.prepareStatement(req23);

	}

	/**
	 * @return a connection or an exception
	 */
	public Connection getConnection() {
		return this.connection;
	}

	public int getProjectID(String name) throws SonarqubeDataBaseException {
		int id = 0;
		try {
			pstGetProjectID.setString(1, name);
			res = pstGetProjectID.executeQuery();
			res.next();
			id = res.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (id == 0)
			throw new SonarqubeDataBaseException("the project " + name + " does not exists in sonar database");
		return id;
	}

	public String getLanguage(int moduleID) throws SonarqubeDataBaseException {

        try {
            pstSelectLanguageModule.setInt(1, moduleID);
            res = pstSelectLanguageModule.executeQuery();
            res.next();
            String lang = res.getString(1);
            return lang;
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new SonarqubeDataBaseException("Project with module" + moduleID + "without lang");
    }
	public int getSourceID(String className, String name, String fileType) throws SonarqubeDataBaseException {
		int id = 0;
		try {
			pstGetTestsResultID.setString(1, className + "." + fileType);
			pstGetTestsResultID.setString(2, name);
			res = pstGetTestsResultID.executeQuery();
			res.next();
			id = res.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (id == 0)
			throw new SonarqubeDataBaseException("the project " + name + " does not exists in sonar database");
		return id;
	}

	public int getSourceID2(String kee, String name) throws SonarqubeDataBaseException {
		int id = 0;
		try {
			pstGetSourceID.setString(1, kee);
			pstGetSourceID.setString(2, name);
			res = pstGetSourceID.executeQuery();
			res.next();
			id = res.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (id == 0)
			throw new SonarqubeDataBaseException("the project " + name + " does not exists in sonar database");
		return id;
	}

	public int getSourceIDForJavaScript( String name,String kee) throws SonarqubeDataBaseException {
		int id = 0;
		try {
			pstGetSourceIDforJavaScript.setString(1, name);
			pstGetSourceIDforJavaScript.setString(2, kee);
			res = pstGetSourceIDforJavaScript.executeQuery();
			res.next();
			id = res.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (id == 0)
			throw new SonarqubeDataBaseException("the project " + name + " does not exists in sonar database");
		return id;
	}
	
	public void insertTestResults(Integer... args) throws SonarqubeDataBaseException {
		try {
			pstInsertTestResults.setInt(1, args[0]);
			pstInsertTestResults.setInt(2, args[1]);
			pstInsertTestResults.setInt(3, args[2]);
			pstInsertTestResults.setInt(4, args[3]);
			pstInsertTestResults.setInt(5, args[4]);
			pstInsertTestResults.setInt(6, args[5]);
			pstInsertTestResults.executeUpdate();

		} catch (Exception e) {
			throw new SonarqubeDataBaseException(e.getMessage() + " TESTS RESULT NOT CREATED");
		}
	}

	public void updateTestResults(Integer... args) throws SonarqubeDataBaseException {
		try {
			pstUpdateTestResults.setInt(1, args[0]);
			pstUpdateTestResults.setInt(2, args[1]);
			pstUpdateTestResults.setInt(3, args[2]);
			pstUpdateTestResults.setInt(4, args[3]);
			pstUpdateTestResults.setInt(5, args[4]);
			pstUpdateTestResults.executeUpdate();

		} catch (Exception e) {

			throw new SonarqubeDataBaseException(e.getMessage() + " TESTS RESULT NOT UPDATED");
		}
	}

	public void insertTestTeacher(int moduleID, String... args) throws SonarqubeDataBaseException {
		try {
			pstInsertTestTeacher.setInt(1, moduleID);
			pstInsertTestTeacher.setString(2, args[0]);
			pstInsertTestTeacher.setString(3, args[1]);
			pstInsertTestTeacher.executeUpdate();
		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate entry")) {
				System.out.println("TESTS TEACHER UPDATED");
			} else {
				throw new SonarqubeDataBaseException(e.getMessage() + " TESTS TEACHER NOT CREATED");
			}

		}
	}

	public void insertTestDetails(int moduleId, int projectID, int sourceID, String... args)
			throws SonarqubeDataBaseException {
		try {
			pstInsertTestDetails.setInt(1, moduleId);
			pstInsertTestDetails.setInt(2, projectID);
			pstInsertTestDetails.setInt(3, sourceID);
			pstInsertTestDetails.setString(4, args[0]);
			pstInsertTestDetails.setString(5, args[1]);
			pstInsertTestDetails.setString(6, args[2]);
			pstInsertTestDetails.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new SonarqubeDataBaseException("TESTS RESULT DETAILS NOT CREATED");
		}
	}

	

	public void insertTestDetails2(int moduleId, int projectID, int sourceID, String... args)
			throws SonarqubeDataBaseException {
		try {
			pstInsertTestDetails2.setInt(1, moduleId);
			pstInsertTestDetails2.setInt(2, projectID);
			pstInsertTestDetails2.setInt(3, sourceID);
			pstInsertTestDetails2.setString(4, args[0]);
			pstInsertTestDetails2.setString(5, args[1]);
			pstInsertTestDetails2.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
			throw new SonarqubeDataBaseException("TESTS RESULT DETAILS NOT CREATED");
		}
	}

	public int getTestScore(int projectID, int idModule) {
		int id = 0;
		try {
			pstTestScore.setInt(1, projectID);
			pstTestScore.setInt(2, idModule);
			res = pstTestScore.executeQuery();
			res.next();
			id = res.getInt(1);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return id;
	}

	public void insertLanguage(int moduleID, String language) throws SonarqubeDataBaseException {
		try {
			pstInsertLanguage.setInt(1, moduleID);
			pstInsertLanguage.setString(2, language);
			pstInsertLanguage.executeUpdate();

		} catch (Exception e) {
			if (e.getMessage().contains("Duplicate")) {
				updateLanguage(moduleID, language);
			} else {
				throw new SonarqubeDataBaseException(e.getMessage() + " TESTS RESULT NOT CREATED");
			}
		}
	}

	public void updateLanguage(int moduleID, String language) throws SonarqubeDataBaseException {
		try {
			pstUpdateLanguage.setString(1, language);
			pstUpdateLanguage.setInt(2, moduleID);
			pstUpdateLanguage.executeUpdate();

		} catch (Exception e) {

			throw new SonarqubeDataBaseException(e.getMessage() + " LANGUAGE MODULE NOT UPDATED");
		}

		System.out.println(" LANGUAGE MODULE UPDATED");
	}

	/**
	 * Delete
	 *
	 * @param testRestID
	 * @throws SonarqubeDataBaseException
	 */
	public void deleteTesDetails(int testRestID) throws SonarqubeDataBaseException {
		try {
			pstDeleteTestDetails.setInt(1, testRestID);
			pstDeleteTestDetails.executeUpdate();
		} catch (Exception e) {
			throw new SonarqubeDataBaseException(e.getMessage() + " TESTS DETAILS NOT UPDATED");
		}
	}

	/**
	 * Delete
	 *
	 * @param moduleID
	 * @throws SonarqubeDataBaseException
	 */
	public void deleteTestTeacher(int moduleID) throws SonarqubeDataBaseException {
		try {
			pstDeleteTestTeacher.setInt(1, moduleID);
			pstDeleteTestTeacher.executeUpdate();
		} catch (Exception e) {
			throw new SonarqubeDataBaseException(e.getMessage() + " TESTS DETAILS NOT UPDATED");
		}
	}

	/**
	 * Delete DELETE FOR UPDATE
	 * 
	 * @param moduleID
	 * @throws SonarqubeDataBaseException
	 */
	public void deleteSonarMetrics(int moduleID) throws SonarqubeDataBaseException {
		try {
			pstDeleteTeacherMetrics.setInt(1, moduleID);
			pstDeleteTeacherMetrics.executeUpdate();
		} catch (Exception e) {
			throw new SonarqubeDataBaseException(e.getMessage() + " METRICS TESTS DETAILS NOT UPDATED");
		}
	}

	/**
	 *
	 * @param moduleID
	 * @param projectName
	 * @param finalGrade
	 * @throws SonarqubeDataBaseException
	 */
	public void insertGrade(String nameModule, int moduleID, String projectName, float finalGrade, int idStudent)
			throws SQLException {

		try {
			pstInsertGrade.setInt(1, moduleID);
			pstInsertGrade.setString(2, projectName);
			pstInsertGrade.setFloat(3, finalGrade);
			pstInsertGrade.setString(4, nameModule);
			pstInsertGrade.setInt(5, idStudent);
			pstInsertGrade.executeUpdate();
		} catch (SQLException e) {
			updateGrade(finalGrade, moduleID, idStudent);

		}

	}
	
	/**
	 * 
	 * @param module_id
	 * @param source_path
	 * @param method
	 * @param score
	 */
	public void insertTestAndScoreTeacher(int module_id, String source_path,String method,int score) 
	{
		//module_id, source_path, method,score) VALUES  (?,?,?,?)";
		try {
			pstInsertTestAndScoreTeacher.setInt(1, module_id);
			pstInsertTestAndScoreTeacher.setString(2, source_path);
			pstInsertTestAndScoreTeacher.setString(3, method);
			pstInsertTestAndScoreTeacher.setInt(4, score);
			pstInsertTestAndScoreTeacher.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * 
	 * @param module_id
	 * @throws SQLException 
	 */
	public void deleteTestAndScoreTeacher(int module_id) throws SQLException
	{
		pstDeleteTestAndScoreTeacher.setInt(1, module_id);
		pstDeleteTestAndScoreTeacher.executeUpdate();
		
	}
	public void updateGrade(float finalGrade, int moduleID, int idStudent) throws SQLException {
		pstUpdateGrade.setFloat(1, finalGrade);
		pstUpdateGrade.setInt(2, moduleID);
		pstUpdateGrade.setInt(3, idStudent);
		pstUpdateGrade.executeUpdate();
	}

	/**
	 * Insert the elements necessary to calculate the student's score
	 * 
	 * @param moduleId
	 * @param key_metric
	 * @param value
	 */
	public void insertSonarQualityTeacher(int moduleId, String key_metric, Float value) {
		try {
			pstInsertSonarQualityTeacher.setInt(1, moduleId);
			pstInsertSonarQualityTeacher.setString(2, key_metric);
			pstInsertSonarQualityTeacher.setFloat(3, value);
			pstInsertSonarQualityTeacher.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			updateSonarQualityTeacher(moduleId, key_metric, value);
		}
	}

	/**
	 * Update the elements necessary to calculate the student's score
	 * 
	 * @param moduleId
	 * @param key_metric
	 * @param value
	 */
	public void updateSonarQualityTeacher(int moduleId, String key_metric, Float value) {
		try {
			pstUpdateSonarQualityTeacher.setInt(1, moduleId);
			pstUpdateSonarQualityTeacher.setString(2, key_metric);
			pstUpdateSonarQualityTeacher.setString(3, key_metric);
			pstUpdateSonarQualityTeacher.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public HashMap<String, Float> getTeacherMetrics(int moduleID) throws SonarqubeDataBaseException {
		HashMap<String, Float> teacherMetrics = new HashMap<>();
		try {
			pstSelectSonarQualityTeacher.setInt(1, moduleID);
			res = pstSelectSonarQualityTeacher.executeQuery();
			while (res.next()) {
				teacherMetrics.putIfAbsent(res.getString(3), res.getFloat(4));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (teacherMetrics.size() == 0)
			throw new SonarqubeDataBaseException(
					"the module with id " + moduleID + " does not have metrics in sonar database");
		return teacherMetrics;
	}

	public void closeConnexion() throws SQLException {
		if (res != null) {
			this.res.close();
			this.connection.close();
			System.out.println("Sonar database connection closed");
		}
	}
}