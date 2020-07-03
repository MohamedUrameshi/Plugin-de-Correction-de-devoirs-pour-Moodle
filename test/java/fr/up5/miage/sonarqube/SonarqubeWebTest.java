package fr.up5.miage.sonarqube;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;

/**
 * This class of tests is used to test the SonarqubeWeb class
 */
public class SonarqubeWebTest {

	/**
	 * Before all tests
	 */
	@BeforeClass
	public static void initLog(){
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
	}

	/**
	 * Determine some functionalities for unit tests
	 */
	@Rule
	//The MockServerRule starts MockServer on a free port before the tests run and stops MockServer 
	//after all tests have completed. Then A MockServerClient is assigned to any field in the unit test
	public MockServerRule mockServerRule = new MockServerRule(this);
	private int port;
	private MockServerClient mockServerClient = new MockServerClient("localhost",(port=mockServerRule.getPort()));


	/**
	 * Before each init() test will be launched
	 * @throws IOException if there are file creation or file reading problems
	 */
	@Before
	public void init() throws IOException{

		//For each test the mockServerCleint change its port, so for each test, we need to modify the system files
		File fileWithHttpUrl = new File("systemConfigurationTestHttp.properties");
		File fileWithHttpsUrl = new File ("systemConfigurationTestHttps.properties");
		File fileWithBadUrl = new File ("systemConfigurationTestBadUrl.properties");
		File fileWithMalformedUrl = new File ("systemConfigurationMalformedUrl.properties");
		try (FileWriter out1 = new FileWriter(fileWithHttpUrl); FileWriter out2 = new FileWriter(fileWithHttpsUrl);
				FileWriter out3 = new FileWriter(fileWithBadUrl);FileWriter out4 = new FileWriter(fileWithMalformedUrl)){
			out1.write("serverSonarUrl=http://localhost:"+this.port+"\nloginSonar=admin\npasswordSonar=admin");
			out2.write("serverSonarUrl=https://localhost:"+this.port+"\nloginSonar=admin\npasswordSonar=admin");
			out3.write("serverSonarUrl=https://local:"+this.port+"\nloginSonar=admin\npasswordSonar=admin");
			out4.write("serverSonarUrl=hppt://localhost:"+this.port+"\nloginSonar=admin\npasswordSonar=admin");
		}
	}


