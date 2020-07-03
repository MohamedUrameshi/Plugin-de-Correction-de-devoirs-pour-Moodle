package fr.up5.miage.utility;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;

import fr.up5.miage.notation.App;

/**
 * This class is used to manage files
 */
public class UtilFile{

	/**
	 * Represent the logger for this class
	 */
	private static LoggerConfig logUtilFile;
	
	/**
	 * Class name
	 */
	private static final String utilFileClass = "UtilFile.class";

	/**
	 * Initialization of static variable
	 */
	static{
		logUtilFile = new LoggerConfig(utilFileClass,Level.INFO,false);
		logUtilFile.addAppender(App.fileLog, Level.INFO, null);
	}

	/**
	 * Stock the operating system file separator
	 */
	private final static String FILE_SEPARATOR = File.separator;

	/**
	 * @return the fileSeparator
	 */
	public static String getFileSeparator(){
		return FILE_SEPARATOR;
	}
 
	/**
	 * <i>Treatment of each element of the zip file</i>
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void treatementZipFile(ZipEntry zipEntry, InputStream zis,String destinationPath,byte[] buffer) throws FileNotFoundException, IOException {
		while((zipEntry = ((ZipInputStream) zis).getNextEntry()) != null){

			if (!zipEntry.isDirectory()){
				String fileSourceName = zipEntry.getName();
				File fileSource = new File(fileSourceName);
				String pathDestinationFolder = UtilFolder.pathTreatement(destinationPath);

				File fileToExtract = null;
				if(fileSource.getParent() != null){

					fileToExtract = new File(pathDestinationFolder
							+UtilFolder.pathTreatement(fileSource.getParent())+fileSource.getName());
				}else{
					fileToExtract = new File(pathDestinationFolder+fileSource.getName());
				}

				fileToExtract.getParentFile().mkdirs();
				if (fileToExtract.createNewFile())
				{
					logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("File successfully created"),null);
				}

				//Read the file to extract content in a new file into the destination
				try(OutputStream fos = new FileOutputStream(fileToExtract); )
				{
					if(fos != null){
						int count = -1;
						/*To copy the source file and write into a file destination with the same name */
						while((count = zis.read(buffer))!= -1){
							fos.write(buffer, 0, count);
						}
						logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage(fileToExtract.getName() + " was extracted"),null);
					}
				}
				((ZipInputStream) zis).closeEntry();		
			}		
		}
	} 
	
	/**
	 * Check if we can read, write or execute a file with his path
	 * @param filePathName is the path of the file to check permissions of the file
	 * @return a String which represents the permissions such as "rwx"
	 * @throws FileNotFoundException if the file does not exist
	 */
	public static String checkPermissionsFile(String filePathName) throws FileNotFoundException{

		//Create a File with the path name in parameter
		File file = new File(filePathName); 

		String permissions = new String();

		if(file.exists() && file.isFile()){
			if (file.canRead())
				permissions += "r";
			if (file.canWrite())
				permissions += "w";
			if (file.canExecute())
				permissions += "x";
		}
		else{
			throw new FileNotFoundException("The program can't check permissions for file: "+file.getName()+" because it does not exist");
		}
		return permissions;
	}


	/**
	 * Unzip a ZIP file into a specific destination
	 * @param filePathName : the path of the ZIP file
	 * @param destinationPath : the destination folder path to extract the files
	 * @return true : if the file decompression is finished
	 * @throws FileNotFoundException if the file to unzip wasn't found
	 * @throws IOException : if an I/O error occurs.
	 */
	public static boolean unzipFile(String filePathName, String destinationPath) throws FileNotFoundException {

		boolean unzipFinished = false;

		/* Sources for a good extract :
		 * http://stackoverflow.com/questions/10633595/java-zip-how-to-unzip-folder
		 * http://www.journaldev.com/960/java-unzip-file-example
		 */

	
		byte[] buffer = new byte[4096];
		File fileZip = new File(filePathName);
		if(fileZip.exists() && fileZip.isFile() && checkPermissionsFile(fileZip.getAbsolutePath()).contains("r")){
			ZipEntry zipEntry = null;
			Charset CP866 = Charset.forName("CP866"); //Source : http://stackoverflow.com/questions/5729806/encode-string-to-utf-8
			try (InputStream zis = new  ZipInputStream(new FileInputStream(fileZip), CP866);) {
				if(zis != null){
				
					treatementZipFile(zipEntry,zis,destinationPath,buffer);
				
				}
				unzipFinished = true;
				logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("All content in " + fileZip.getName() + " was extracted with success"),null);
			} catch (IOException e) {
				logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("Error while unzipping " + fileZip.getName()),null);
			}
		}else{
			throw new FileNotFoundException("File '" + fileZip.getName() + " can't be uncompressed because it don't exists or it is not readable or the program enough permissions");
		}
		return unzipFinished;
	}
	
	/**
	 * Zip a directory
	 * @param fileToZip the file/directory to zip
	 * @param fileName the name of of the file to zip
	 * @param zipOut the zip output
	 * @throws IOException
	 */
	public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException{
		// https://www.baeldung.com/java-compress-and-uncompress
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(fileToZip);){
        	 ZipEntry zipEntry = new ZipEntry(fileName);
             zipOut.putNextEntry(zipEntry);
             byte[] bytes = new byte[1024];
             int length;
             while ((length = fis.read(bytes)) >= 0) {
                 zipOut.write(bytes, 0, length);
             }
        } catch (FileNotFoundException e) {
        	logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("The file to zip wasn't found"),null);
		} catch (IOException e) {
			logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("Error encountered while zipping the file"),null);
		}
       
    }


	/**
	 * Copy a file into a specific destination
	 * @param filePathName : path of the file
	 * @param destinationPath : the destination folder path to copy the files
	 * @return true : if the file was copied
	 * @throws IOException if the file can't be written or can't be closed
	 */
	public static boolean copyFile(String filePathName, String destinationPath) throws IOException{

		/* Source for a good copy
		 * http://www.jmdoudoudoux.fr/java/dej/chap-flux.htm
		 */
		
		File sourceFile = new File(filePathName);
		String pathDestinationFolder = UtilFolder.pathTreatement(destinationPath);
		File destinationFile = new File(pathDestinationFolder+sourceFile.getName());	
		
		if(sourceFile.exists() && sourceFile.isFile() && sourceFile.length() !=0 && checkPermissionsFile(sourceFile.getAbsolutePath()).contains("r")){
			try (InputStream fis = new FileInputStream(sourceFile);
				 OutputStream fos = new FileOutputStream(destinationFile);){				
	
				if (destinationFile.createNewFile()) 
				{
					logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("File successfully created"),null);
				}
				
			
				byte[] buffer = new byte[4096];
				int count = -1;

				
				while((count = fis.read(buffer))!= -1){
					fos.write(buffer, 0, count);
				}
						
			} 
			catch (IOException e) {
				logUtilFile.log(utilFileClass,null,null,Level.ERROR,(Message)new SimpleMessage(e.getCause().getMessage()),null);
			}
		}
		else{
			logUtilFile.log(utilFileClass,null,null,Level.ERROR,(Message)new SimpleMessage("File '" + sourceFile.getName() + "' does not exist or can't be read or the program don't get enough permissions"),null);
		}	
		
		return destinationFile.exists();
	}


	/**
	 * Method to rename a file
	 * @param filePathName : the actual path of the file to rename
	 * @param newName : the new name of the file
	 * @return : true : if the file is renamed
	 * @throws FileNotFoundException : if the file does not exist
	 */
	public static boolean renameFile(String filePathName, String newName) throws FileNotFoundException{

		boolean renamed = false;

		File fileSource = new File(filePathName);
		File fileDestination = new File(UtilFolder.pathTreatement(fileSource.getParentFile().getPath())+newName);
		if(fileSource.exists() && fileSource.isFile() && checkPermissionsFile(fileSource.getAbsolutePath()).contains("w")){
			renamed = fileSource.renameTo(fileDestination);//Change the name of the source file
			if(renamed)
				logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage("The rename of: "+fileSource.getName()+" to: " + fileDestination.getName() + " was a success"),null);
			fileDestination.mkdirs();
		}
		return renamed;
	}

	/**
	 * Method to delete a file
	 * @param filePathName : the actual path of the file to rename
	 * @return true : is the file is deleted
	 * @throws FileNotFoundException : if the file does not exist
	 */
	public static boolean deleteFile(String filePathName) throws FileNotFoundException{

		File file = new File(filePathName);
		boolean deleted = false;
		if(file.exists() && file.isFile()){
			deleted = file.delete();
			if(deleted)
				logUtilFile.log(utilFileClass,null,null,Level.INFO,(Message)new SimpleMessage(file.getName()+" was deleted with success"),null);
		}
		else{
			throw new FileNotFoundException("File '"+file.getName()+"' can't be deleted because it does not exist or is open");
		}
		return deleted;
	}


	/**
	 *
	 * @throws IOException
	 */
	public static List<String> readFile(String path) throws IOException
	{
		List <String> list =  new ArrayList();

		FileReader fileReader = new FileReader(path);
		BufferedReader bf = new BufferedReader(fileReader);

		String line = "";

		do{
			line = bf.readLine();
			if(line != null) list.add(line);
		}
		while(line!= null);

		bf.close();
		return list;
	}

}
