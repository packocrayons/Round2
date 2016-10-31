
package tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * FileFactory class loads the given file or writes the file from the given filename 
 * 
 * @author Team 17
 */
public class FileFactory {
	
	private final String directory;

	public FileFactory(String directory) {
		this.directory = directory.concat(File.separator);
	}

	
	
	/**
	 * 
	 * creates or overrides a file, and returns the OutputStream to it
	 * 
	 * thrown IllegalAccessException if the file cannot be created.
	 * 
	 */
	
	public OutputStream writeFile(String fileName) throws IllegalAccessException{
		try{
			return new FileOutputStream(directory+fileName);
		}catch(FileNotFoundException e){
			throw new IllegalAccessException(e.getMessage());
		}
	}

	
	/**
	 * 
	 * checks is the file exists, if it does it opens it.
	 * 
	 * if it does not exist, FileNotFoundException is thrown.
	 * if it does exist, but cannot be opened, IllegalAccessException is thrown.
	 * 
	 */
	public InputStream readFile(String fileName) throws IllegalAccessException, FileNotFoundException{		
		if(new File(directory+fileName).exists()){
			try{
				return new FileInputStream(directory+fileName);
			}catch(FileNotFoundException e){
				throw new IllegalAccessException(e.getMessage());
			}
		}else{
			throw new FileNotFoundException(directory+fileName+" does not exist.");
		}
	}
}


