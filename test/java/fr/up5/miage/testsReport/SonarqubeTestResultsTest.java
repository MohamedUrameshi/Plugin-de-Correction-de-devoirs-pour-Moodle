package fr.up5.miage.testsReport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * This class of tests is used to test the UtilJSON class
 */
public class SonarqubeTestResultsTest {

	private SonarqubeTestResults sonarresult;
	/**
	 * This method initializes the listJsonText attribute with different kinds of JSON text replies, the
	 * mapReturn attribute and the listReturn attribute
	 */
	@Before
	public  void init() {

		sonarresult = new SonarqubeTestResults(7,3,1,1);
	}
	/**
	 * This test tests if all the metrics are recovered
	 */
	@Test
	public void gettersTest(){

		Assert.assertEquals(7, sonarresult.getRun());
		Assert.assertEquals(3, sonarresult.getFailure());
		Assert.assertEquals(1, sonarresult.getError());
		Assert.assertEquals(1, sonarresult.getSkypped());
	}
}
