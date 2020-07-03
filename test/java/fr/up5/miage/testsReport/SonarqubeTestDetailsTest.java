package fr.up5.miage.testsReport;

import fr.up5.miage.testsReport.SonarqubeTestDetails;
import org.junit.*;

import javax.json.stream.JsonParsingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class of tests is used to test the UtilJSON class
 */
public class SonarqubeTestDetailsTest {

	private SonarqubeTestDetails sonarDetail;
	/**
	 * This method initializes the listJsonText attribute with different kinds of JSON text replies, the
	 * mapReturn attribute and the listReturn attribute
	 */
	@Before
	public  void init() {

		sonarDetail = new SonarqubeTestDetails("Cadrage.java","cadrageDroite","2","3");
	}
	/**
	 * This test tests if all the metrics are recovered
	 */
	@Test
	public void gettersTest(){

		Assert.assertEquals("Cadrage.java", sonarDetail.getClassName());
		Assert.assertEquals("cadrageDroite", sonarDetail.getMethod());
		Assert.assertEquals("2", sonarDetail.getSpected());
		Assert.assertEquals("3", sonarDetail.getResult());
	}
}
