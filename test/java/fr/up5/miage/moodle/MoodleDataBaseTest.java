package fr.up5.miage.moodle;
import static org.junit.Assert.*;
//ajout sut bit bucket
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Test;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
public class MoodleDataBaseTest{

@Test
/**
 * test de la méthode getPrefix de classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getPrefixTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	  MoodleDataBase mdb = MoodleDataBase.getInstance();
	  mdb.setSystemConfiguration(new SystemConfiguration("configurationSystem.properties"));
	  System.out.println("le prefix vaut : "+mdb.getPrefix());
	  assertEquals(mdb.getPrefix(), "mdl_");
	
			  

  }

@Test
/**
 * test de la méthode getInstance de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getInstanceTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	assertSame(MoodleDataBase.getInstance(), MoodleDataBase.getInstance());
}

@Test
/**
 * test de la méthode setPrefix de la classe MoodlDatabase
 * @param p change le prefix en p
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void setPrefixTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	MoodleDataBase.getInstance().setPrefix("mdl_");
	assertEquals("mdl_", MoodleDataBase.getInstance().getPrefix());
}
@Test
/**
 * test de la méthode getSystemConfiguration
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getSystemConfigurationTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	MoodleDataBase mdb = MoodleDataBase.getInstance();
	SystemConfiguration sc = new SystemConfiguration("configurationSystem.properties");
	assertEquals(mdb.getSystemConfiguration(), sc);
	
}
@Test
/**
 * test de la méthode setSystemConfiguration
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void setSystemConfigurationTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
    MoodleDataBase.getInstance().setSystemConfiguration(new SystemConfiguration("configurationSystem.properties"));
	SystemConfiguration sc = new SystemConfiguration("configurationSystem.properties");
	assertEquals(MoodleDataBase.INSTANCE.getSystemConfiguration(), sc);
	
}


@Test

public void getConnectionTest() throws Exception {
	Connection conn;
	SystemConfiguration sc = new SystemConfiguration("configurationSystem.properties");
	Class.forName(sc.getSystemConfigs().get("DriverJDBC"));
	String moodleDataBaseClass = "MoodleDataBase.class";

  LoggerConfig logMoodleDB = new LoggerConfig(moodleDataBaseClass,Level.INFO,false);
	logMoodleDB.addAppender(App.fileLog, Level.INFO, null);
	String prefix = "mdl_";

	String url = sc.getSystemConfigs().get("moodleDatabaseUrl");
	String login = sc.getSystemConfigs().get("loginMoodleDB");
	String pass = sc.getSystemConfigs().get("passwordMoodleDB");

		conn =  DriverManager.getConnection(url, login, pass);
		
		//New instance creation for test
		MoodleDataBase mdb = MoodleDataBase.getInstance();
		assertEquals(conn, mdb.getConnection());
	
}

@Test
/**
 * teste getRealModuleId de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getRealModuleIdTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	assertEquals(1, MoodleDataBase.getInstance().getRealModuleId(1));
}

@Test
/**
 * cette méthode teste getModuleName de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getModuleNameTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	assertEquals("devoir1", MoodleDataBase.getInstance().getModuleName(1));
}

@Test
/**
 * test de la méthode getAssignmentParticipants de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getAssignmentParticipantsTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
    MoodleUser m = new MoodleUser(3, "etudiant", "Kensaye", MoodleDataBase.getInstance().getProjectName(1, 3));
	//MoodleDataBase.getInstance().getAssignmentParticipants(1, 3, 1);
	assertEquals(m, MoodleDataBase.getInstance().getAssignmentParticipants(1, 3, 1));
}


@Test
/**
 * test de la méthode getProjectName de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getProjectNameTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	assertSame(MoodleDataBase.getInstance().getProjectName(1, 3), MoodleDataBase.getInstance().getProjectName(1, 3));
}

@Test
/**
 * test de la méthode closeConnexion de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void closeConnexionTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	Connection conn=null;
	if(MoodleDataBase.INSTANCE!=null) {
		MoodleDataBase.INSTANCE.closeConnexion();
	     conn= MoodleDataBase.INSTANCE.getConnection();
	}
	assertEquals(conn, MoodleDataBase.INSTANCE.getRes());
}

@Test
/**
 *  test de la méthode  getProjectHashPath de la classe MoodleDataBase
 * @throws ClassNotFoundException
 * @throws MoodleDataBaseAccessException
 * @throws SQLException
 * @throws IOException
 */
public void getProjectHashPathTest() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
	assertSame(MoodleDataBase.getInstance().getProjectHashPath(1, 3), MoodleDataBase.getInstance().getProjectHashPath(1, 3));
}
}


