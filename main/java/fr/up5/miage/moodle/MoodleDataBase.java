package fr.up5.miage.moodle;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Public class which recovers informations in Moodle database and make a connection to Moodle database
 */
public class MoodleDataBase{

	/**
	 * Represents the logger for this class
	 */
	private static LoggerConfig logMoodleDB;

	/**
	 * Class name
	 */
	private static final String moodleDataBaseClass = "MoodleDataBase.class";

	/**
	 *  Private attribute that stocks the prefix of tables in Moodle database
	 */
	private static String prefix;

	private Connection conn = null;
	/**
	 * Attribute used to recover the systemConfiguration
	 */
	private SystemConfiguration systemConfiguration;

	public static MoodleDataBase INSTANCE;
	private  ResultSet res;
	public ResultSet getRes() {
		return res;
	}
	public void setRes(ResultSet res) {
		this.res = res;
	}

	private PreparedStatement statementForAssignmentParticipants;
	private PreparedStatement statement;
	private PreparedStatement statementForProjectFileName;
	private PreparedStatement statementForSubmissionFile;
	private PreparedStatement statementForProjectFileHashPath;
	private PreparedStatement statementModName;

	/**
	 * This method is used to make a connection with the sonar database, and browse configuration file to get the id in sonar database
	 * @throws MoodleDataBaseAccessException
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static MoodleDataBase getInstance() throws MoodleDataBaseAccessException, SQLException, IOException, ClassNotFoundException, MoodleDataBaseAccessException {
		if (INSTANCE == null) {
			INSTANCE = new MoodleDataBase();
		}
		return INSTANCE;
	}
	/**
	 * Class constructor
	 * @throws ClassNotFoundException : if the class cannot be found
	 */
	private MoodleDataBase() throws ClassNotFoundException, MoodleDataBaseAccessException, IOException, SQLException {
		this.systemConfiguration = new SystemConfiguration("configurationSystem.properties");
		Class.forName(systemConfiguration.getSystemConfigs().get("DriverJDBC"));

		logMoodleDB = new LoggerConfig(moodleDataBaseClass,Level.INFO,false);
		logMoodleDB.addAppender(App.fileLog, Level.INFO, null);
		prefix = "mdl_";

		String url = getSystemConfiguration().getSystemConfigs().get("moodleDatabaseUrl");
		String login = getSystemConfiguration().getSystemConfigs().get("loginMoodleDB");
		String pass = getSystemConfiguration().getSystemConfigs().get("passwordMoodleDB");
		try {
			conn =  DriverManager.getConnection(url, login, pass);
		} catch (SQLException exp) {
			throw new MoodleDataBaseAccessException("The connection to Moodle database failed for this cause : " + exp.getCause().getMessage());
		}
		System.out.println("Connected in moodle database successfuly");

		String tableUser = prefix + "user";
		String tableUserMappingAssign = prefix + "assign_user_mapping";
		String request = "SELECT " + tableUser +".id, " + tableUser +".username, "
				+ tableUser +".lastname FROM "
				+ tableUser + ", " + tableUserMappingAssign
				+ " WHERE " + tableUserMappingAssign +".assignment=? AND "
				+ tableUser + ".id=" + tableUserMappingAssign +".userid" + " AND  userid=?";
		statementForAssignmentParticipants = conn.prepareStatement(request);

		String tablePart = prefix + "course_modules";

		String request2 = "SELECT " + tablePart + ".instance FROM " + tablePart
				+ " WHERE " + tablePart + ".id=?";
		statement = conn.prepareStatement(request2);

		String tableProj = prefix + "files";
		String request3 = "SELECT source FROM mdl_files f JOIN mdl_context c ON c.id = f.contextid AND c.instanceid = ?  AND userid = ?  AND f.mimetype =?  AND f.filearea =?";
		statementForProjectFileName = conn.prepareStatement(request3);

		String tableForSubm = prefix + "assign_submission";
		String requestForSubm = "SELECT " + tableForSubm + ".id FROM "+ tableForSubm
				+" WHERE " + tableForSubm + ".assignment=? AND " + tableForSubm + ".userid=? AND "
				+ tableForSubm + ".status=?";
		statementForSubmissionFile = conn.prepareStatement(requestForSubm);


		String tableProjectF = prefix + "files";
		String requestForProjej = "SELECT " + tableProjectF + ".contenthash FROM " + tableProjectF + " "
				+ "WHERE " + tableProjectF + ".component=?  AND "
				+ tableProjectF + ".mimetype=? AND "
				+ tableProjectF + ".itemid=?  AND "
				+ tableProjectF + ".userid=? AND "
				+ tableProjectF + ".filesize<>0";
		statementForProjectFileHashPath = conn.prepareStatement(requestForProjej);

		String tableModName = prefix + "assign";
		String requestModName = "SELECT name FROM "+ tableModName+" WHERE id = ?";
		statementModName = conn.prepareStatement(requestModName);
	}

