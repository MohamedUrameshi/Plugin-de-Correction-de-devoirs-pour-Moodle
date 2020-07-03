package fr.up5.miage.moodle;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import fr.up5.miage.notation.App;
public class MoodleDataBaseAccessExceptionTest {
	private static MoodleDataBase m;
	
	@Before
	public void init() throws ClassNotFoundException, MoodleDataBaseAccessException, SQLException, IOException {
		String analysisName = "Abracadabra";
		LocalDateTime localDate = App.getLocalDateTime();
		String dateLaunch = localDate.getDayOfMonth() + "-" + localDate.getMonthValue() + "-" + localDate.getYear()
				+ " " + localDate.getHour() + "-" + localDate.getMinute() + "-" + localDate.getSecond();
		String nameLog = "Logs" + File.separator + analysisName + " " + dateLaunch + "-" + 99 + ".log";
		App.initLog(analysisName, dateLaunch, 99);
		App.log(dateLaunch, analysisName, nameLog);
		m=MoodleDataBase.getInstance();
		m.ndio();
	}
	/*@Test(expected=MoodleDataBaseAccessException.class)
	public void testingException(){
		try {
			m.getModuleName(998);
		} catch (MoodleDataBaseAccessException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	} */
	
	@Test
	public void whenExceptionThrown_thenExpectationSatisfied() throws MoodleDataBaseAccessException {
	    /*String test = null;
	    test.length(); */
		//System.err.println("nnnnnnnnnnnnnnn");
		assertThrows(MoodleDataBaseAccessException.class, ()->m.getModuleName(777));
	    //throw new MoodleDataBaseAccessException("Vieux cons");
	}
	@Test
	public void ttet() 
	{
		m.ndio();
		assertTrue(true);
	}	
}
//ajout sur bitbucket