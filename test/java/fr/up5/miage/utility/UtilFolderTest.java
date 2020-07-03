package fr.up5.miage.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.notation.App;


/**
 * This class of tests is used to test the UtilFolder class
 */
public class UtilFolderTest {





	/*
	 * To execute the unit tests, you need to create some projects,
	 * all in the same file.
	 * 
	 * Please write here this file's path :
	 */
	private static final String FOLDER_TEST = System.getProperty("user.dir") + File.separator + "testsProjects" + File.separator  ;
	
	/*
	 * In this File, you have to create a Java Project
	 * which contains at least a folder "main" and a folder "test"
	 * in the folder "src"
	 * 
	 * Please to write here this project's name
	 */
	private static final String JAVA_TEST = "Java" ;
	
	
	/*
	 * You have to create two projects Python in the same folder,
	 * one for the teacher and one for the student
	 */
	private static final String PYTHON_TEST_T = "Python_teacher" ;
	private static final String PYTHON_TEST_S = "Python_student" ;




	/**
	 * Class attribute that represents a folder with a Maven structure within it
	 */
	private static File folderTestMaven;

	/**
	 * Class attribute that represents a folder with no Maven structure within it
	 */
	private static File folderTestNotMaven;

	/**
	 * Class attribute that represents a folder which must be deleted
	 */
	private static File folderTestToDelete;

	/**
	 * Class attribute that represents a folder which must be created
	 */
	private static File folderTestToCreate;