	/**
	 * Test in case of URL that does not answer. The URL is probably badly written
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test(expected = IOException.class) 
	public void sendRequestScoreMetricTestWithBadUrlTest() throws IOException, SonarServerHttpErrorException{
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestBadUrl.properties");
		SonarqubeWeb sonarHttp = new SonarqubeWeb(sys);
		HashMap<String,String> map2 = new HashMap<String,String>();
		map2.put("complexity", "11");
		map2.put("public_api", "4");
		sonarHttp.sendRequestScoreMetrics("upd.mll5u2o:dupond001.iy001.cadrage");
	}

	/**
	 * Test if the server is accessible but no resources of the server corresponds to the request
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test(expected = SonarServerHttpErrorException.class) 
	public void sendRequestScoreMetricWithNoRessourcesFoundTest() throws IOException, SonarServerHttpErrorException{
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.sendRequestScoreMetrics("upd.mll5u2o:dupond001.iy001.cadrage");
	}

	/**
	 * Test in case of malformed URL, like "hppt//..."
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test(expected = IOException.class)
	public void sendRequestScoreMetricWithMalformedUrlTest() throws IOException, SonarServerHttpErrorException{
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestMalformedUrl.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.sendRequestScoreMetrics("upd.mll5u2o:dupond001.iy001.cadrage");
	}

	/**
	 * Test the recovery of all metrics via the HTTP protocol
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void sendRequestScoreMetricWithHttpTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> listForMockServer = new ArrayList<String>();
		listForMockServer.add("componentKey=upd.mll5u2o:dupond001.iy001.cadrage");
		listForMockServer.add("metricKeys=comment_lines_density,complexity,tests,test_success_density");
		createExpectation(createHttpGetRequest("/api/measures/component",listForMockServer),createHttpResponse("200","{\"component\":{\"id\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qualifier\":\"TRK\",\"measures\":["+
				"{\"metric\":\"complexity\",\"value\":\"11\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},"+
				"{\"metric\":\"tests\",\"value\":\"4\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"index\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]}]}}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,Float> map = new HashMap<String,Float>();
		map.put("complexity", new Float(11));
		map.put("tests", new Float(4));
		Assert.assertEquals(sonar.sendRequestScoreMetrics("upd.mll5u2o:dupond001.iy001.cadrage"), map);
	}


	/**
	 * Test the profile creation with the HTTP and HTTPS protocol.
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible.
	 */
	@Test
	public void createProfileWithHttpTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> listForMockServer = new ArrayList<String>();
		listForMockServer.add("profileName=profile java test");
		listForMockServer.add("language=java");
		createExpectation(createHttpPostRequest("/api/qualityprofiles/create",listForMockServer),(
				createHttpResponse("200","{\"profile\":{\"key\":\"java-profile-java-test-86266\",\"name\":\"profile java test\",\"language\":\"java\",\"languageName\":\"Java\",\"isDefault\":false,\"isInherited\":false}}")));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(String.valueOf(sonar.createProfile("profile java test")), "java-profile-java-test-86266");
		sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(String.valueOf(sonar.createProfile("profile java test")), "java-profile-java-test-86266");
	}


	/**
	 * Test the deletion of a profile with the HTTP protocol
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test
	public void deleteProfileWithHttpTest() throws IOException, SonarServerHttpErrorException{
		ArrayList<String> listForMockServer = new ArrayList<String>();
		listForMockServer.add("profileName=profile java test");
		listForMockServer.add("language=java");
		createExpectation(createHttpPostRequest("/api/qualityprofiles/delete",listForMockServer), createHttpResponse("200",""));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.deleteProfile("profile java test");
	}

	/**
	 * Test the deletion of a profile with the HTTP protocol but the profile to delete does not exist
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test(expected = SonarServerHttpErrorException.class)
	public void deleteProfileButNoPorfileToDeleteTest() throws IOException, SonarServerHttpErrorException{
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.deleteProfile("profile that does not exist");
	}

	/**
	 * Test the activation of a rule with HTTP protocol
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test
	public void activateRuleTest() throws IOException, SonarServerHttpErrorException{
		ArrayList<String> listForMockServer = new ArrayList<String>();
		listForMockServer.add("profile_key=java-profile-java-test-86266");
		listForMockServer.add("rule_key=squid:S2998");
		createExpectation(createHttpPostRequest("/api.qualityprofiles/activate_rule",listForMockServer), createHttpResponse("200",""));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.activateRule("squid:S2998", "java-profile-java-test-86266");

	}

	/**
	 * Test the activation of a rule with HTTP protocol but the rule does not exist
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test(expected = SonarServerHttpErrorException.class)
	public void activateRuleButRuleNoExistTest() throws IOException, SonarServerHttpErrorException{
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.activateRule("squid:S2548", "java-profile-java-test-86266");
	}

	/**
	 * Test the version of the server in case the version is accepted
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerException if the server returns a HTTP error
	 */
	@Test
	public void getVersionGoodWithHttpTest() throws IOException, SonarServerException{
		createExpectation(createHttpGetRequest("/api/server/version"),(
				createHttpResponse("200", "6.3")));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(String.valueOf(sonar.getVersion()), "6.3");
		sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(String.valueOf(sonar.getVersion()), "6.3");

	}

	
	/**
	 * (Currently unused since the version restriction was removed)
	 * Test the version of the server in case the version is not accepted
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerException if the server returns a HTTP error
	 */
	/*@Test(expected=SonarServerVersionException.class)
	public void getVersionBadTest() throws IOException, SonarServerException{
		createExpectation(createHttpGetRequest("/api/server/version"),(
				createHttpResponse("200", "3.2")));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		sonar.getVersion();
	}*/


	/**
	 * Test the recovery of project issues in case the issues do not concern the same rule
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void getIssuesProjectWithTwoIssuesButDifferentRuleTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=projectName");
		mapForMockServer.add("language=java");
		mapForMockServer.add("resolved=false");
		mapForMockServer.add("ps=500");
		createExpectation(createHttpGetRequest("/api/issues/search", mapForMockServer), createHttpResponse("200",
				"{\"total\":3,\"p\":1,\"ps\":500,\"paging\":{\"pageIndex\":1,\"pageSi"
						+"ze\":500,\"total\":3},\"issues\":[{\"key\":\"AVoNxwQT1C3xhl5Ik9Mv\",\"ru"
						+"le\":\"squid:S106\",\"severity\":\"MAJOR\",\"component\":\"upd.mll5u2o:du"
						+"pond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupond004/iy004/cadrage/M"
						+"ain.java\",\"componentId\":17,\"project\":\"upd.mll5u2o:dupond004.iy004.ca"
						+"drage\",\"line\":11,\"textRange\":{\"startLine\":11,\"endLine\":11,\"start"
						+"Offset\":5,\"endOffset\":15},\"flows\":[],\"status\":\"OPEN\",\"message\":\"Re"
						+"place this usage of System.out or System.err by a logger.\",\"debt\":\"10m"
						+"in\",\"author\":\"\",\"tags\":[\"bad-practice\"],\"creationDate\":\"2017-02"
						+"-05T11:17:07+0100\",\"updateDate\":\"2017-02-05T11:17:07+0100\"},{\"key\":\"AV"
						+"oNxwQT1C3xhl5Ik9Mx\",\"rule\":\"squid:S1888\",\"severity\":\"MAJOR\",\"comp"
						+"onent\":\"upd.mll5u2o:dupond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupon"
						+"d004/iy004/cadrage/Main.java\",\"componentId\":17,\"project\":\"upd.mll5u2o:dupon"
						+"d004.iy004.cadrage\",\"line\":7,\"textRange\":{\"startLine\":7,\"endLine\":7,\"st"
						+"artOffset\":13,\"endOffset\":17},\"flows\":[],\"status\":\"OPEN\",\"message\":\"A"
						+"dd a private constructor to hide the implicit public one.\",\"debt\":\"30min\",\"auth"
						+"or\":\"\",\"tags\":[\"design\"],\"creationDate\":\"2017-02-05T11:17:07+0100\",\"up"
						+"dateDate\":\"2017-02-05T11:17:07+0100\"}],\"components\":[{\"id\":17,\"key\":\"up"
						+"d.mll5u2o:dupond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupond004/iy004/cadrag"
						+"e/Main.java\",\"uuid\":\"AVoNxwOv1C3xhl5Ik9Mq\",\"enabled\":true,\"qualifier\":\"FI"
						+"L\",\"name\":\"Main.java\",\"longName\":\"src/main/java/upd/mll5u2o/dupond004/iy0"
						+"04/cadrage/Main.java\",\"path\":\"src/main/java/upd/mll5u2o/dupond004/iy004/cadrage/Mai"
						+"n.java\",\"projectId\":14,\"subProjectId\":14},{\"id\":14,\"key\":\"upd.mll5u2o:dupo"
						+"nd004.iy004.cadrage\",\"uuid\":\"AVoNxv4B1C3xhl5Ik9Mk\",\"enabled\":true,\"qualifi"
						+"er\":\"TRK\",\"name\":\"cadrage\",\"longName\":\"cadrage\"}]}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		map.put("squid:S106", new Integer("1"));
		map.put("squid:S1888", new Integer("1"));
		Assert.assertEquals(sonar.getIssuesProject("projectName"), map);
	}

	/**
	 * Test the recovery of project issues in case the issues concern the same rule
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void getIssuesProjectWithTwoIssuesButSameRuleTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=projectName");
		mapForMockServer.add("language=java");
		mapForMockServer.add("resolved=false");
		mapForMockServer.add("ps=500");
		createExpectation(createHttpGetRequest("/api/issues/search", mapForMockServer), createHttpResponse("200",
				"{\"total\":3,\"p\":1,\"ps\":500,\"paging\":{\"pageIndex\":1,\"pageSi"
						+"ze\":500,\"total\":3},\"issues\":[{\"key\":\"AVoNxwQT1C3xhl5Ik9Mv\",\"ru"
						+"le\":\"squid:S106\",\"severity\":\"MAJOR\",\"component\":\"upd.mll5u2o:du"
						+"pond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupond004/iy004/cadrage/M"
						+"ain.java\",\"componentId\":17,\"project\":\"upd.mll5u2o:dupond004.iy004.ca"
						+"drage\",\"line\":11,\"textRange\":{\"startLine\":11,\"endLine\":11,\"start"
						+"Offset\":5,\"endOffset\":15},\"flows\":[],\"status\":\"OPEN\",\"message\":\"Re"
						+"place this usage of System.out or System.err by a logger.\",\"debt\":\"10m"
						+"in\",\"author\":\"\",\"tags\":[\"bad-practice\"],\"creationDate\":\"2017-02"
						+"-05T11:17:07+0100\",\"updateDate\":\"2017-02-05T11:17:07+0100\"},{\"key\":\"AV"
						+"oNxwQT1C3xhl5Ik9Mx\",\"rule\":\"squid:S106\",\"severity\":\"MAJOR\",\"comp"
						+"onent\":\"upd.mll5u2o:dupond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupon"
						+"d004/iy004/cadrage/Main.java\",\"componentId\":17,\"project\":\"upd.mll5u2o:dupon"
						+"d004.iy004.cadrage\",\"line\":7,\"textRange\":{\"startLine\":7,\"endLine\":7,\"st"
						+"artOffset\":13,\"endOffset\":17},\"flows\":[],\"status\":\"OPEN\",\"message\":\"A"
						+"dd a private constructor to hide the implicit public one.\",\"debt\":\"30min\",\"auth"
						+"or\":\"\",\"tags\":[\"design\"],\"creationDate\":\"2017-02-05T11:17:07+0100\",\"up"
						+"dateDate\":\"2017-02-05T11:17:07+0100\"}],\"components\":[{\"id\":17,\"key\":\"up"
						+"d.mll5u2o:dupond004.iy004.cadrage:src/main/java/upd/mll5u2o/dupond004/iy004/cadrag"
						+"e/Main.java\",\"uuid\":\"AVoNxwOv1C3xhl5Ik9Mq\",\"enabled\":true,\"qualifier\":\"FI"
						+"L\",\"name\":\"Main.java\",\"longName\":\"src/main/java/upd/mll5u2o/dupond004/iy0"
						+"04/cadrage/Main.java\",\"path\":\"src/main/java/upd/mll5u2o/dupond004/iy004/cadrage/Mai"
						+"n.java\",\"projectId\":14,\"subProjectId\":14},{\"id\":14,\"key\":\"upd.mll5u2o:dupo"
						+"nd004.iy004.cadrage\",\"uuid\":\"AVoNxv4B1C3xhl5Ik9Mk\",\"enabled\":true,\"qualifi"
						+"er\":\"TRK\",\"name\":\"cadrage\",\"longName\":\"cadrage\"}]}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		map.put("squid:S106", new Integer("2"));
		Assert.assertEquals(sonar.getIssuesProject("projectName"), map);
	}

	/**
	 * Test the recovery of project issues in case there is no issue
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void getIssuesProjectWithNoIssuesTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=projectName");
		mapForMockServer.add("language=java");
		mapForMockServer.add("resolved=false");
		mapForMockServer.add("ps=500");
		createExpectation(createHttpGetRequest("/api/issues/search", mapForMockServer), createHttpResponse("200",
				"{}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		Assert.assertEquals(sonar.getIssuesProject("projectName"), map);
	}

	/**
	 * Test the recovery of the score success test metric if every metric is present
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void sendRequestScoreSuccessMetricTestNotNullTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=upd.mll5u2o:dupond001.iy001.cadrage");
		mapForMockServer.add("metricKeys=tests,test_success_density");
		createExpectation(createHttpGetRequest("/api/measures/component", mapForMockServer), createHttpResponse("200","{\"component\":{\"i"
				+ "d\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qua"
				+ "lifier\":\"TRK\",\"measures\":[{\"metric\":\"tests\",\"value\":\"4\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"i"
				+ "ndex\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},{\"metric\":\"test_success_density\",\"val"
				+ "ue\":\"50.0\",\"periods\":[{\"index\":1,\"value\":\"0.0\"},{\"index\":2,\"value\":\"0.0\"},{\"index\":3,\"value\":\"0.0\"}]}]}}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(sonar.sendRequestScoreSuccessTestMetric("upd.mll5u2o:dupond001.iy001.cadrage", 8), new Float(0.25));
	}

	/**
	 * Test the recovery of the score success test metric if one or both metrics are absent
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void sendRequestScoreSuccessTestMetricNullIfOneAbsentTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=upd.mll5u2o:dupond001.iy001.cadrage");
		mapForMockServer.add("metricKeys=tests,test_success_density");
		createExpectation(createHttpGetRequest("/api/measures/component", mapForMockServer), createHttpResponse("200","{\"component\":{\"i"
				+ "d\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qua"
				+ "lifier\":\"TRK\",\"measures\":[{\"metric\":\"tests\",\"value\":\"4\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"i"
				+ "ndex\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]}]}}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(sonar.sendRequestScoreSuccessTestMetric("upd.mll5u2o:dupond001.iy001.cadrage", 8), new Float(0));
	}

	/**
	 * Test the recovery of the score success test metric if one or both metrics have no values
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void sendRequestScoreSuccessTestMetricNullIfOneEqualNothingTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=upd.mll5u2o:dupond001.iy001.cadrage");
		mapForMockServer.add("metricKeys=tests,test_success_density");
		createExpectation(createHttpGetRequest("/api/measures/component", mapForMockServer), createHttpResponse("200","{\"component\":{\"i"
				+ "d\":\"AVoNxE7e1C3xhl5Ik9L8\",\"key\":\"upd.mll5u2o:dupond001.iy001.cadrage\",\"name\":\"cadrage\",\"description\":\"\",\"qua"
				+ "lifier\":\"TRK\",\"measures\":[{\"metric\":\"tests\",\"value\":\"4\",\"periods\":[{\"index\":1,\"value\":\"0\"},{\"i"
				+ "ndex\":2,\"value\":\"0\"},{\"index\":3,\"value\":\"0\"}]},{\"metric\":\"test_success_density\",\"val"
				+ "ue\":\"\",\"periods\":[{\"index\":1,\"value\":\"0.0\"},{\"index\":2,\"value\":\"0.0\"},{\"index\":3,\"value\":\"0.0\"}]}]}}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(sonar.sendRequestScoreSuccessTestMetric("upd.mll5u2o:dupond001.iy001.cadrage", 8), new Float(0));
	}

	/**
	 * Test the recovery of the score success test metric in case of no metric is returned
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void sendRequestScoreSuccessTestMetricWithNoValueTest() throws SonarServerHttpErrorException, IOException{
		ArrayList<String> mapForMockServer = new ArrayList<String>();
		mapForMockServer.add("componentKey=upd.mll5u2o:dupond001.iy001.cadrage");
		mapForMockServer.add("metricKeys=test_success_density");
		createExpectation(createHttpGetRequest("/api/measures/component", mapForMockServer), createHttpResponse("200","{\"component\":{\"id\":\"AVoNx"
				+ "rPk1C3xhl5Ik9MY\",\"key\":\"upd.mll5u2o:dupond003.iy003.cadrage\",\"name\":\"cadrage\",\"descr"
				+ "iption\":\"\",\"qualifier\":\"TRK\",\"measures\":[]}}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,String> map = new HashMap<String, String>();
		Assert.assertEquals(sonar.sendRequestScoreMetrics("upd.mll5u2o:dupond001.iy001.cadrage"), map);
	}


	/**
	 * Test the recovery of the id of a quality profile if it is absent
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test
	public void getIdQualityProfileAbsentTest() throws IOException, SonarServerHttpErrorException{
		createExpectation(createHttpGetRequest("/api/qualityprofiles/search"), createHttpResponse("200","{\"profi"
				+ "les\":[{\"key\":\"java-profilefornotationautomated-81596\",\"name\":\"ProfileForNotationAutomated\",\"langu"
				+ "age\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":false,\"activeRuleCount\":7,\"active"
				+ "DeprecatedRuleCount\":0,\"projectCount\":0,\"rulesUpdatedAt\":\"2017-03-25T15:51:45+0000\",\"lastUsed\":\"2017-03-2"
				+ "5T16:51:51+0100\",\"userUpdatedAt\":\"2017-03-25T16:51:45+0100\"},{\"key\":\"java-sonar-way-06261\",\"name\":\"Sona"
				+ "r way\",\"language\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":true,\"activeRuleCount\":27"
				+ "7,\"activeDeprecatedRuleCount\":0,\"rulesUpdatedAt\":\"2017-03-20T20:24:39+0000\",\"lastUsed\":\"2017-03-21T10:23:41+0100\"}]}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertFalse(sonar.getIdQualityProfile("NoExist"));
	}


	/**
	 * Test the recovery of the id of a quality profile if it is present
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 */
	@Test
	public void getIdQualityProfilePresentTest() throws IOException, SonarServerHttpErrorException{
		createExpectation(createHttpGetRequest("/api/qualityprofiles/search"), createHttpResponse("200","{\"profi"
				+ "les\":[{\"key\":\"java-profilefornotationautomated-81596\",\"name\":\"ProfileForNotationAutomated\",\"langu"
				+ "age\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":false,\"activeRuleCount\":7,\"active"
				+ "DeprecatedRuleCount\":0,\"projectCount\":0,\"rulesUpdatedAt\":\"2017-03-25T15:51:45+0000\",\"lastUsed\":\"2017-03-2"
				+ "5T16:51:51+0100\",\"userUpdatedAt\":\"2017-03-25T16:51:45+0100\"},{\"key\":\"java-sonar-way-06261\",\"name\":\"Sona"
				+ "r way\",\"language\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":true,\"activeRuleCount\":27"
				+ "7,\"activeDeprecatedRuleCount\":0,\"rulesUpdatedAt\":\"2017-03-20T20:24:39+0000\",\"lastUsed\":\"2017-03-21T10:23:41+0100\"}]}"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertTrue(sonar.getIdQualityProfile("ProfileForNotationAutomated"));
	}


	/**
	 * Test the recovery of the queue analysis report state of the Sonarqube server in case where it is true
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void getQueueAnalysisReportsCaseTrueTest() throws SonarServerHttpErrorException, IOException{
		createExpectation(createHttpGetRequest("/api/analysis_reports/is_queue_empty"), createHttpResponse("200","true"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(sonar.getQueueAnalysisReports(),"true");
	}


	/**
	 * Test the recovery of the queue analysis report state of the Sonarqube server in case where it is false
	 * @throws SonarServerHttpErrorException if the server returns a HTTP error
	 * @throws IOException if the server is not accessible
	 */
	@Test
	public void getQueueAnalysisReportsCaseFalseTest() throws SonarServerHttpErrorException, IOException{
		createExpectation(createHttpGetRequest("/api/analysis_reports/is_queue_empty"), createHttpResponse("200","false"));
		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		Assert.assertEquals(sonar.getQueueAnalysisReports(), "false");
	}

	/**
	 * Create a HttpRequest with GET method
	 * @param path the path of the resource like "/home"
	 * @return an instance of HttpRequest
	 */
	public HttpRequest createHttpGetRequest(String path){
		return HttpRequest.request().withMethod("GET").withPath(path);
	}

	/**
	 * Create a HttpRequest with POSTGET resource like "/home"
	 * @param path of the resource on server
	 * @param queryStringParameter a list that contains the parameters of the query and their values
	 * @return an instance of HttpRequest.
	 */
	public HttpRequest createHttpGetRequest(String path, ArrayList<String> queryStringParameter){
		HttpRequest requestExpected = createHttpGetRequest(path);
		for (String parameter : queryStringParameter){
			requestExpected.withQueryStringParameter(parameter);
		}
		return requestExpected;
	}

	/**
	 * Create a HttpRequest with POST method
	 * @param path the path of the resource like "/home"
	 * @param queryStringParameter a list that contains the parameters of the query and their values
	 * @return an instance of HttpRequest
	 * @throws UnsupportedEncodingException if UTF-8 is not supported
	 */
	public HttpRequest createHttpPostRequest(String path, ArrayList<String> queryStringParameter) throws UnsupportedEncodingException{
		HttpRequest requestExpected = HttpRequest.request().withMethod("POST").withPath(path).withHeader("Authorization", "Basic "+Base64.getEncoder().encodeToString("admin:admin".getBytes()));
		String body = new String();
		for (int i = 0 ; i < queryStringParameter.size() ; i++){
			body += urlEncodeParameter(queryStringParameter.get(i));
			if (i != queryStringParameter.size()-1){
				body += "&";
			}
		}
		requestExpected.withBody(body);
		return requestExpected;
	}

	/**
	 * Create a response
	 * @param statusCodeResponse the status code HTTP of the response
	 * @param bodyResponse the content of the response
	 * @return an instance of HttpResponse
	 */
	public HttpResponse createHttpResponse(String statusCodeResponse, String bodyResponse){
		return HttpResponse.response().withStatusCode(Integer.valueOf(statusCodeResponse)).withBody(bodyResponse);
	}

	/**
	 * Create of an expectation for the mockServer
	 * @param request the request expected
	 * @param response the response returned
	 */
	public void createExpectation(HttpRequest request, HttpResponse response){
		mockServerClient.when(request).respond(response);
	}

	/**
	 * This method encodes a string in UTF-8
	 * @param toEncode the string that must be encoded
	 * @return an encoded String
	 * @throws UnsupportedEncodingException
	 */
	private String urlEncode(String toEncode) throws UnsupportedEncodingException{
		return URLEncoder.encode(toEncode, "UTF-8");
	}

	/**
	 * This method encodes a String like parameter "xxxx=xxxxx" in UTF-8
	 * @param parameter is the parameter to encode
	 * @return an encoded String
	 * @throws UnsupportedEncodingException if the character encoding is not supported
	 */
	private String urlEncodeParameter(String parameter) throws UnsupportedEncodingException{
		int equal = parameter.indexOf("=");
		return this.urlEncode(parameter.substring(0,equal))+"="+this.urlEncode(parameter.substring(equal+1));
	}

	/**
	 * After the last tests, the files to which the unit tests have been used are deleted
	 */
	@AfterClass
	public static void deletionFile(){
		File fileWithHttpUrl = new File("systemConfigurationTestHttp.properties");
		File fileWithHttpsUrl = new File ("systemConfigurationTestHttps.properties");
		File fileWithBadUrl = new File ("systemConfigurationTestBadUrl.properties");
		File fileWithMalformedUrl = new File ("systemConfigurationMalformedUrl.properties");
		fileWithHttpUrl.delete();
		fileWithHttpsUrl.delete();
		fileWithBadUrl.delete();
		fileWithMalformedUrl.delete();
		
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}
}
