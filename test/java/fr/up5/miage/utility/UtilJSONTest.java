package fr.up5.miage.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonParsingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class of tests is used to test the UtilJSON class
 */
public class UtilJSONTest{

	/*
	 * Static List that contains different text JSON, the unit tests will apply on these different texts
	 */
	private static List<StringBuilder> listJsonText;

	/*
	 * Static HashMap that represents some returns of methods tested
	 */
	private static Map<String,Float> mapReturn;

	/*
	 * Static HashMap that represents some returns of methods tested
	 */
	private static List<String> listReturn;

	/**
	 * This method initializes the listJsonText attribute with different kinds of JSON text replies, the
	 * mapReturn attribute and the listReturn attribute
	 */
	@BeforeClass
	public static void initJsonText(){
		//Initialization of class' attributes
		listJsonText = new ArrayList<StringBuilder>();
		mapReturn = new HashMap<String,Float>();
		listReturn = new ArrayList<String>();

		//Filling the listJsonText attribute
		listJsonText.add(new StringBuilder("{\"component\":{\"id\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qualifier\":\"TRK\",\"measures\":["
				+ "{\"metric\":\"public_undocumented_api\",\"value\":\"1\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},"
				+ "{\"metric\":\"complexity\",\"value\":\"11\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},"
				+ "{\"metric\":\"tests\",\"value\":\"1\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},"
				+ "{\"metric\":\"test_success_density\",\"value\":\"100.0\",\"periods\":[{\"index\":1,\"value\":\"0.0\"},{\"index\":2,\"value\":\"0.0\"},{\"index\":3,\"value\":\"0.0\"}]},"
				+ "{\"metric\":\"public_api\",\"value\":\"4\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]}]}}"));	
		listJsonText.add(new StringBuilder());
		listJsonText.add(null);
		listJsonText.add(new StringBuilder("{\"component\":{\"id\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qualifier\":\"TRK\",\"measures\":[{\"met"
				+ "ric\":\"test\",\"value\":\"\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]}]}}"));
		listJsonText.add(new StringBuilder("{\"profile\":{\"key\":\"java-profile-java-test-99859\",\"name\":\"profile java test\",\"language\":\"java\",\"languageName\":\"Java\",\"isDefault\":false,\"isInherited\":false}}"));
		listJsonText.add(new StringBuilder("Ceci n'est pas du texte en format JSON."));
		listJsonText.add(new StringBuilder("{\"profile\":{\"name\":\"profile java test\",\"language\":\"java\",\"languageName\":\"Java\",\"isDefault\":false,\"isInherited\":false}}"));
	}

	/**
	 * This test tests if all the metrics are recovered
	 */
	@Test 
	public void getValueOfAllMetricsTest(){
		mapReturn.put("public_undocumented_api", 1f);
		mapReturn.put("complexity", 11f);
		mapReturn.put("tests", 1f);
		mapReturn.put("test_success_density", 100.0f);
		mapReturn.put("public_api", 4f);
		Assert.assertEquals(UtilJSON.getValueOfAllMetrics(listJsonText.get(0)), mapReturn);
	}

	/**
	 * This test tests if metrics are recovered even if they have no values
	 */
	@Test
	public void getValueOfAllMetricsWithNoScoreTest(){
		Assert.assertEquals(UtilJSON.getValueOfAllMetrics(listJsonText.get(3)), mapReturn);
	}

	/**
	 * This test verifies that a JsonParsingException is thrown if the received text is empty
	 */
	@Test(expected=JsonParsingException.class)
	public void getValueOfAllMetricsEmptyParamameterTest(){
		UtilJSON.getValueOfAllMetrics(listJsonText.get(1));
	}

	/**
	 *  This test verifies that a NullPointerException is thrown if the received text is a null object
	 */
	@Test(expected=NullPointerException.class)
	public void getValueOfAllMetricsNullTest(){
		UtilJSON.getValueOfAllMetrics(listJsonText.get(2));
	}

	/**
	 *  This test verifies that an empty HashMap is returned if there is no metric in JSON text
	 */
	@Test
	public void getValueOfAllMetricsWithNoMetricTest(){
		Assert.assertEquals(UtilJSON.getValueOfAllMetrics(listJsonText.get(4)), mapReturn);
	}

	/**
	 * This test verifies that all values of a specific field are recovered
	 */
	@Test
	public void getValueOfAllSpecificFieldTest(){
		listReturn.add("public_undocumented_api");
		listReturn.add("complexity");
		listReturn.add("tests");
		listReturn.add("test_success_density");
		listReturn.add("public_api");
		Assert.assertEquals(UtilJSON.getValueOfAllSpecificField(listJsonText.get(0),"metric"), listReturn);
	}

	/**
	 *  This test verifies that an empty HashMap is returned if there is no specific field in JSON text
	 */
	@Test
	public void getValueOfAllSpecificFieldWithNoSpecificFieldTest(){
		listReturn.clear();
		Assert.assertEquals(UtilJSON.getValueOfAllSpecificField(listJsonText.get(4), "metric"), listReturn);
	}

	/**
	 *  This test verifies that a NullPointerException is thrown if the received text is a null object
	 */
	@Test(expected=NullPointerException.class)
	public void getValueOfAllSpecifiedFieldNullTest(){
		UtilJSON.getValueOfAllSpecificField(listJsonText.get(2), "metric");
	}

	/**
	 *  This test verifies that a JsonParsingException is thrown if the received text is not a JSON text
	 */
	@Test(expected=JsonParsingException.class)
	public void getValueOfAllSpecifiedFieldNoJsonTest(){
		UtilJSON.getValueOfAllSpecificField(listJsonText.get(5), "metric");
	}

	/**
	 * Verify that the value of profile id is returned
	 */
	@Test
	public void getIdQualityProfileTest(){
		Assert.assertEquals(UtilJSON.getIdQualityProfile(listJsonText.get(4)), "java-profile-java-test-99859");
	}

	/**
	 *  This test verifies that a JsonParsingException is thrown if the received text is not a JSON text
	 */
	@Test(expected=JsonParsingException.class)
	public void getIdQualityProfileWithNoIdTest(){
		UtilJSON.getIdQualityProfile(listJsonText.get(5));
	}

	/**
	 * This test verifies that a NullPointerException is thrown if the received text is a null object
	 */
	@Test(expected=NullPointerException.class)
	public void getIdQualityProfileNullTest(){
		UtilJSON.getIdQualityProfile(listJsonText.get(2));
	}

	/**
	 * This test verifies that null is returned if there is no quality profile id in JSON text
	 */
	@Test
	public void getIdQualityProfileWithoutIdTest(){
		Assert.assertEquals(UtilJSON.getIdQualityProfile(listJsonText.get(6)), null);
	}

	/**
	 * After each test, the mapReturn is cleaned
	 */
	@After
	public void clearMap(){
		mapReturn.clear();
	}
}
