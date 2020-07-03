/*Links that helped for coding:
 * http://opencsv.sourceforge.net/apidocs/com/opencsv/CSVWriter.html
 * http://howtodoinjava.com/3rd-party/parse-read-write-csv-files-opencsv-tutorial/
 * https://openclassrooms.com/forum/sujet/probleme-pour-arrondir-a-2-chiffres-apres-la-virgule-71877
 * http://forum.hardware.fr/hfr/Programmation/Java/repertoire-courant-application-sujet_44615_1.htm
 */

package fr.up5.miage.utility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.text.DecimalFormat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import com.opencsv.CSVWriter;

import fr.up5.miage.notation.App;

/**
 * This class provides some features to create and write in file in CSV format
 */
public class UtilCSVFile {

	/**
	 * Represent the logger for this class
	 */
	private static LoggerConfig logCsvFile;

	/**
	 * Initialization of static variable
	 */
	static{
		logCsvFile = new LoggerConfig("UtilCSVFile.class",Level.INFO,false);
		logCsvFile.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * This is a String of the absolute path of the current Student project
	 */
	private String absolutePath;

	/**
	 * Represent the name of the CSV file
	 */
	private String fileName;

	/**
	 * Represent the header of the CSV file with the list of column names which will be written in the CSV file
	 */
	private String[] csvFileHeader;

	/**
	 * Represent the ordered list of the parameters which will be added to the CSV file
	 */
	private ArrayList<String> orderCsvFileHeader;

	/**
	 * Represent the file that will be modify
	 */
	private File csvFile;

	/**
	 * Constructor of class CSVFile, that takes three parameters
	 * @param absolutePath is the path where the the CSV file will be created and modified
	 * @param fileName is the name of the CSV file
	 * @param csvFileHeaderModel contains the parameters that will be added as header to CSV file
	 * @throws IOException if the creation of the file has a problem
	 * @throws BadNameCsvFileException if the fileName is not long enough or is not well written
	 */
	public UtilCSVFile(String absolutePath, String fileName, Set<String> csvFileHeaderModel ) throws IOException, BadNameCsvFileException{ 
		
		this.absolutePath = absolutePath;
		this.orderCsvFileHeader = new ArrayList<String>();
		this.fileName = fileName;
		
		System.err.println("absolute path "+absolutePath);
		
		
		orderCsvFileHeader.add("Name of Project");
		for (String columnName : csvFileHeaderModel){
			orderCsvFileHeader.add(columnName);
		}
		orderCsvFileHeader.add("Rules");
		orderCsvFileHeader.add("Final Grade");
		csvFileHeader = toStringList(this.orderCsvFileHeader);
		
		
		this.createCSVFile();
	}


	/**
	 * Method to use when we have to create a new CSV File
	 * @throws IOException if the creation of the file has a problem
	 * @throws BadNameCsvFileException if the fileName is not long enough or is not well written
	 */
	public void createCSVFile() throws IOException, BadNameCsvFileException{
		this.csvFile = new File(this.absolutePath+File.separator+this.fileName);
		int longueurChaine = this.fileName.length();
		String subStr = this.fileName.substring(longueurChaine-4, longueurChaine);
		if(this.fileName.length() >= 5 && subStr.equals(".csv")){
			if(!csvFile.exists()){
				try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile))){
					writer.writeNext(csvFileHeader);
				} 
			}
		}
		else{
			throw new BadNameCsvFileException("The file name is not respected");
		}
	} 


	/**
	 * Method to write a line in the CSV File, value by value, if the CSV is already created
	 * @param map contains the values of a project according to the parameters in the header
	 * @param projectName is the name of the current project analyzed
	 * @throws IOException if the writing in the file has a problem or if the file is not created yet
	 */
	public void writeLine(HashMap<String,Float> map, String projectName) throws IOException{
		StringBuilder sb = new StringBuilder();
		String[] list;
		
		
		DecimalFormat df = new DecimalFormat("0.##");

		
		if (map!=null){
			list = new String[map.size()+1];
			list[0]=projectName;
			Iterator<String> itOrderHeader = orderCsvFileHeader.iterator();
			itOrderHeader.next();
			for (int i = 1; i < orderCsvFileHeader.size()-1; i++)
				list[i] = df.format(map.get(itOrderHeader.next()));
			list[map.size()] = df.format(map.get("FinalGrade"));

			if(csvFile.exists()){
				try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile.getAbsolutePath(), true))){
					if(list.length == csvFileHeader.length)
						writer.writeNext(list);
					else if(list.length > 0){
						try (PrintWriter pw = new PrintWriter(new FileOutputStream(new File(csvFile.getAbsolutePath()),true))){
							sb.append(list[0] + "\n");
							pw.append(sb.toString());
						}
					}
				} 
			}
		}
		else{
			try (CSVWriter writer = new CSVWriter(new FileWriter(csvFile.getAbsolutePath(), true))){
				list = new String[1];
				list[0] = projectName;
				writer.writeNext(list);
			}
		}
		logCsvFile.log("UtilCSVFile.class",null,null,Level.INFO,(Message)new SimpleMessage("Add result of "+projectName+" project in CSVFile"),null);
	}


	/**
	 * Method to convert an ArrayList to a String List
	 * @param map the ArrayList to convert
	 * @return stringArray will return the string list
	 */
	public static String[] toStringList(ArrayList<String> map){
		String[] stringArray = new String[map.size()];
		Iterator<String> it = map.iterator();
		int i = 0;
		while(it.hasNext()){
			stringArray[i] = ""+it.next()+"";
			i++;
		}
		return stringArray;
	}
	
	/**
	 * This method deletes the CSV file
	 */
	public void deleteCsvFile(){
		csvFile.delete();
	}
	
	/**
	 * Return the path of csvFile
	 * @return the path of csvFile
	 */
	public String getAbsolutePath(){
		return this.csvFile.getAbsolutePath();
	}
}
