package fr.up5.miage.testsReport;

import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.sonarqube.SonarqubeDataBaseException;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertTrue;

import fr.up5.miage.utility.UtilFile;
import scala.Int;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.mysql.cj.x.protobuf.MysqlxDatatypes.Array;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class TestRepport {

	/**
	 * the method where was failed
	 */
	private String originName;
	/**
	 * the error message
	 */
	private String errorMesssage;
	private int moudleID;
	private List<String> listClassName;
	private List<SonarqubeTestTeacher> listTestTeacher;
	private String projectName;
	private int projectID;

	/**
	 * @throws InterruptedException
	 */
	public TestRepport(int moduleID, String projectName) {
		this.projectName = projectName;
		listClassName = new ArrayList<>();
		listTestTeacher = new ArrayList<>();
		this.moudleID = moduleID;

	}

	public void laucnchTestsRapport(String commandLog)
			throws ClassNotFoundException, SQLException, IOException, InterruptedException, SonarqubeDataBaseException {
		this.errorMesssage = commandLog;
		createRepport();
	}

	public TestRepport() {
		listTestTeacher = new ArrayList<>();
	}

	/**
	 * @throws InterruptedException
	 *
	 */
	public void createRepport()
			throws ClassNotFoundException, SQLException, SonarqubeDataBaseException, IOException, InterruptedException {
		List<String> listTests = new ArrayList<>();
		String[] token = errorMesssage.split("\n");
		boolean isStarted = false, isFinished = false;

		for (int i = 0; i < token.length; i++) {

			if (token[i].contains("Failed tests:")) {
				isStarted = true;
				token[i] = token[i].replace("Failed tests:", "");
			}

			if (isStarted && !isFinished) {
				if (token[i].trim().length() > 0)
					listTests.add((token[i].trim()));
			}

			if (token[i].contains("Tests run:") && isStarted)
				break;
		}
		System.err.println(listTests);
		addAllSourceFile(listTests);
		isertTestResult(listTests);
	}

	public SonarqubeTestResults createTestResult(String message) {

		String[] token = message.split(",");
		int[] tabResults = new int[4];
		for (int i = 0; i < token.length; i++) {
			String[] token2 = token[i].split(":");
			tabResults[i] = Integer.parseInt(token2[1].trim());
		}

		int run = tabResults[0];
		int failure = tabResults[1];
		int error = tabResults[2];
		int skypped = tabResults[3];

		return new SonarqubeTestResults(run, failure, error, skypped);
	}

	public SonarqubeTestDetails createTestDetails(String message) {
		String[] token1 = message.split("\\): ");
		String[] token2 = message.split("expected:<");
		String[] token3 = message.split("but was:<");
		String method = token1[0].split("\\(")[0];
		String className = token1[0].split("\\(")[1];
		String spected = token2[1].split(">")[0];
		;
		String result = token3[1].split(">")[0];
		;
		listClassName.add(className);
		return new SonarqubeTestDetails(className, method, spected, result);
	}

	public SonarqubeTestDetails createTestDetailsForPython(int projectID, String projectName, int moduleID,
			String message) {
		String[] token = message.split("\n");

		String sourceFile = "";
		String method = "";
		String msg = "";
		String tokenTMP[] = null;
		boolean isDeleted = false;
		for (String line : token) {
			if (line.contains("File ")) {

				tokenTMP = line.split(" ");
				method = tokenTMP[tokenTMP.length - 1];
				sourceFile = line.split(projectName + "/")[1] + ".py";
				sourceFile = sourceFile.split(".py")[0];
				sourceFile = sourceFile + ".py";

				System.out.println();
			} else if (line.contains("Error: ")) {
				msg = line;

				try {

					int sourceID = SonarDataBase.getInstance().getSourceID2(projectName, sourceFile);
					if (!isDeleted) {
						SonarDataBase.getInstance().deleteTesDetails(sourceID);
						isDeleted = true;
					}

					SonarDataBase.getInstance().insertTestDetails2(moduleID, projectID, sourceID, method, msg);
				} catch (Exception e) {
					System.err.println("NOT CREATED NEW TEST TEACHER TABLE ");
				}

			}
		}
		return null;// new SonarqubeTestResults(ran,failures,errors,skypped);
	}

	public static void createTestMetricsForPython(String message, HashMap<String, Float> mapMetrics, int projectID,
			int moudleID) throws ClassNotFoundException, SQLException, IOException, SonarqubeDataBaseException {
		String[] token = message.split("\n");
		int ran = 0;
		int failures = 0;
		int errors = 0;
		int skypped = 0;
		for (String line : token) {

			if (line.contains("Ran ") && line.contains(" in ")) {
				ran = Integer.parseInt(line.split(" ")[1]);

			} else if (line.contains("FAILED")) {

				String tokenTMP[] = line.split("\\)");

				if ((line.contains("errors") && !line.contains("failures"))
						|| (!line.contains("errors") && line.contains("failures"))) {

					int val = Integer.parseInt(tokenTMP[0].split("=")[1]);
					if (line.contains("errors")) {
						errors = val;
					} else if (line.contains("failures")) {
						failures = val;
					}
				} else {
					String tokenTMP2[] = tokenTMP[0].split("=");
					System.out.println();
					failures = Integer.parseInt(tokenTMP2[1].split(",")[0]);
					errors = Integer.parseInt(tokenTMP2[2]);
				}
			}
		}
		System.err.println("ran : " + ran + " errors : " + errors + " failures : " + failures);

		int notPassed = failures + errors;
		float tmp = ran * 1.0f;
		float diff = notPassed / tmp;
		float density = (1 - (diff)) * 100;
		mapMetrics.putIfAbsent("tests", ran * 1.0f);
		mapMetrics.putIfAbsent("test_success_density", density);
		try {
			SonarDataBase.getInstance().insertTestResults(moudleID, projectID, ran, failures, errors, skypped);
		} catch (Exception e) {
			if (projectID == 0) // NOT FOUND
				e.printStackTrace();
			else {
				SonarDataBase.getInstance().updateTestResults(ran, failures, errors, skypped, projectID);
			}
		}
		// return new SonarqubeTestResults(ran,failures,errors,skypped);
	}

	public void findAllMethodsForPython(int moduleID, String message) {
		String[] token = message.split("\n");
		try {
			SonarDataBase.getInstance().deleteTestTeacher(moduleID);
		} catch (Exception e) {
			System.err.println("NOT DELETED TEST TEACHER TABLE ");
		}
		for (String line : token) {
			if (line.contains("============"))
				return;
			if (line.contains("(") && line.contains(")")) {
				String tokenTMP[] = line.replaceAll("\\(", "").replaceAll("\\)", "").split(" ");
				String method = tokenTMP[0];
				String sourcePath = tokenTMP[1].split("\\.")[0] + ".py";
				try {

					SonarDataBase.getInstance().insertTestTeacher(moduleID, sourcePath, method);
				} catch (Exception e) {
					System.err.println("NOT CREATED NEW TEST TEACHER TABLE ");
				}
			}
		}
		System.out.println("Yugapap");
	}

	public void findAllMethods(String path, int moduleID)
			throws IOException, ClassNotFoundException, SonarqubeDataBaseException, SQLException {

		path += File.separator + "target" + File.separator + "surefire-reports";
		File surefireReportsFolder = new File(path);
		String[] tabFiles = surefireReportsFolder.list();
		List<String> allTetsFiles = new ArrayList<>();
		if (tabFiles != null) {
			for (String f : tabFiles) {
				if (f.contains("Test.xml")) {
					allTetsFiles.addAll(UtilFile.readFile(path + File.separator + f));
				}
			}
		}
		for (String line : allTetsFiles) {
			if (line.contains("<testcase"))
				createSonarqubeTestTeacher(line, moduleID);
		}
	}

	public void createSonarqubeTestTeacher(String message, int moduleID)
			throws ClassNotFoundException, SQLException, IOException, SonarqubeDataBaseException {
		String[] token1 = message.split("\"");
		String method = token1[3];
		String sourcePath = token1[1];

		try {
			SonarDataBase.getInstance().insertTestTeacher(moduleID, sourcePath, method);
		} catch (Exception e) {
			System.err.println("NOT CREATED NEW TEST TEACHER TABLE ");
		}

		listTestTeacher.add(new SonarqubeTestTeacher(moduleID, sourcePath, method));
	}

	public void createTestDetailsForPHP(int projectID, String projectName, int moduleID, String message) {
		String[] token = message.split("\n");
		String sourceFile = "";
		String method = "";
		String msg = "";
		String tokenTMP[] = null;
		boolean isDeleted = false;
		int cpt = 0;
		boolean isOk = false;
		if (message.contains("::")) {
			for (int i = 0; i < token.length; i++) {
				String line = token[i];
				if (line.contains("::")) {
					tokenTMP = line.split("::");
					method = tokenTMP[tokenTMP.length - 1];
					sourceFile = tokenTMP[0].split(" ")[1] + ".php";
					cpt = i;
					isOk = true;
				} else if (cpt == i - 1 && isOk) {
					msg = line;
					isOk = false;
					try {

						int sourceID = SonarDataBase.getInstance().getSourceID2(projectName, sourceFile);
						if (!isDeleted) {
							SonarDataBase.getInstance().deleteTesDetails(sourceID);
							isDeleted = true;
						}

						SonarDataBase.getInstance().insertTestDetails2(moduleID, projectID, sourceID, method, msg);
					} catch (Exception e) {
						System.err.println("NOT CREATED NEW TEST TEACHER TABLE ");
					}

				}
			}
		}
	}

	public static void createTestMetricsForPHP(String message, HashMap<String, Float> mapMetrics, int projectID,
			int moudleID) throws Exception {
		String[] token = message.split("\n");
		int tests = 0;
		int failures = 0;
		int errors = 0;
		int skypped = 0;

		for (String line : token) {
			if (line.contains("OK (") || line.contains("Tests: ")) {
				if (line.contains("OK (")) {
					String[] token2 = line.split("\\(");
					tests = Integer.parseInt(token2[1].split(" ")[0]);
				} else if (line.contains("Tests: ")) {
					String[] token2 = line.replace(".", "").split(",");
					for (String s : token2) {
						String[] sTmp = s.replace(" ", "").split(":");
						String type = sTmp[0];
						if (type.equals("Tests")) {
							tests = Integer.parseInt(sTmp[1]);
						} else if (type.equals("Errors")) {
							errors = Integer.parseInt(sTmp[1]);
						} else if (type.equals("Failures")) {
							failures = Integer.parseInt(sTmp[1]);
						}
					}
				}
				if (tests == 0) {
					throw new Exception("not found tests");
				}
			}
		}

		int notPassed = failures + errors;
		float tmp = tests * 1.0f;
		float diff = notPassed / tmp;
		float density = (1 - (diff)) * 100;
		mapMetrics.putIfAbsent("tests", tests * 1.0f);
		mapMetrics.putIfAbsent("test_success_density", density);
		try {
			SonarDataBase.getInstance().insertTestResults(moudleID, projectID, tests, failures, errors, skypped);
		} catch (Exception e) {
			if (projectID == 0) // NOT FOUND
			{
				throw new SonarqubeDataBaseException("Project not founf in database");
			} else {
				SonarDataBase.getInstance().updateTestResults(tests, failures, errors, skypped, projectID);
			}
		}
	}

	public void findAllMethodsForPHP(int moduleID, String message) {
		String[] token = message.split("\n");
		try {
			SonarDataBase.getInstance().deleteTestTeacher(moduleID);
		} catch (Exception e) {
			System.err.println("NOT DELETED TEST TEACHER TABLE ");
		}
		for (String line : token) {
			if (line.contains("::") && line.contains("-")) {
				String tokenTMP[] = line.replaceAll("-", "").replaceAll(" ", "").split("::");
				String method = tokenTMP[1];
				String sourcePath = tokenTMP[0] + ".php";
				try {
					SonarDataBase.getInstance().insertTestTeacher(moduleID, sourcePath, method);
				} catch (Exception e) {
					System.err.println("NOT CREATED NEW TEST TEACHER TABLE ");
				}
			}
		}
	}

	public void addAllSourceFile(List<String> listTests) {
		for (int i = 0; i < listTests.size() - 1; i++) {
			String[] token1 = listTests.get(i).split("\\): ");
			String className = token1[0].split("\\(")[1];
			if (!contains(className))
				listClassName.add(className);
		}
	}

	public boolean contains(String s) {
		for (String className : listClassName) {
			if (s.equals(className))
				return true;
		}
		return false;
	}

	private void isertTestResult(List<String> list)
			throws ClassNotFoundException, SQLException, IOException, SonarqubeDataBaseException, InterruptedException {

		SonarDataBase sonarDB = SonarDataBase.getInstance();
		SonarqubeTestResults sonarResults = createTestResult(list.get(list.size() - 1));

		list.remove(list.size() - 1);
		SonarqubeTestDetails sonarDetails;
		Map<Integer, String> mapID = new HashMap();
		sleep(2000);
		projectID = sonarDB.getProjectID(this.projectName);
		try {
			sonarDB.insertTestResults(this.moudleID, projectID, sonarResults.getRun(), sonarResults.getFailure(),
					sonarResults.getError(), sonarResults.getSkypped());
		} catch (Exception e) {
			if (projectID == 0) // NOT FOUND
				e.printStackTrace();
			else {
				sonarDB.updateTestResults(sonarResults.getRun(), sonarResults.getFailure(), sonarResults.getError(),
						sonarResults.getSkypped(), projectID);
			}
		}
		// Update
		for (String name : listClassName) {
			int resultID = sonarDB.getSourceID(name, this.projectName, "java");
			sonarDB.deleteTesDetails(resultID);
		}
		for (String s : list) {
			sonarDetails = createTestDetails(s);
			int sourceID = sonarDB.getSourceID(sonarDetails.getClassName(), this.projectName, "java");
			try {
				sonarDB.insertTestDetails(this.moudleID, projectID, sourceID, sonarDetails.getMethod(),
						sonarDetails.getSpected(), sonarDetails.getResult());
			} catch (Exception e) {
				if (sourceID == 0) {// NOT FOUND
					e.printStackTrace();
				}
			}
		}
	}

	public int getTestScore(int idMoule)
			throws ClassNotFoundException, SQLException, IOException, SonarqubeDataBaseException {

		return SonarDataBase.getInstance().getTestScore(this.projectID, idMoule);
	}

	/**
	 * 
	 * @param mapResult
	 * @param mapMetrics
	 * @param moduleId
	 * @param projectId
	 */
	public static void createMetricsForJs(HashMap<String, String> mapResult, HashMap<String, Float> mapMetrics,
			int moduleId, int projectId) {
		int run = Integer.parseInt(mapResult.get("tests"));
		int fails = Integer.parseInt(mapResult.get("fails"));
		float tmp = run * 1.0f;
		float tmp2 = fails * 1.0f;
		float div = tmp2/tmp;
		float density = (1 - (div)) * 100;
		mapMetrics.putIfAbsent("tests", run * 1.0f);
		mapMetrics.putIfAbsent("test_success_density", density);

	}

	/**
	 * 
	 * @param moduleId
	 * @param projectId
	 * @param mapRes
	 * @throws ClassNotFoundException
	 * @throws SonarqubeDataBaseException
	 * @throws SQLException
	 * @throws IOException
	 */
	public void insertTestResultForJs(int moduleId, int projectId, HashMap<String, String> mapRes)
			throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		
		int error = 0;
		int skip = 0;
		int run = Integer.parseInt(mapRes.get("tests"));
		int fails = Integer.parseInt(mapRes.get("fails"));

		try {
			SonarDataBase.getInstance().insertTestResults(moduleId, projectId, run, fails, error, skip);
		} catch (ClassNotFoundException | SonarqubeDataBaseException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			SonarDataBase.getInstance().updateTestResults(moduleId, projectId, run, fails, error, skip);
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param path
	 * @param moduleID
	 * @return
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws SonarqubeDataBaseException 
	 * @throws ClassNotFoundException 
	 */
	public HashMap<String, String> readXmlFileForTeacher(String path, int moduleID,String projectName) throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		
		SonarDataBase.getInstance().deleteTestTeacher(moduleID);
		try {
			// creating a constructor of file class and parsing an XML file
			File file = new File(path);
			if (!file.exists()) {
				System.out.println("Le fichier ,n'existe pas");
				return null;
			}
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nodeList = doc.getElementsByTagName("testsuites");
			Node resume = nodeList.item(0);
			Element elem = (Element) resume;
			String run = elem.getAttribute("tests");
			String fails = elem.getAttribute("failures");
			HashMap<String, String> globalResul = new HashMap<String, String>();

			globalResul.put("tests", run);
			globalResul.put("fails", fails);
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList enf1 = node.getChildNodes();

				System.out.println("nbEnf" + enf1.getLength());

				for (int t = 0; t < enf1.getLength(); t++) {
					Node nod = enf1.item(t);

					if (nod.getNodeType() == Node.ELEMENT_NODE) {

						NodeList node2 = nod.getChildNodes();
						int length2 = node2.getLength();
						// System.out.println("ee "+length2);

						if (length2 == 1) {
							continue;
						}
						Element el = (Element) nod;
						System.out.println(el.getTagName());
						String src = el.getAttribute("file");
						for (int x = 0; x < node2.getLength(); x++) {
							Node nod2 = node2.item(x);
							if (nod2.getNodeType() == Node.ELEMENT_NODE) {
								Element ell = (Element) nod2;
								System.out.println("*****************" + ell.getTagName());
								if (ell.getTagName().equals("testcase")) {
									System.out.println("*************************" + ell.getAttribute("classname"));
									String method = ell.getAttribute("classname");

									String source=src.split(projectName)[1];
									String pathComplet= projectName+source;
									
									System.err.println("src " + pathComplet + "   meth " + method);
									
									SonarDataBase.getInstance().insertTestTeacher(moduleID, pathComplet, method);

								}
								System.out.println();

							}
						}
					}
				}

			}
			return globalResul;
		} catch (Exception e) {
			
			System.out.println("Message " + e.getMessage());
			e.printStackTrace();

		}

		return null;
	}

	public HashMap<String, String> readXmlFileForStudent(String path, int moduleID, int projectID) throws ClassNotFoundException, SonarqubeDataBaseException, SQLException, IOException {
		SonarDataBase.getInstance().deleteTesDetails(projectID);
		try {
			// creating a constructor of file class and parsing an XML file
			File file = new File(path);
			if (!file.exists()) {
				System.out.println("Le fichier ,n'existe pas");
				return null;
			}
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("testsuites");
			Node resume = nodeList.item(0);
			Element elem = (Element) resume;
			String run = elem.getAttribute("tests");
			String fails = elem.getAttribute("failures");
			HashMap<String, String> globalResul = new HashMap<String, String>();

			globalResul.put("tests", run);
			globalResul.put("fails", fails);

			insertTestResultForJs(moduleID, projectID, globalResul);

			System.out.println("par" + nodeList.getLength());
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList enf1 = node.getChildNodes();
				for (int t = 0; t < enf1.getLength(); t++) {
					Node nod = enf1.item(t);

					if (nod.getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) nod;

						NodeList node2 = nod.getChildNodes();
						int length2 = node2.getLength();
						if (length2 == 1) {
							continue;
						}
						System.out.println(el.getTagName());
						for (int x = 0; x < node2.getLength(); x++) {
							Node nod2 = node2.item(x);
							if (nod2.getNodeType() == Node.ELEMENT_NODE) {
								Element ell = (Element) nod2;

								String method = ell.getAttribute("classname");

								System.out.println();

								NodeList node3 = nod2.getChildNodes();
								int length3 = node3.getLength();
								if (length3 == 1) {
									continue;
								}
								for (int y = 0; y < node3.getLength(); y++) {
									Node nod3 = node3.item(y);
									if (nod3.getNodeType() == Node.ELEMENT_NODE) {
										Element ell3 = (Element) nod3;
										String message = ell3.getAttribute("message");
										String typeError = ell3.getAttribute("type") + ": ";
										System.err.println("methode= " + method + " message :" + message);
										String msg = typeError + message;
										String file1 = el.getAttribute("file");

										String sep = "";
										if (System.getProperty("os.name").contains("Windows")) {
											sep = "\\\\";
										} else {
											sep = "/";

										}
										String fileItem[] = file1.split(sep);
										String fileName = fileItem[fileItem.length - 1];
										System.err.println("methode= " + method + " message :" + message + "src "
												+ file1 + "fileMatch " + fileName);

										int srcId = SonarDataBase.getInstance().getSourceIDForJavaScript(fileName,
												"NodePro");
										SonarDataBase.getInstance().insertTestDetails2(moduleID, projectID, srcId,
												method, msg);

									}

								}
							}
						}
					}
				}

			}

			return globalResul;

		} catch (Exception e) {
			System.out.println("Message " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	

}