	/**
	 * This methods initializes all class attributes
	 * @throws IOException if an error occurs during initialization
	 */
	@BeforeClass
	public static void init() throws IOException{
		
		//Initialize the log for the tests
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();

		//Initialization attributes of class
		folderTestNotMaven = new File("testFolderNotMaven");
		folderTestNotMaven.mkdirs();
		folderTestMaven = new File("testFolderMaven");
		folderTestMaven.mkdirs();
		folderTestToDelete = new File("testToDelete");
		folderTestToDelete.mkdirs();
		folderTestToCreate = new File("testToCreate");
		
		//Some files in the folder to delete
		//(to check they are deleted)
		File file = new File(folderTestToDelete.getAbsolutePath() + File.separatorChar + "text.txt") ;
		file.createNewFile() ;
		file = new File(folderTestToDelete.getAbsolutePath() + File.separatorChar + "xml.xml") ;
		file.createNewFile() ;
		file = new File(folderTestToDelete.getAbsolutePath() + File.separatorChar + "class.java") ;
		file.createNewFile() ;

		//Initialization of folder with a Maven structure
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+"pom.xml");
		file.createNewFile();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+".classpath");
		file.createNewFile();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+".project");
		file.createNewFile();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+"src"+File.separatorChar+"java"+File.separatorChar+"packageTest");
		file.mkdirs();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+"target");
		file.mkdirs();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+".settings");
		file.mkdirs();
		file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+"src"+File.separatorChar+"java"+File.separatorChar+"packageTest"+File.separatorChar+"testClass.java");
		file.createNewFile();
		try (FileWriter input = new FileWriter(file);BufferedWriter bufInput = new BufferedWriter(input)){
			bufInput.write("import fr.up5.packageTest.importOfClass");
		}
	}

	/**
	 * This test tests if the permission folder write and read are given to the program on user.home folder
	 * @throws FileNotFoundException if the folder does not exist
	 */
	@Test
	public void checkPermissionsFolderTest() throws FileNotFoundException {
		assertEquals(UtilFolder.checkPermissionsFolder(System.getProperty("user.home")),"rw");
	}
	
	
	/**
	 * This test tests is the function isMavenProject()
	 * @throws FileNotFoundException  if the folder is not found
	 */
	@Test
	public void isMavenProjectTest() throws FileNotFoundException{
		assertTrue(UtilFolder.isMavenProject(UtilFolderTest.folderTestMaven.getAbsolutePath()));
		assertFalse(UtilFolder.isMavenProject(UtilFolderTest.folderTestNotMaven.getAbsolutePath()));
		assertFalse(UtilFolder.isMavenProject(FOLDER_TEST + JAVA_TEST));
		assertFalse(UtilFolder.isMavenProject(FOLDER_TEST + PYTHON_TEST_T)) ;
		assertFalse(UtilFolder.isMavenProject(FOLDER_TEST + PYTHON_TEST_S)) ;
	}
	
	
	/**
	 * This test tests the deletion of a folder
	 * @throws FileNotFoundException if the folder to delete is not found
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void deleteFolderTest() throws FileNotFoundException{
		
		File[] foldertesttodeleteSFiles ;
		
		foldertesttodeleteSFiles = folderTestToDelete.listFiles() ;
		assertFalse(foldertesttodeleteSFiles==null) ;
		
		assertTrue(UtilFolder.deleteFolder(UtilFolderTest.folderTestToDelete.getAbsolutePath()));
		
		foldertesttodeleteSFiles = folderTestToDelete.listFiles() ;
		assertEquals(null,foldertesttodeleteSFiles) ;
	
	}
	
	
	/**
	 * Test the function pathTreatment
	 */
	@Test
	public void pathTreatmentTest(){
		
		// Not separator at the end : the function must add it
		assertEquals("userHome"+File.separatorChar , UtilFolder.pathTreatement("userHome"));
		
		// Separator at the end : the function musn't change anything
		assertEquals("userHome"+File.separatorChar , UtilFolder.pathTreatement("userHome" + File.separatorChar)) ;
	
	}
	
	
	
	/**
	 * Test the function isSeparator
	 */
	@Test
	public void isSeparatorTest() {
		
		assertTrue(UtilFolder.isSeparator(File.separator)) ;
		assertFalse(UtilFolder.isSeparator("String")) ;
		
	}
	
	
	/**
	 * Test the function endWithSeparator
	 */
	@Test
	public void endWithSeparatorTest() {
		
		assertFalse(UtilFolder.endsWithSeparator("ABC")) ;
		assertTrue(UtilFolder.endsWithSeparator(File.separator)) ;
		assertFalse(UtilFolder.endsWithSeparator("false" + File.separator + "path" + File.separator + "to" + File.separator + "test")) ;
		assertTrue(UtilFolder.endsWithSeparator("false" + File.separator + "path" + File.separator + "to" + File.separator + "test" + File.separator)) ;
		assertFalse(UtilFolder.endsWithSeparator(folderTestMaven.getAbsolutePath())) ;
		assertTrue(UtilFolder.endsWithSeparator(folderTestMaven.getAbsolutePath() + File.separator)) ;
		
	}
	

	/**
	 * This test tests the creation of a folder
	 */
	@Test
	public void createFolderTest(){
		assertTrue(UtilFolder.createFolder(folderTestToCreate.getAbsolutePath(), folderTestToCreate.getName()));
	}

	
	
	/**
	 * This test tests the recovery of a arborescence folder
	 * @throws FileNotFoundException if the folder concerned if not found
	 */
	@Test
	public void getFolderArborescenceTest() throws FileNotFoundException{
		assertFalse(UtilFolder.getFolderArborescence(UtilFolderTest.folderTestMaven.getAbsolutePath()).isEmpty());
		assertTrue(UtilFolder.getFolderArborescence(UtilFolderTest.folderTestToCreate.getAbsolutePath()).isEmpty());
	}

	
	
	/**
	 * Test if the import change is done
	 * @throws IOException if the reading of concerned file has a problem
	 */
	@Test
	public void changesImportTest() throws IOException{
		File file = new File (folderTestMaven.getAbsolutePath()+File.separatorChar+"src"+File.separatorChar+"java"+File.separatorChar+"packageTest"+File.separatorChar+"testClass.java");
		assertEquals(UtilFolder.changesImport(file, "importOfClass", "fr.up5.packageForTest.importOfClassTest"), "import fr.up5.packageForTest.importOfClassTest;\n");
	}
	

		

	/**
	 * Test if the path with package format is returned
	 * @throws FileNotFoundException if the file is not found
	 */
	@Test
	public void searchClassPathPackageFormatTest() throws FileNotFoundException{
		assertEquals(UtilFolder.searchClassPathPackageFormat("testClass.java", folderTestMaven), "packageTest.testClass");
		assertEquals(UtilFolder.searchClassPathPackageFormat("testClassNotExist.java", folderTestMaven), "");
	}


	
	@Test
	public void isACPlusPlusProjectTest() {
		assertFalse(UtilFolder.isACPlusPlusProject(folderTestMaven.getAbsolutePath())) ;
		assertFalse(UtilFolder.isACPlusPlusProject(FOLDER_TEST + JAVA_TEST)) ;
		assertFalse(UtilFolder.isACPlusPlusProject(FOLDER_TEST + PYTHON_TEST_T)) ;
		assertFalse(UtilFolder.isACPlusPlusProject(FOLDER_TEST + PYTHON_TEST_S)) ;
	}


	@Test
	public void isAJavaProjectTest() {
		assertTrue(UtilFolder.isAJavaProject(folderTestMaven.getAbsolutePath())) ;
		assertTrue(UtilFolder.isAJavaProject(FOLDER_TEST + JAVA_TEST)) ;
		assertFalse(UtilFolder.isAJavaProject(FOLDER_TEST + PYTHON_TEST_T)) ;
		assertFalse(UtilFolder.isAJavaProject(FOLDER_TEST + PYTHON_TEST_S)) ;
	}


	@Test
	public void projectTypeJavaTest() {
		int size = UtilFolder.extensionsArray.size() ;
		UtilFolder.projectTypeJava(folderTestMaven.getAbsolutePath());
		assertEquals(size+1 , UtilFolder.extensionsArray.size()) ;
		UtilFolder.projectTypeJava(FOLDER_TEST + PYTHON_TEST_T);
		assertEquals(size+1 , UtilFolder.extensionsArray.size()) ;
	}



	@Test
	public void projectTypeCPPTest() {
		int size = UtilFolder.extensionsArray.size() ;
		UtilFolder.projectTypeCPP(folderTestMaven.getAbsolutePath());
		assertEquals(size , UtilFolder.extensionsArray.size()) ;
	}
	
	
	
	
	

	/**
	 * 
	 * @param path : Full absolute path of a file
	 * @param separator : character used as separator in path (often '\\')
	 * @return The file's name.
	 * For example,  fileName("C:\\users\\JohnDoe\\eclipse-workspace\\project\\src\\Main.java" , '\\')  return "Main.java"
	 * 
	 * 
	 */
	private String fileName(String path, char separator) {

		String res = new String() ;

		for(int i = 0 ; i<path.length() ; i++) {

			if(path.charAt(i) == separator)
				res = "" ;
			else
				res += path.charAt(i) ;

		}

		return res ;

	}
	
	
	
	
	
	@Test
	public void copyMainFolderIntoMVNStandardMainFolderTest() throws Exception {

		UtilFolder.copyMainFolderIntoMVNStandardMainFolder(FOLDER_TEST + JAVA_TEST) ;

		File destinationFile = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main");

		File[] listFTJ = new File(FOLDER_TEST + JAVA_TEST + "\\src\\main").listFiles() ;
		File[] listDF = destinationFile.listFiles() ;

		assertEquals(listFTJ.length , listDF.length) ;
		if(listFTJ.length == listDF.length) {
			for(int i = 0 ; i < listFTJ.length ; i++)
				assertEquals(fileName(listFTJ[i].toString(),File.separatorChar) , fileName(listDF[i].toString(),File.separatorChar)) ;
		}

	}
	
	
	@Test
	public void copyTestFolderIntoMVNStandardTestFolderTest() throws Exception {

		UtilFolder.copyTestFolderIntoMVNStandardTestFolder(FOLDER_TEST + JAVA_TEST) ;

		File destinationFile = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test");

		File[] listFTJ = new File(FOLDER_TEST + JAVA_TEST + "\\src\\test").listFiles() ;
		File[] listDF = destinationFile.listFiles() ;

		assertEquals(listFTJ.length , listDF.length) ;
		if(listFTJ.length == listDF.length) {
			for(int i = 0 ; i < listFTJ.length ; i++)
				assertEquals(fileName(listFTJ[i].toString(),File.separatorChar) , fileName(listDF[i].toString(),File.separatorChar)) ;
		}

	}

	


	@Test
	public void convertJavaToMavenTest() throws Exception {

		UtilFolder.convertJavaToMaven(FOLDER_TEST + JAVA_TEST);

		File destinationFileMain = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main");
		File destinationFileTest = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test");
		
		File[] listFTJMain = new File(FOLDER_TEST + JAVA_TEST + "\\src\\main").listFiles() ;
		File[] listFTJTest = new File(FOLDER_TEST + JAVA_TEST + "\\src\\test").listFiles() ;
		File[] listDFMain = destinationFileMain.listFiles() ;
		File[] listDFTest = destinationFileTest.listFiles() ;


		File[] listFTJ, listDF ;



		for(int i = 0 ; i <= 1 ; i++) {

			listFTJ = (i==0) ? listFTJMain : listFTJTest ;
			listDF = (i==0) ? listDFMain : listDFTest ;

			assertEquals(listFTJ.length , listDF.length) ;
			if(listFTJ.length == listDF.length) {
				for(int j = 0 ; j < listFTJ.length ; j++)
					assertEquals(fileName(listFTJ[j].toString(),File.separatorChar) , fileName(listDF[j].toString(),File.separatorChar)) ;
			}

		}


	}



	@Test
	public void moveConvertedMVNIntoFolderTest() throws Exception {

		UtilFolder.convertJavaToMaven(FOLDER_TEST + JAVA_TEST);

		File originalFileMain = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main");
		File originalFileTest = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test");

		UtilFolder.moveConvertedMVNIntoFolder(folderTestMaven.getAbsolutePath()) ;

		File destinationFileMain = new File(folderTestMaven.getAbsolutePath() + "\\src\\main") ;
		File destinationFileTest = new File(folderTestMaven.getAbsolutePath() + "\\src\\test") ;


		assertFalse(destinationFileMain == null) ;
		assertFalse(destinationFileTest == null) ;

		if(destinationFileMain != null && destinationFileTest != null) {

			File[] listOFMain = originalFileMain.listFiles() ;
			File[] listOFTest = originalFileTest.listFiles() ;
			File[] listDFMain = destinationFileMain.listFiles() ;
			File[] listDFTest = destinationFileTest.listFiles() ;


			File[] listOF, listDF ;



			for(int i = 0 ; i <= 1 ; i++) {

				listOF = (i==0) ? listOFMain : listOFTest ;
				listDF = (i==0) ? listDFMain : listDFTest ;

				assertEquals(listOF.length , listDF.length) ;
				if(listOF.length == listDF.length) {
					for(int j = 0 ; j < listOF.length ; j++)
						assertEquals(fileName(listOF[j].toString(),File.separatorChar) , fileName(listDF[j].toString(),File.separatorChar)) ;
				}

			}
		}

	}







	@SuppressWarnings("deprecation")
	@Test
	public void reinitializeMVNStandardFolderTest() throws Exception {

		UtilFolder.convertJavaToMaven(FOLDER_TEST + JAVA_TEST);

		File destinationFileMain = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main") ;
		File destinationFileTest = new File(System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test") ;
		
		File[] mains = destinationFileMain.listFiles() ;
		File[] tests = destinationFileTest.listFiles() ;

		assertFalse(mains.length + tests.length == 0) ;


		UtilFolder.reinitializeMVNStandardFolder();

		mains = destinationFileMain.listFiles() ;
		tests = destinationFileTest.listFiles() ;
		assertEquals(null , mains) ;
		assertEquals(null , tests) ;


	}
	
	
	@Test
	public void isAPythonProjectTest() {
		
		assertTrue(UtilFolder.isAPythonProject(FOLDER_TEST + PYTHON_TEST_T)) ;
		assertTrue(UtilFolder.isAPythonProject(FOLDER_TEST + PYTHON_TEST_S)) ;
		assertFalse(UtilFolder.isAPythonProject(FOLDER_TEST + JAVA_TEST)) ;
		assertFalse(UtilFolder.isAPythonProject(folderTestMaven.getAbsolutePath())) ;
		
	}

	
	@Test
	public void projectTypePythonTest() {
		int size = UtilFolder.extensionsArray.size() ;
		UtilFolder.projectTypePython(folderTestMaven.getAbsolutePath());
		assertEquals(size , UtilFolder.extensionsArray.size()) ;
		UtilFolder.projectTypePython(FOLDER_TEST + PYTHON_TEST_T);
		assertFalse(size == UtilFolder.extensionsArray.size()) ;
	}
	
	@Test 
	public void placeTestFromProjectTeacherToProjectStudentforJavaScriptTest() throws IOException 
	{
		UtilFolder.deleteFolder("C:\\NodePro\\kio\\test");
		FileUtils.copyDirectory(new File("C:\\Users\\LENOVO E330\\Desktop\\ImageL3\\ImageL3-master\\ImageL3-master\\imgProcsJava\\src\\imageProject\\test"), new File("C:\\NodePro\\kio\\test"));
		System.err.println("yeyey");
		assertTrue(true);
	}

	/**
	 * After the last tests, the files and folders to which the unit tests have been used are deleted
	 * @throws FileNotFoundException if the file or folder concerned is not found
	 */
	@AfterClass
	public static void finish() throws FileNotFoundException {
		if (folderTestNotMaven.exists()){
			folderTestNotMaven.delete();
		}
		if (folderTestMaven.exists()){
			UtilFolder.deleteFolder(folderTestMaven.getAbsolutePath());
		}
		if (folderTestToDelete.exists()){
			folderTestToDelete.delete();
		}
		if (folderTestToCreate.exists()){
			UtilFolder.deleteFolder(folderTestToCreate.getAbsolutePath());
		}

		//Delete the file log tests
		File f = new File ("test.log");
		f.delete();
	}
}
