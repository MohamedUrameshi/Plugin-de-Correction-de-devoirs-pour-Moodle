package fr.up5.miage.moodle;
import static org.junit.Assert.assertEquals;
//ajout sur bitbucket
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Before;
import org.junit.Test;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;

public class MoodleUserTest {
	MoodleUser m =  new MoodleUser(1, "ken", "kensaye", "cadageProf");
	
@Test
/**
 * test du constructeur de la classe MoodleUser
 */
public void moodleUserTest() {
	assertEquals( m,m);

}

@Test
/**
 * test du getter getIdUser de la classe MoodleUser
 */
public void getUserIdTest() {
	assertEquals( m.getIdUser(),m.getIdUser());

}

@Test
/**
 * test du setter setIdUser de la classe MoodleUser
 */
public void setUserIdTest() {
	m.setIdUser(3);
	assertEquals( m.getIdUser(),m.getIdUser());

}
@Test
/**
 * test du getter getLastName de la classe MoodleUser
 */
public void getLastNameTest() {
	
	assertEquals("kensaye",m.getLastName());

}

@Test
/**
 * test du setter setIdUser de la classe MoodleUser
 */
public void setLastNameTest() {
	m.setLastName("Urameshi");
	assertEquals("Urameshi",m.getLastName());

}


@Test
/**
 * test du getter getUserName de la classe MoodleUser
 */
public void getUserNameTest() {
	
	assertEquals( "ken",m.getUserName());

}

@Test
/**
 * test du setter setUserName de la classe MoodleUser
 */
public void setUserNameTest() {
	m.setUserName("Urameshi");
	assertEquals( "Urameshi",m.getUserName());

}

@Test
/**
 * test du getter getProjectName de la classe MoodleUser
 */
public void getProjectNameTest() {
	
	assertEquals( "cadageProf",m.getProjectName());

}

@Test
/**
 * test du setter setProjectName de la classe MoodleUser
 */
public void setProjectNameTest() {
	m.setProjectName("Python");
	assertEquals( "Python",m.getProjectName());

}


@Test
/**
 * test de toString() de la classe MoodleUser
 */
public void toStringTest() {
	String a = m.toString();
	String b = m.toString();
	assertEquals(a, b);

}


}
