package mil.nga;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Properties;

import mil.nga.util.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author L. Craig Carpenter
 */
public class FileGenerator implements FileGeneratorI {

	/**
	 * Static logger for use throughout the class.
	 */
	static final Logger LOG = LoggerFactory.getLogger(FileGenerator.class);
	
	private String _stagingArea       = null;
	private String _defaultOutputFile = null;
	
	/**
	 * Default constructor used to extract default properties from the input
	 * Properties object.
	 * 
	 * @param props System properties object.
	 */
	public FileGenerator(Properties props) {
		if (props != null) {
			setStagingArea(props.getProperty(STAGING_AREA_PROPERTY));
			setDefaultOutputFile(props.getProperty(OUTPUT_FILE_PROPERTY));
		}
	}
	
	
	/**
	 * Obtain the host name from the server hosting the application.  If the
	 * actual host name cannot be retrieved, the string localhost is returned.
	 * 
	 * @return A host name
	 */
	public static String getHostname() {
		String method   = "getHostname() - ";
		String hostname = "localhost";
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException uhe) {
			LOG.warn(method 
					+ "Unable to obtain the hostname.  Exception ["
					+ uhe.getMessage()
					+ "].");
		}
		return FileUtils.removeExtensions(hostname.trim());
	}
	
	
	/**
	 * Calculate the name for a directory that will be used to store the 
	 * output archive files.  Important note:  This method will return a 
	 * different value for the archive directory every time it's called.
	 *   
	 * @return A full path to an output directory.
	 */
	public String getOutputDirectory() {
		
		String        method  = "getOutputDirectory() - ";
		StringBuilder sb      = new StringBuilder();
		String        pathSep = System.getProperty("file.separator");
		
		sb.append(getStagingArea());
		if (!sb.toString().endsWith(pathSep)) {
			sb.append(pathSep);
		}
		
		// Construct the unique directory name
		sb.append(PREFIX);
		sb.append("_");
		sb.append(getHostname());
		sb.append("_");
		sb.append(getUniqueToken());
		
		// Make sure the directory is unique
		File file = new File(sb.toString());
		if (file.exists()) {
			return getOutputDirectory();
		}
		else {
			// Updated to ensure directory permissions are wide open
			file.setExecutable(true, false);
			file.setReadable(true, false);
			file.setWritable(true, false);
			file.mkdir();
			if (!file.exists()) {
				LOG.error(method
						+ "Unable to create the output archive directory.  "
						+ "Attempted to create [" 
						+ file.getAbsolutePath()
						+ "].");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Calculate a regular expression that can be used to search for 
	 * bundler staging directories.  
	 * @return A REGEX used to search for staging directories.
	 */
	public static String getRegEx() {
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIX);
		sb.append("_");
		sb.append(getHostname());
		sb.append("_");
		sb.append("[A-Z0-9]{");
		sb.append(2*TOKEN_LENGTH);
		sb.append("}+");
		return sb.toString();
	}
	
	/**
	 * Create the full path to the target output file.
	 * 
	 * @param outputFile The suggested name of the output file.
	 * @return The full path to the target output file.
	 */
	public String getOutputPath(String outputFile) {
		
		StringBuilder sb      = new StringBuilder();
		String        pathSep = System.getProperty("file.separator");
		
		// Append the temporary output directory
		sb.append(getOutputDirectory());
		if (!sb.toString().endsWith(pathSep)) {
			sb.append(pathSep);
		}
		
		if ((outputFile == null) || (outputFile.isEmpty())) {
			sb.append(DEFAULT_OUTPUT_FILE_NAME);
		}
		else {
			if (outputFile.endsWith(PDF_FILE_EXTENSION)) {
				sb.append(outputFile);
			}
			else {
				sb.append(
						FileUtils.removeExtensions(outputFile) + 
						PDF_FILE_EXTENSION);
			}
		}
		return sb.toString();
	}
	
	
	/**
	 * Getter method for the location of the staging area to be used.
	 * @return The default staging area. 
	 */
	public String getStagingArea() {
		return _stagingArea;
	}
	
	
	/**
	 * Setter method for the location of the staging area to be used.
	 * @param value The default staging area. 
	 */
	public void setStagingArea(String value) {
		if ((value == null) || (value.isEmpty())) {
			_defaultOutputFile = System.getProperty("java.io.tmpdir");
		}
		else {
			_stagingArea = value;
		}
	}
	
	
	/**
	 * Setter method for the default output file.
	 * @param value The default output file.
	 */
	public void setDefaultOutputFile(String value) {
		if ((value == null) || (value.isEmpty())) {
			_defaultOutputFile = DEFAULT_OUTPUT_FILE_NAME;
		}
		else {
			_defaultOutputFile = value;
		}
	}
	
	
	/**
	 * Calculate a unique token used to make directories and/or filenames 
	 * unique.
	 * @return A unique string.
	 */
	public String getUniqueToken() {
		return FileUtils.generateUniqueToken(TOKEN_LENGTH);
	}
}
