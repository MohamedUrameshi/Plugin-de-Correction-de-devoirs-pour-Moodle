package fr.up5.miage.testsReport;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.twitter.finagle.netty3.channel.BrokerChannelHandler.Write;

import java.time.LocalDateTime;

/*

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
*/
import fr.up5.miage.notation.App;
import fr.up5.miage.project.Project;
import fr.up5.miage.sonarqube.SonarDataBase;
import fr.up5.miage.utility.UtilFile;

/**
 * This class of tests is used to test the UtilJSON class
 */
public class SonarqubeTestTeacherTest {

	private SonarqubeTestTeacher sonarTeacher;

	/**
	 * This method initializes the listJsonText attribute with different kinds of
	 * JSON text replies, the mapReturn attribute and the listReturn attribute
	 */
	@Before
	public void init() {
		String analysisName="Abracadabra";
		LocalDateTime localDate=App.getLocalDateTime();
		String dateLaunch=localDate.getDayOfMonth()+"-"+localDate.getMonthValue()+"-"+localDate.getYear()+" "+
				localDate.getHour()+"-"+localDate.getMinute()+"-"+localDate.getSecond();
		String nameLog = "Logs"+File.separator+analysisName+" "+dateLaunch+"-"+99+".log";
		App.initLog(analysisName, dateLaunch, 66);
		App.log(dateLaunch, analysisName, nameLog);

		sonarTeacher = new SonarqubeTestTeacher(7, "Cadrage.java", "cadrageDroite");
	}

	/**
	 * This test tests if all the metrics are recovered
	 */
	@Test
	public void gettersTest() {

		Assert.assertEquals("cadrageDroite", sonarTeacher.getMethod());
	}

