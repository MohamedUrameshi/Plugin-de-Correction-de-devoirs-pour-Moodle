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

import fr.up5.miage.configuration.ModelValue;
import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;

/**
 * This class of tests is used to test the QualityProfile class.
 */
public class QualityProfileTest{
	
	/**
	 * Before all tests
	 */
	@BeforeClass
	public static void initLog(){
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
	}
	
	@Rule
	//The MockServerRule starts MockServer on a free port before the any test runs and stops MockServer 
	//after all tests have completed. A MockServerClient is then assigned to any field in the unit test.
	public MockServerRule mockServerRule = new MockServerRule(this);
	private int port;
	private MockServerClient mockServerClient = new MockServerClient("localhost",(port=mockServerRule.getPort()));
	
	/**
	 * Before each init() test will be launched
	 * @throws IOException if there is a problem during a file creation or a file reading
	 */
	@Before
	public void init() throws IOException{
		File fileWithHttpUrl = new File("systemConfigurationTestHttp.properties");
		try (FileWriter out1 = new FileWriter(fileWithHttpUrl)){
			out1.write("serverSonarUrl=http://localhost:"+this.port+"\nloginSonar=admin\npasswordSonar=admin");
		}
	}

	/**
	 * Tests the creation of a quality profile and the activation of two rules on this profile. Also test the getters. In the case 
	 * where the quality profile with the same name is not already present on the Sonarqube server
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns an HTTP error
	 */
	@Test
	public void qualityProfileConstructorIfNoAlreadyQualityProfileTest() throws IOException, SonarServerHttpErrorException{
		
		createExpectation(createHttpGetRequest("/api/qualityprofiles/search"), createHttpResponse("200","{\"profi"
				+ "les\":[{\"key\":\"java-profilefornotationautomated-81596\",\"name\":\"ProfileForNotationAutomated\",\"langu"
				+ "age\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":false,\"activeRuleCount\":7,\"active"
				+ "DeprecatedRuleCount\":0,\"projectCount\":0,\"rulesUpdatedAt\":\"2017-03-25T15:51:45+0000\",\"lastUsed\":\"2017-03-2"
				+ "5T16:51:51+0100\",\"userUpdatedAt\":\"2017-03-25T16:51:45+0100\"},{\"key\":\"java-sonar-way-06261\",\"name\":\"Sona"
				+ "r way\",\"language\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":true,\"activeRuleCount\":27"
				+ "7,\"activeDeprecatedRuleCount\":0,\"rulesUpdatedAt\":\"2017-03-20T20:24:39+0000\",\"lastUsed\":\"2017-03-21T10:23:41+0100\"}]}"));
		
		ArrayList<String> listForMockServer = new ArrayList<String>();
		listForMockServer.add("profileName=profile java test");
		listForMockServer.add("language=java");
		createExpectation(createHttpPostRequest("/api/qualityprofiles/create", listForMockServer),(
				createHttpResponse("200","{\"profile\":{\"key\":\"java-profile-java-test-86266\",\"name\":\"profile java"
						+ " test\",\"language\":\"java\",\"languageName\":\"Java\",\"isDefault\":false,\"isInherited\":false}}")));
		listForMockServer.clear();
		listForMockServer.add("profile_key=java-profile-java-test-86266");
		listForMockServer.add("rule_key=squid:S2998");
		createExpectation(createHttpPostRequest("/api.qualityprofiles/activate_rule", listForMockServer),createHttpResponse("200",""));
		listForMockServer.clear();
		listForMockServer.add("profile_key=java-profile-java-test-86266");
		listForMockServer.add("rule_key=squid:S3448");
		createExpectation(createHttpPostRequest("/api.qualityprofiles/activate_rule", listForMockServer),createHttpResponse("200",""));

		SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
		SonarqubeWeb sonar = new SonarqubeWeb(sys);
		HashMap<String,ModelValue> mapRuleToActivate = new HashMap<String,ModelValue>();
		mapRuleToActivate.put("squid:S2998", new ModelValue(new Float(5), new Float(4), new Float(9)));
		mapRuleToActivate.put("squid:S2998", new ModelValue(new Float(3), new Float(2), new Float(1)));
		QualityProfile qualityProfileTest = new QualityProfile("profile java test",mapRuleToActivate,sonar);
		qualityProfileTest.configurationRules();
		Assert.assertEquals(qualityProfileTest.getName(), "profile java test");
		Assert.assertEquals(qualityProfileTest.getProfileKey(), "java-profile-java-test-86266");
	}