	/**
	 * Getter for prefix attribute
	 * @return the prefix
	 */
	public static String getPrefix() {
		return prefix;
	}

	/**
	 * Setter for prefix attribute
	 * @param prefix the prefix to set
	 */
	public static void setPrefix(String prefix) {
		MoodleDataBase.prefix = prefix;
	}

	/**
	 * @return the system configuration
	 */
	public SystemConfiguration getSystemConfiguration() {
		return systemConfiguration;
	}

	/**
	 * Set the system configuration file
	 * @param systemConfiguration represents the systemConfiguration instance
	 */
	public void setSystemConfiguration(SystemConfiguration systemConfiguration) {
		this.systemConfiguration = systemConfiguration;
	}

	/**
	 * This method is used to make a connection with the Moodle database, and browse configuration file to get the id in Moodle database
	 * @return a connection or an exception
	 * @throws MoodleDataBaseAccessException : if connection to database failed
	 * @throws SQLException
	 */
	public Connection getConnection(){
		return conn;
	}

	/**
	 * This method is used to get the id of assignment
	 * @param idModule : the id of module instance
	 * @return id of assignment
	 * @throws MoodleDataBaseAccessException : : if we can't execute the request in the method
	 */
	public int getRealModuleId(int idModule) throws MoodleDataBaseAccessException {

		int realid=0;
		try {
			statement.setInt(1, idModule);
			res = statement.executeQuery();
			if (!res.next()) {
				logMoodleDB.log(moodleDataBaseClass,null,null,Level.WARN,(Message)new SimpleMessage("The real id of instance of assignment " + idModule + " was not found"),null);
			} else {
				realid = res.getInt(1);
				//System.out.println("realid = "+res.getInt(1));
			}
		}catch (SQLException e) {
			throw new MoodleDataBaseAccessException(e.getMessage());
		}
		return realid;
	}

	/**
	 * This method is used to get the name of module
	 * @param idModule : the id of module instance
	 * @return id of assignment
	 * @throws MoodleDataBaseAccessException : : if we can't execute the request in the method
	 */
	public String getModuleName(int idModule) throws MoodleDataBaseAccessException {
		String modName="";
		try {
			statementModName.setInt(1, idModule);
			res = statementModName.executeQuery();
			if (!res.next()) {
				//logMoodleDB.log(moodleDataBaseClass,null,null,Level.WARN,(Message)new SimpleMessage("The real id of instance of assignment " + idModule + " was not found"),null);
				throw new MoodleDataBaseAccessException("Vieux Cons");

			} else {
				modName = res.getString(1);
				
			}
		}catch (SQLException e) {
			throw new MoodleDataBaseAccessException(e.getMessage());
			
		}
		return modName;
	}	

	public void ndio() 
	{
		System.out.println("NDhidon");
	}

	/**
	 * This method is used to recover the id of all the participants for an assignment
	 * @param idAssignment : the assignment to get the participants
	 * @return a List of Moodle users
	 * @throws MoodleDataBaseAccessException if there is exception
	 */
	public MoodleUser getAssignmentParticipants(int idAssignment, int idUser, int idModule) throws MoodleDataBaseAccessException {
		try
		{
			statementForAssignmentParticipants.setInt(1, idAssignment);
			statementForAssignmentParticipants.setInt(2, idUser);
			ResultSet res2 = statementForAssignmentParticipants.executeQuery();
			// Get informations of the Moodle users
			if (!res2.next()) {
				logMoodleDB.log(moodleDataBaseClass, null, null, Level.WARN,
						(Message) new SimpleMessage("No participant has been found in database"), null);
			} else{
				int idMoodleUser = res2.getInt(1);
				String login =     res2.getString(2);
				String lastname =  res2.getString(3);
				System.out.println("Student info : " + idMoodleUser + "," + login + "," + lastname);
				return new MoodleUser(idMoodleUser, login, lastname, this.getProjectName(idModule, idMoodleUser));
			}
		} catch (SQLException exp) {
			exp.printStackTrace();
			throw new MoodleDataBaseAccessException(exp.getCause().getMessage());
		}

		throw new MoodleDataBaseAccessException("USER NOT FOUND");
	}