	/*
	public void readXmlFileForStudent() {
		try {
			// creating a constructor of file class and parsing an XML file
			File file = new File("C:\\NodePro\\report.xml");
			if (!file.exists()) {
				System.out.println("Le fichier ,n'existe pas");
				return;
			}
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nodeList = doc.getElementsByTagName("testsuites");
			Node resume =nodeList.item(0);
			Element elem = (Element) resume;
			System.err.println(elem.getAttribute("tests"));
			System.err.println(elem.getAttribute("failures"));

			System.out.println("par" + nodeList.getLength());
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList enf1 = node.getChildNodes();
				
			
				//System.out.println("nbEnf" + enf1.getLength());

				for (int t = 0; t < enf1.getLength(); t++) {
					Node nod = enf1.item(t);

					if (nod.getNodeType() == Node.ELEMENT_NODE) {
						Element el = (Element) nod;
						
						NodeList node2 = nod.getChildNodes();
						int length2=node2.getLength();
						if(length2==1){
							continue;
						} 
						
						System.out.println(el.getTagName());
						for (int x = 0; x < node2.getLength(); x++) {
							Node nod2 = node2.item(x);
							if (nod2.getNodeType() == Node.ELEMENT_NODE) {
								Element ell = (Element) nod2;
								//System.out.println("*****************" + ell.getTagName());
								
									 String method=ell.getAttribute("classname");
								
								System.out.println();

								NodeList node3 = nod2.getChildNodes();
								int length3=node3.getLength();
								if(length3==1) 
								{
									continue;
								}
								for (int y = 0; y < node3.getLength(); y++) {
									Node nod3 = node3.item(y);
									if (nod3.getNodeType() == Node.ELEMENT_NODE) {
										Element ell3 = (Element) nod3;
										String message=ell3.getAttribute("message");
										String typeError=ell3.getAttribute("type")+": ";
										String file1=el.getAttribute("file");
										String sep="";
										if(System.getProperty("os.name").contains("Windows"))
										{
											sep="\\\\";
										}
										else 
										{
											sep="/";
											
										}
										String fileItem[]=file1.split(sep);
										String fileName=fileItem[fileItem.length-1];
										System.err.println("methode= "+method +" message :"+message +"src "+file1+ "fileMatch "+fileName);
										String msg=typeError+message;	
										//int srcId=SonarDataBase.getInstance().getSourceIDForJavaScript(fileName, "NodePro");
									
										//SonarDataBase.getInstance().insertTestDetails2(66, 200, srcId, method, msg);

										
									}

									
								}
							}
						}
					}
				}

			}

			assertTrue(true);

		} catch (Exception e) {
			System.out.println("Message " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	

	public void readXmlFileForTeacher() {
		try {
			// creating a constructor of file class and parsing an XML file
			File file = new File("C:\\NodePro\\report.xml");
			if (!file.exists()) {
				System.out.println("Le fichier ,n'existe pas");
				return;
			}
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			NodeList nodeList = doc.getElementsByTagName("testsuites");

		
			System.out.println("par" + nodeList.getLength());
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList enf1 = node.getChildNodes();

				System.out.println("nbEnf" + enf1.getLength());

				for (int t = 0; t < enf1.getLength(); t++) {
					Node nod = enf1.item(t);

					if (nod.getNodeType() == Node.ELEMENT_NODE) {
						
						NodeList node2 = nod.getChildNodes();
						int length2=node2.getLength();
					//	System.out.println("ee "+length2);
						
						if(length2==1){
							continue;
						}
						Element el = (Element) nod;
						System.out.println(el.getTagName());
						String src=el.getAttribute("file");
						for (int x = 0; x < node2.getLength(); x++) {
							Node nod2 = node2.item(x);
							if (nod2.getNodeType() == Node.ELEMENT_NODE) {
								Element ell = (Element) nod2;
								System.out.println("*****************" + ell.getTagName());
								if (ell.getTagName().equals("testcase")) {
									System.out.println("*************************" + ell.getAttribute("classname"));
									String method=ell.getAttribute("classname");
									String source=src.split("NodePro")[1];
									String dd= "NodePro"+source;
									System.err.println("New source "+dd);
									System.err.println("src "+src+ "   meth "+ method );
									SonarDataBase.getInstance().insertTestTeacher(33, src,method);

								}
								System.out.println();
						
							}
						}
					}
				}

			}

			assertTrue(true);

		} catch (Exception e) {
			System.out.println("Message " + e.getMessage());
			//SonarDataBase.getInstance().up
			e.printStackTrace();
		}
	}
	
	

	*/
/*	
	public void addNodeFileXml() {
		try {
			// creating a constructor of file class and parsing an XML file
			File file = new File("C:\\NodePro\\pomTest.xml");
			if (!file.exists()) {
				System.out.println("Le fichier ,n'existe pas");
				return;
			}
			// an instance of factory that gives a document builder
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// an instance of builder to parse the specified xml file
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();
			System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
			Node pro=doc.getFirstChild();
			//System.err.println(pro.getTagName());
			Element ndione=doc.createElement("url");
			ndione.appendChild(doc.createTextNode("bismillah"));
			pro.appendChild(ndione);

		} catch (Exception e) {
			System.out.println("Message " + e.getMessage());
			//SonarDataBase.getInstance().up
			e.printStackTrace();
		}
	}
	
	
*/
	public static String readFile(String path, Charset encoding) throws IOException
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
	@Test
	public void tetet () throws Exception 
	{
		
		String build="</properties>\r\n"
				+ "<build>\r\n" + 
				"    <plugins>\r\n" + 
				"      <plugin>\r\n" + 
				"        <groupId>org.apache.maven.plugins</groupId>\r\n" + 
				"        <artifactId>maven-compiler-plugin</artifactId>\r\n" + 
				"        <version>3.3</version>\r\n" + 
				"        <configuration>\r\n" + 
				"          <source>1.8</source>\r\n" + 
				"          <target>1.8</target>\r\n" + 
				"        </configuration>\r\n" + 
				"      </plugin>\r\n" + 
				"    </plugins>\r\n" + 
				"  </build>" ;
		
		
		String bui=" <dependencies>\r\n"
				+ "<dependency>\r\n" + 
				"      <groupId>junit</groupId>\r\n" + 
				"      <artifactId>junit</artifactId>\r\n" + 
				"      <version>4.12</version>\r\n" + 
				"      <scope>test</scope>\r\n" + 
						"<!--srcpath-->\r\n"+
				"    </dependency>" ;
	         String file= readFile("C:\\NodePro\\pomTest.xml",  StandardCharsets.UTF_8);
	         
	         String f2=file.replace("</properties>", build);
	         
	         String f4=f2.replace("<dependencies>",bui);
	        // System.err.println(file);
	         System.out.println();
	         System.err.println(f4);
	         UtilFile.deleteFile("C:\\NodePro\\pomTest.xml");
	         File f= new File("C:\\NodePro\\pomTest.xml");
	         BufferedWriter bw = new BufferedWriter(new FileWriter(f));
	         bw.write(f4);
	         bw.flush();
	         assertTrue(true);
	               
	}
    

	
	/*
	 *  Element newServer = document.createElement("server");

            Element name = document.createElement("name");
            name.appendChild(document.createTextNode(server.getName()));
            newServer.appendChild(name);

            Element port = document.createElement("port");
            port.appendChild(document.createTextNode(Integer.toString(server.getPort())));
            newServer.appendChild(port);

            root.appendChild(newServer);*/
}