	/**
	 * Tests the creation of a quality profile and the activation of two rules on this profile. Also test the getters. In the case 
	 * where the quality profile with the same name is already present on the Sonarqube server
	 * @throws IOException if the server is not accessible
	 * @throws SonarServerHttpErrorException if the server returns an HTTP error
	 */
	@Test
	public void qualityProfileConstructorIfArlreadyQualityProfileTest() throws IOException, SonarServerHttpErrorException{
	createExpectation(createHttpGetRequest("/api/qualityprofiles/search"), createHttpResponse("200","{\"profi"
			+ "les\":[{\"key\":\"java-profilefornotationautomated-81596\",\"name\":\"profile java test0\",\"langu"
			+ "age\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":false,\"activeRuleCount\":7,\"active"
			+ "DeprecatedRuleCount\":0,\"projectCount\":0,\"rulesUpdatedAt\":\"2017-03-25T15:51:45+0000\",\"lastUsed\":\"2017-03-2"
			+ "5T16:51:51+0100\",\"userUpdatedAt\":\"2017-03-25T16:51:45+0100\"},{\"key\":\"java-sonar-way-06261\",\"name\":\"profil"
			+ "e java test1\",\"language\":\"java\",\"languageName\":\"Java\",\"isInherited\":false,\"isDefault\":true,\"activeRuleCount\":27"
			+ "7,\"activeDeprecatedRuleCount\":0,\"rulesUpdatedAt\":\"2017-03-20T20:24:39+0000\",\"lastUsed\":\"2017-03-21T10:23:41+0100\"}]}"));
	
	ArrayList<String> listForMockServer = new ArrayList<String>();
	listForMockServer.add("profileName=profile java test2");
	listForMockServer.add("language=java");
	createExpectation(createHttpPostRequest("/api/qualityprofiles/create",listForMockServer),(
			createHttpResponse("200","{\"profile\":{\"key\":\"java-profile-java-test-86266\",\"name\":\"profile java"
					+ " test2\",\"language\":\"java\",\"languageName\":\"Java\",\"isDefault\":false,\"isInherited\":false}}")));
	listForMockServer.clear();
	listForMockServer.add("profile_key=java-profile-java-test-86266");
	listForMockServer.add("rule_key=squid:S2998");
	createExpectation(createHttpPostRequest("/api.qualityprofiles/activate_rule",listForMockServer),createHttpResponse("200",""));
	listForMockServer.clear();
	listForMockServer.add("profile_key=java-profile-java-test-86266");
	listForMockServer.add("rule_key=squid:S3448");
	createExpectation(createHttpPostRequest("/api.qualityprofiles/activate_rule",listForMockServer),createHttpResponse("200",""));

	SystemConfiguration sys = new SystemConfiguration("systemConfigurationTestHttp.properties");
	SonarqubeWeb sonar = new SonarqubeWeb(sys);
	HashMap<String,ModelValue> mapRuleToActivate = new HashMap<String,ModelValue>();
	mapRuleToActivate.put("squid:S2998",new ModelValue(new Float(5),new Float(4), new Float(9)));
	mapRuleToActivate.put("squid:S2998",new ModelValue(new Float(3),new Float(2), new Float(1)));
	QualityProfile qualityProfileTest = new QualityProfile("profile java test1", mapRuleToActivate,sonar);
	qualityProfileTest.configurationRules();
	Assert.assertEquals(qualityProfileTest.getName(),"profile java test2");
	Assert.assertEquals(qualityProfileTest.getProfileKey(),"java-profile-java-test-86266");
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
	 * @return an instance of HttpRequest
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
		for (int i = 0 ; i < queryStringParameter.size() ; i++ ){
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
	 * Create an expectation for the mockServer
	 * @param request the request expected
	 * @param response the response returned
	 */
	public void createExpectation(HttpRequest request, HttpResponse response){
		mockServerClient.when(request).respond(response);
	}

	/**
	 * This method encodes a string in UTF-8
	 * @param toEncode the string that must be encoded
	 * @return a string encoded
	 * @throws UnsupportedEncodingException
	 */
	private String urlEncode(String toEncode) throws UnsupportedEncodingException{
		return URLEncoder.encode(toEncode, "UTF-8");
	}
	
	/**
	 * This method encodes a string like parameter "xxxx=xxxxx" in UTF-8
	 * @param parameter is the parameter to encode
	 * @return a String encoded
	 * @throws UnsupportedEncodingException if the character encoding is not supported
	 */
	private String urlEncodeParameter(String parameter) throws UnsupportedEncodingException{
		int equal = parameter.indexOf("=");
		return this.urlEncode(parameter.substring(0,equal))+"="+this.urlEncode(parameter.substring(equal+1));
	}

	/**
	 * After the last tests, deletion of the file used to test the unit tests
	 */
	@AfterClass
	public static void deletaionFile(){
		File fileWithHttpUrl = new File("systemConfigurationTestHttp.properties");
		fileWithHttpUrl.delete();
		
		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}	
}
