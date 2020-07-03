package fr.up5.miage.utility;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.core.appender.FileAppender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.up5.miage.notation.App;

/**
 * Tests UtilFile methods
 */
public class UtilFileTest {


	private static final String FOLDER_TEST = System.getProperty("user.dir") + File.separator + "testsProjects" + File.separator  ;



	/**
	 * Class attribute that represents the user directory
	 */
	private String homeUserDirectory;



	/**
	 * Class attribute that represents an example of a directory were the tests will be done
	 */
	private File folderTest;
	
	
	/**
	 * Class attribute that represents an example of a ZIP file
	 */
	private File fileZIP;


	/**
	 * Class attribute that represents an example of a text file
	 */
	private File fileTextTest;

	/**
	 * Class attribute that represents an example of a RTF file
	 */
	private File fileDocRTF;

	/**
	 * Before all tests
	 */
	@BeforeClass
	public static void initLog(){
		App.fileLog=FileAppender.newBuilder().withFileName("test.log").withName("automaticNotation").withLayout(App.layout).build();
		App.fileLog.start();
	}

	/**
	 * This method initializes all class attributes
	 * It creates the files and folders that we need to make the unit tests
	 * @throws IOException if an error occurs during initialization
	 */
	@Before
	public void init() throws IOException {

		this.homeUserDirectory = System.getProperty("user.home");

		this.fileZIP = new File("cadragePROF_Test");
		fileZIP.mkdirs();
		
		try (FileOutputStream fos = new FileOutputStream("cadragePROF_Test.zip");
				ZipOutputStream zipOut = new ZipOutputStream(fos);){
			
			UtilFile.zipFile(fileZIP, fileZIP.getName(), zipOut);
			
		} catch (IOException e) {
			System.out.println("Error while zipping a file");
		}
        
        this.fileZIP = new File("cadragePROF_Test.zip");
        
        

		this.folderTest = new File(homeUserDirectory + UtilFile.getFileSeparator() + "test");
		folderTest.mkdirs();

		this.fileTextTest = new File(folderTest.getAbsolutePath() + UtilFile.getFileSeparator() + "test.txt");
		fileTextTest.createNewFile();
		fileTextTest.mkdirs();

		this.fileDocRTF = new File(folderTest.getAbsolutePath() + UtilFile.getFileSeparator() + "doc.rtf");
		fileDocRTF.createNewFile();
		fileDocRTF.mkdirs();
	}





	/**
	 * Test if the permissions of the ZIP file are read, write and execute
	 * @throws FileNotFoundException if the file does not exist
	 */
	@Test
	public void checkPermissionsFileTest() throws FileNotFoundException {
		assertEquals("rwx" , UtilFile.checkPermissionsFile(FOLDER_TEST + "fileToTest.txt")) ;
	}

	
	@Test
	public void unzipFileTest() {
		
		try {
		 assertTrue(UtilFile.unzipFile(FOLDER_TEST + "fileToUnzip.zip", FOLDER_TEST)) ;
		} catch (FileNotFoundException e) {
			System.err.println("The file to unzip don't exist") ;
		}
		
		File unzipedFolder = new File(FOLDER_TEST + "fileToUnzip" + File.separator + "folder1" + File.separator + "folder2") ;
		assertFalse(unzipedFolder == null) ;
		
		File[] listFile = unzipedFolder.listFiles() ;
		assertEquals(5,listFile.length) ;
		
	}
	
	

	
	
	
	
	/**
	 * Test if the copy of the ZIP file in the test directory is correct
	 * @throws IOException if the file can't be written or can't be closed
	 */
	@Test
	public void copyFileTest() throws IOException {
		assertEquals(UtilFile.copyFile(fileZIP.getAbsolutePath(), folderTest.getAbsolutePath()), true);
	}
	
	
	
	

	/**
	 * Test if the rename of the text file is correct
	 * @throws FileNotFoundException if the file does not exist
	 */
	@Test
	public void renameFileTest() throws FileNotFoundException {
		assertEquals(UtilFile.renameFile(fileTextTest.getAbsolutePath(), "testRenamed.txt"), true);
	}


	/**
	 * Test if the deletion of the RTF file is correct
	 * @throws FileNotFoundException if the file does not exist
	 */
	@Test
	public void deleteFileTest() throws FileNotFoundException {
		assertEquals(UtilFile.deleteFile(fileDocRTF.getPath()), true);
	}


	/**
	 * After the last tests, the files and folders to which the unit tests have been used are deleted
	 * @throws FileNotFoundException if the file or folder concerned is not found.
	 */
	@After
	public void finish() throws FileNotFoundException{
		if (folderTest.exists())
			folderTest.delete();

		File fileTextTestRenamed = new File(folderTest.getAbsolutePath() + UtilFile.getFileSeparator() + "testRenamed.txt");
		if (fileTextTestRenamed.exists())
			fileTextTestRenamed.delete();

		if (fileDocRTF.exists())
			fileDocRTF.delete();

		File zipCopied = new File(folderTest + "cadragePROF_Test.zip");
		if (zipCopied.exists())
			zipCopied.delete();

		File fileRenamed = new File(fileTextTest.getParentFile().getAbsolutePath() + "testRenamed.txt");
		if (fileRenamed.exists())
			fileRenamed.delete();

		// The 'folderTest' is not deleted correctly if this method is not correct
		UtilFolder.deleteFolder(folderTest.getAbsolutePath());
	}

	/**
	 * After all tests
	 * @throws FileNotFoundException 
	 */
	@AfterClass
	public static void delete() throws FileNotFoundException{
		File f = new File ("test.log");
		f.delete();
		
		UtilFolder.deleteFolder(FOLDER_TEST + "fileToUnzip") ;
	}
}