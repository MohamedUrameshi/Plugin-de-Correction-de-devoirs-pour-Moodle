package fr.up5.miage.utility;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.up5.miage.project.Project;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.configuration.SystemConfiguration;
import fr.up5.miage.notation.App;
//import java.util.ArrayList;

/**
 * This class is used to manage folders
 */
public class UtilFolder {
	/**
	 * Represent an array containing files of type ".java" or ".cpp" in order to
	 * check if our project is of one of those types
	 */
	static ArrayList<File> extensionsArray = new ArrayList<File>();
	/**
	 * Represent the logger for this class
	 */
	private static LoggerConfig logUtilFolder;

	/**
	 * Initialization of static variable
	 */
	static {
		logUtilFolder = new LoggerConfig("UtilFolder.class", Level.INFO, false);
		logUtilFolder.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * Stock the file separator of the host system
	 */
	private final static String FILE_SEPARATOR = File.separator;

	/**
	 * @return the fileSeparator
	 */
	public static String getFileSeparator() {
		return FILE_SEPARATOR;
	}

	/**
	 * Check if we can read or write a folder with his path
	 *
	 * @param folderPathName : The path of folder to check permissions the folder
	 * @return a string which represents the permissions such as "rw"
	 */
	public static String checkPermissionsFolder(String folderPathName) {

		String permissions = new String();
		File folder = new File(folderPathName); // Create a folder with the path name in parameter

		if (folder.isDirectory()) {
			if (folder.canRead())
				permissions += "r";
			if (folder.canWrite())
				permissions += "w";
		}
		return permissions;
	}

	/**
	 * Check if a folder is a Maven project by testing presence of file "pom.xml"
	 *
	 * @param folderPathName : The path of folder for a Maven project to check the
	 *                       arborescence
	 * @return true if the folder contents all the files and folders in a list
	 */
	public static boolean isMavenProject(String folderPathName) {
		File parentFolder = new File(folderPathName);
		String[] tabFiles = parentFolder.list();

		if (tabFiles != null) {
			for (String f : tabFiles) {
				if (f.length() == 7 && f.equals("pom.xml")) {
					System.out.println("Maven Project detected");
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Delete a folder thanks to his path name
	 *
	 * @param folderPathName : The path of folder to delete
	 * @return true : if the folder is completely deleted
	 * @throws FileNotFoundException : if the folder does not exist
	 */
	public static boolean deleteFolder(String folderPathName) throws FileNotFoundException {

		boolean deletedFolder = false;
		File folder = new File(pathTreatement(folderPathName));

		if (folder.exists() && folder.isDirectory()) {
			deletedFolder = folder.delete();

			if (!deletedFolder) {
				File[] tableOfFiles = folder.listFiles();
				for (File file : tableOfFiles) {
					if (file.isFile()) {
						UtilFile.deleteFile(file.getAbsolutePath());
					} else {
						deleteFolder(file.getAbsolutePath());
					}
				}
				deletedFolder = folder.delete();
			}
		} else {
			logUtilFolder.log(
					"UtilFolder.class", null, null, Level.ERROR, (Message) new SimpleMessage("Folder '"
							+ folder.getAbsolutePath() + "' can't be deleted because it does not exist or is open"),
					null);
		}
		if (deletedFolder) {
			logUtilFolder.log("UtilFolder.class", null, null, Level.INFO,
					(Message) new SimpleMessage(folder.getAbsolutePath() + " was deleted with success"), null);
		}
		return deletedFolder;
	}

	/**
	 * Verify if the path needs a separator at the end
	 *
	 * @param path : the path of the file
	 * @return the path with the correct name
	 */
	public static String pathTreatement(String path) {

		if (!endsWithSeparator(path))
			path += getFileSeparator();
		return path;
	}

	/**
	 * Check if the string is a separator
	 *
	 * @param string : the string to test
	 * @return true if it is a path separator
	 */
	public static boolean isSeparator(String string) {

		return string.equals(getFileSeparator());
	}

	/**
	 * Check if the path ends with a separator
	 *
	 * @param path : the path to test
	 * @return true if it is a path which ends with a separator
	 */
	public static boolean endsWithSeparator(String path) {

		return isSeparator(path.substring(path.length() - 1));
	}

	/**
	 * Create a folder in a specific directory
	 *
	 * @param pathDestination : directory where the folder will be created
	 * @param folderName      : the name to give
	 * @return true : if the folder was created
	 */
	public static boolean createFolder(String pathDestination, String folderName) {

		String pathDestinationFolder = pathTreatement(pathDestination);

		File folder = new File(pathDestinationFolder + folderName);

		if (!folder.exists()) {
			File folderParent = folder.getParentFile();

			boolean parentFolderExists = folderParent.exists();

			while (parentFolderExists) {
				folderParent.mkdirs();
				if (folderParent.getParentFile() != null) {
					folderParent = folderParent.getParentFile();
				} else {
					parentFolderExists = false;
				}
			}
		} else {
			logUtilFolder.log("UtilFolder.class", null, null, Level.WARN,
					(Message) new SimpleMessage("Folder: " + folder.getName() + " already exists"), null);
			return folder.exists();
		}

		return folder.mkdirs();
	}

	/**
	 * Get the content of a folder and the arborescence of his content
	 *
	 * @param folderPathName : The path of folder to check the arborescence
	 * @return a map with arborescence
	 * @throws FileNotFoundException : if the folder does not exist
	 */
	public static Map<String, List<File>> getFolderArborescence(String folderPathName) throws FileNotFoundException {

		Map<String, List<File>> map = new HashMap<>();

		File parentFolder = new File(folderPathName);

		List<File> listContent = new ArrayList<File>();

		if (parentFolder.exists() && parentFolder.isDirectory()
				&& checkPermissionsFolder(parentFolder.getAbsolutePath()).contains("r")) {

			File[] tabFiles = parentFolder.listFiles();

			for (File fileElement : tabFiles) {
				listContent.add(fileElement);
			}

			map.put(parentFolder.getPath(), listContent);

			for (File childFile : listContent) {
				if (childFile.isDirectory() && checkPermissionsFolder(childFile.getAbsolutePath()).contains("r"))
					map.putAll(getFolderArborescence(childFile.getAbsolutePath()));
			}
		} else {
			logUtilFolder.log("UtilFolder.class", null, null, Level.INFO,
					(Message) new SimpleMessage("Folder '" + parentFolder.getName() + "' does not exist or is open"),
					null);
		}
		return map;
	}

	/**
	 * This method copies the project teacher's tests in a student project
	 *
	 * @param studentFile is the project student concerned
	 * @param teacherFile is the teacher project concerned
	 * @throws IOException if folder or file operation has a problem
	 */
	public static void placeTestFromProjectTeacherToProjectStudent(File studentFile, File teacherFile) throws IOException{
		File studentTestFile = new File (studentFile.getAbsolutePath()+"/src/test/java");
		File teacherTestFile = new File (teacherFile.getAbsolutePath()+"/src/test/java");
		if (studentTestFile.exists()){
			UtilFolder.deleteFolder(studentTestFile.getAbsolutePath());
		}
		UtilFolder.createFolder(studentFile.getAbsolutePath()+"/src/test", "java");
		String [] listTeacherTest = teacherTestFile.list();
		for (String testFile : listTeacherTest){
			if (testFile.contains(".java")){
				String pathClass = UtilFolder.searchClassPathPackageFormat(testFile, studentFile);
				UtilFolder.createTestInStudentProject(teacherTestFile, studentTestFile, testFile, pathClass);
			}
		}
	}
	
	/**
	 * 
	 * @param studentFile
	 * @param teacherFile
	 * @throws IOException
	 */
	public static void placeTestFromProjectTeacherToProjectStudentforJavaScript(File studentFile, File teacherFile) throws IOException{
		File studentTestFile = new File (studentFile.getAbsolutePath()+"/test");
		File teacherTestFile = new File (teacherFile.getAbsolutePath()+"/test");
		
		System.err.println(teacherTestFile.getAbsolutePath());
		System.err.println(studentTestFile.getAbsolutePath());
		if (studentTestFile.exists()){
			UtilFolder.deleteFolder(studentTestFile.getAbsolutePath());
		}
		
			FileUtils.copyDirectory(teacherTestFile, studentTestFile);
		
	}
	
	

	/**
	 * This method creates test file
	 *
	 * @param fileTeacherTest  is the teacher file from where the test file is
	 * @param fileStudentTest  is the destination of the test file to create
	 * @param nameClassTest    is the class name to give to the new file
	 * @param pathPackageClass is the path in package format to put in the new test
	 *                         file
	 * @throws IOException if a file or a folder operation has a problem
	 */
	public static void createTestInStudentProject(File fileTeacherTest, File fileStudentTest, String nameClassTest,
												  String pathPackageClass) throws IOException {
		File fileTest = new File(fileStudentTest.getAbsolutePath() + "/" + nameClassTest);
		try (BufferedWriter entrees = new BufferedWriter(new FileWriter(fileTest))) {
			entrees.write((UtilFolder.changesImport(new File(fileTeacherTest.getAbsolutePath() + "/" + nameClassTest),
					nameClassTest.replaceAll("Test.java", ""), pathPackageClass)));
		}
	}

	/**
	 * This method changes the import at the beginning of a file .java
	 *
	 * @param fileToChange       is the file ".java" to change
	 * @param classNameConcerned is the class name import that will be replaced
	 * @param pathPackageToPut   is the new import to put
	 * @return a String with the change
	 * @throws IOException if a file or a folder operation has a problem
	 */
	public static String changesImport(File fileToChange, String classNameConcerned, String pathPackageToPut)
			throws IOException {
		try (BufferedReader input = new BufferedReader(new FileReader(fileToChange))) {
			StringBuilder toWrite = new StringBuilder();
			String line;
			while ((line = input.readLine()) != null) {
				if (line.contains("import") && line.contains(classNameConcerned)) {
					line = "import " + pathPackageToPut + ";";
				}
				try {
					toWrite.append(line + "\n");
				} catch (Exception e) {
					toWrite.append("\n");
				}
			}
			return toWrite.toString();
		}
	}

	/**
	 * This method returns the path of the class in package format like
	 * "up5.fr.miage.Class"
	 *
	 * @param className   is the class that the path is searched
	 * @param projectFile is the Maven project concerned
	 * @return a String that represents the path of the class in package format
	 * @throws FileNotFoundException if a file or a folder is not found
	 */
	public static String searchClassPathPackageFormat(String className, File projectFile) throws FileNotFoundException {
		String classPathSearched = new String();
		Collection<List<File>> arbo = (UtilFolder.getFolderArborescence(projectFile.getAbsolutePath()).values());
		Iterator<List<File>> itListFile = arbo.iterator();
		while (itListFile.hasNext()) {
			List<File> listFile = itListFile.next();
			Iterator<File> itFile = listFile.iterator();
			while (itFile.hasNext()) {
				File file = itFile.next();
				if (file.getName().equals(className.replaceAll("Test", ""))) {
					classPathSearched = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf("java") + 5);
				}
			}
		}
		String stringToReplace;
		if (File.separatorChar == '\\') {
			stringToReplace = "\\" + File.separatorChar;
		} else {
			stringToReplace = "" + File.separatorChar;
		}
		return classPathSearched.replaceAll(stringToReplace, ".").replace(".java", "");
	}

	/**
	 * Check if a folder is a C++ project by testing presence of file ".cpp"
	 *
	 * @param folderPathName : The path of folder for a C++ project to check the
	 *                       arborescence
	 * @return true if the folder contents all the files and folders in a list
	 */
	public static boolean isACPlusPlusProject(String folderPathName) {
		boolean rep = false;
		// now recursively check all files of type .cpp from all folders from this path
		// then add them to extensionsArray
		projectTypeCPP(folderPathName);
		for (File f : extensionsArray) {
			if (f.getAbsolutePath().contains(".cpp")) {
				rep = true;
				extensionsArray.clear();
				break;
			}
		}
		return rep;

	}

	/**
	 * Check if a folder is a java project by testing presence of file ".java"
	 *
	 * @param folderPathName : The path of folder for a java project to check the
	 *                       arborescence
	 * @return true if the folder contents all the files and folders in a list
	 */
	public static boolean isAJavaProject(String folderPathName) {
		boolean rep = false;
		// now recursively check all files of type .java from all folders from this path
		// then add them to extensionsArray
		projectTypeJava(folderPathName);
		for (File f : extensionsArray) {
			if (f.getAbsolutePath().contains(".java")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * checks if the folder is type of "java" and then put it into the class
	 * attribute "extensionsArrays"
	 *
	 * @param path path of folder for a type of project to check the arborescence
	 */
	public static void projectTypeJava(String path) {
		File startingFolder = new File(path);
		File[] list = startingFolder.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isFile() && f.getAbsolutePath().contains(".java")) {
				extensionsArray.add(f);
			} else {
				projectTypeJava(f.getAbsolutePath());
			}
		}
	}

	/**
	 * checks if the folder is type of "C++" and then put it into the classe
	 * attribute "extensionsArrays"
	 *
	 * @param path
	 */
	public static void projectTypeCPP(String path) {
		File startingFolder = new File(path);
		File[] list = startingFolder.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isFile() && f.getAbsolutePath().contains(".cpp")) {
				extensionsArray.add(f);
			} else {
				projectTypeCPP(f.getAbsolutePath());
			}
		}
	}

	/**
	 * copy the main folder of the path (which is a java standard project) into the
	 * maven standard main project
	 *
	 * @param path the path to the java standard project
	 */

	public static void copyMainFolderIntoMVNStandardMainFolder(String path) throws Exception {
		SystemConfiguration sysConf = new SystemConfiguration("configurationSystem.properties");

		String mavenEmpiteMainFolderPath = System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main";
		File mavenStandardProject = new File(mavenEmpiteMainFolderPath);

		File mainToFind = new File(path);
		File[] list = mainToFind.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isDirectory() && f.getAbsolutePath().contains("src" + File.separator + "main")) {
				walk(f.getAbsolutePath());
				System.err.println("fin");
				FileUtils.copyDirectory(f, mavenStandardProject);
				// FileUtils is a class from Apache
				break;
			} else {
				copyMainFolderIntoMVNStandardMainFolder(f.getAbsolutePath());
			}
		}

	}

	/**
	 * copy the test folder of the java standard project into the mvn test folder
	 *
	 * @param path <i>the path of the java standard project</i>
	 * @throws Exception
	 */

	public static void copyTestFolderIntoMVNStandardTestFolder(String path) throws Exception {
		SystemConfiguration sysConf = new SystemConfiguration("configurationSystem.properties");

		String mavenEmpiteTestFolderPath = System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test";
		File mavenStandardProject = new File(mavenEmpiteTestFolderPath);

		File testToFind = new File(path);
		File[] list = testToFind.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isDirectory() && f.getAbsolutePath().contains("src" + File.separator + "test")) {
				walk(f.getAbsolutePath());
				System.err.println("fin");
				FileUtils.copyDirectory(f, mavenStandardProject);

				// FileUtils is a class from Apache
				break;
			} else {
				copyTestFolderIntoMVNStandardTestFolder(f.getAbsolutePath());
			}
		}

	}

	/**
	 * <i>converts a java project to a maven project</i>
	 *
	 * @param path the path to the javaa standard project
	 */
	public static void convertJavaToMaven(String path) {
		try {
			copyMainFolderIntoMVNStandardMainFolder(path);
			copyTestFolderIntoMVNStandardTestFolder(path);
			System.out.println("convertiiiiiit aaveccccccc successsss");
		} catch (Exception e) {
			System.out.println("Conversion non reussi" + e.getLocalizedMessage());
		}
	}

	public static void moveConvertedMVNIntoFolder(String pathToFolder) throws Exception {
		File destDir = new File(pathToFolder);
		SystemConfiguration sysConf = new SystemConfiguration("configurationSystem.properties");
		String mvnDefault = System.getProperty("user.home")+File.separator+"com.sonar.maven";
		File srcDir = new File(mvnDefault);
		FileUtils.copyDirectory(srcDir, destDir.getAbsoluteFile());
		// Now we reinitialize the maven standard folder
	}

	public static void reinitializeMVNStandardFolder() throws Exception {
		// on se places dans les dossier test et main pui on vide leur contenu
		String mavenEmpiteMainFolderPath = System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"main";
		String mavenEmpiteTestFolderPath = System.getProperty("user.home")+File.separator+"com.sonar.maven"+File.separator+"src"+File.separator+"test";
		File mvnMainFolder = new File(mavenEmpiteMainFolderPath);
		File mvnTestFolder = new File(mavenEmpiteTestFolderPath);
		FileUtils.deleteDirectory(mvnMainFolder);
		FileUtils.deleteDirectory(mvnTestFolder);
	}

	public static void walk(String path) {

		File root = new File(path);
		File[] list = root.listFiles();

		if (list == null)
			return;

		for (File f : list) {
			if (f.isDirectory()) {
				walk(f.getAbsolutePath());
				System.out.println("Dir:" + f.getAbsoluteFile());
			} else {
				System.out.println("File:" + f.getAbsoluteFile());
			}
		}
	}

	/**
	 * return true if the project to the path is a python project
	 *
	 * @param folderPathName the path to the project folder
	 * @return
	 */
	public static boolean isAPythonProject(String folderPathName) {
		boolean rep = false;
		// now recursively check all files of type .py from all folders from this path
		// then add them to extensionsArray
		projectTypePython(folderPathName);
		for (File f : extensionsArray) {
			if (f.getAbsolutePath().contains(".py")) {
				rep = true;
				extensionsArray.clear();
				break;
			}
			;
		}
		return rep;
	}

	/**
	 * used for the method isAPythonProject
	 *
	 * @param path the path to the project
	 */
	public static void projectTypePython(String path) {
		File startingFolder = new File(path);
		File[] list = startingFolder.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isFile() && f.getAbsolutePath().contains(".py")) {
				extensionsArray.add(f);
			} else {
				projectTypePython(f.getAbsolutePath());
			}
		}
	}

	/**
	 * copies tests files from teacher's project to student's project
	 *
	 * @param studentFile the path to the student tests'files
	 * @param teacherFile the path to the teacher tests'files
	 * @throws IOException
	 */
	public static void placeTestFromProjectTeacherToProjectStudentForPythonOrPHP(File studentFile, File teacherFile, String lang)
			throws IOException {
		String test = "test.py";
		if(lang == "php")
			test ="test.php";

		ArrayList<File> mesRecaps = new ArrayList<>();
		File dossierTeacher = new File(teacherFile.getAbsolutePath());
		File dossierStudent = new File(studentFile.getAbsolutePath());
		File dossierTestDeTeacher = new File(dossierTeacher.getAbsolutePath());
		File dossierTestDeStudent = new File(dossierStudent.getAbsolutePath());
		File[] listDesFichiersTeacher = dossierTestDeTeacher.listFiles();
		// on supprime le dossier test de l'enseignant
		for (File f : dossierTestDeStudent.listFiles()) {
			if (f.isFile() && f.getName().toLowerCase().contains(test)) {
				FileUtils.forceDelete(f);
			}
		}
		// On copie maintenant le test de l'enseignant dans le test de l'etudiant
		for (File g : listDesFichiersTeacher) {
			if (g.isFile() && g.getName().toLowerCase().contains(test) ) {
				FileUtils.copyFileToDirectory(g, dossierTestDeStudent);
			}
		}
	}
}