	/**
	 * Get the id of a submission in a specific assignment
	 * @param idAssignment : the id of assignment
	 * @param idMoodleUser : the id (not login) of Moodle user in database
	 * @return the id of submission
	 * @throws MoodleDataBaseAccessException : if we can't execute the request in the method
	 */
	private int getSubmissionFileId(int idAssignment, int idMoodleUser) throws MoodleDataBaseAccessException{


		int iditem = 0;

		String statusType = "submitted";

		try {

			statementForSubmissionFile.setInt(1, idAssignment);
			statementForSubmissionFile.setInt(2, idMoodleUser);
			statementForSubmissionFile.setString(3, statusType);

			res = statementForSubmissionFile.executeQuery();
			if (!res.next()) {
				logMoodleDB.log(moodleDataBaseClass,null,null,Level.WARN,(Message)new SimpleMessage("There is no project for user " + idMoodleUser + " in assignment " + idAssignment),null);
			} else {
				iditem = res.getInt(1);
				res.close();
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new MoodleDataBaseAccessException(ex.getMessage());
		}

		return iditem;
	}

	/**
	 * Get the project name (of a ZIP file) of a participant for an assignment
	 * @param idModule : the id of assignment
	 * @param idMoodleUser : the id (not login) of Moodle user in database
	 * @return the project file name
	 * @throws MoodleDataBaseAccessException : if we can't execute the request in the method
	 */
	public String getProjectName(int idModule, int idMoodleUser) throws MoodleDataBaseAccessException {


		String fileArea = "submission_files";
		String mimetype = "application/zip";
		String projectName = "";

		try {

				statementForProjectFileName.setInt(1, idModule);
				statementForProjectFileName.setInt(2, idMoodleUser);
				statementForProjectFileName.setString(3, mimetype);
				statementForProjectFileName.setString(4, fileArea);

				res  = statementForProjectFileName.executeQuery();
				if (!res.next()) {
					return null;
				} else {
					projectName = res.getString(1);
					res.close();
				}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MoodleDataBaseAccessException(e.getMessage());
		}
		return projectName;
	}

	/**
	 * Get path name of a project file of a participant for a specific assignment
	 * @param idAssignment : the id of assignment
	 * @param idMoodleUser : the id (not login) of Moodle user in database
	 * @return the 'hashPath' of the project
	 * @throws MoodleDataBaseAccessException : if we can't execute the request in the method
	 */
	public String getProjectHashPath(int idAssignment, int idMoodleUser) throws MoodleDataBaseAccessException {

		String projectHashPath = "";
		String componentType = "assignsubmission_file";
		String mimetype = "application/zip";

		try{

			int iditem = this.getSubmissionFileId(idAssignment, idMoodleUser);

			if(iditem != 0){
				statementForProjectFileHashPath.setString(1, componentType);
				statementForProjectFileHashPath.setString(2, mimetype);
				statementForProjectFileHashPath.setInt(3, iditem);
				statementForProjectFileHashPath.setInt(4, idMoodleUser);

				res = statementForProjectFileHashPath.executeQuery();
				{
					if (!res.next()) {
						logMoodleDB.log(moodleDataBaseClass,null,null,Level.WARN,(Message)new SimpleMessage("The projet has no hash path name for user " + idMoodleUser + " in assignment " + idAssignment),null);
					} else {
						projectHashPath = res.getString(1);
						res.close();
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MoodleDataBaseAccessException(e.getMessage());
		}
		return projectHashPath;
	}

	public void closeConnexion() throws SQLException {
		if(res != null) {
			this.res.close();
			this.conn.close();
			System.out.println("Moodle connection closed");
		}
	}
}