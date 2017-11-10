package mil.nga;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import mil.nga.interfaces.PDFMergeI;
import mil.nga.util.FileUtils;
import mil.nga.util.URIUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing the logic required to generate the required directories 
 * and output file names which will be used to store the output merged 
 * PDF file.
 * 
 * @author L. Craig Carpenter
 */
public class FileGenerator implements PDFMergeI {

    /**
     * Static logger for use throughout the class.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(FileGenerator.class);
    
    /**
     * The staging area in which output individual merge jobs will be stored.
     */
    private URI stagingArea = null;
    
    /**
     * User-supplied filename to use when creating the output merged PDF.
     */
    private String defaultOutputFile = null;
    
    /**
     * Default constructor used to extract default properties from the input
     * Properties object.
     * 
     * @param props System properties object.
     */
    public FileGenerator(Properties props) {
        if (props != null) {
            setStagingArea(props.getProperty(STAGING_DIRECTORY_PROPERTY));
            setDefaultOutputFile(props.getProperty(OUTPUT_FILE_PROPERTY));
        }
    }
    
    /**
     * Calculate the name for a directory that will be used to store the 
     * output archive files.  Important note:  This method will return a 
     * different value for the archive directory every time it's called.
     *   
     * @return A full path to an output directory.
     */
    public URI getOutputDirectory() throws IOException {
        
        StringBuilder sb      = new StringBuilder();
        String        pathSep = System.getProperty("file.separator");
        
        sb.append(getStagingArea().toString());
        if (!sb.toString().endsWith(pathSep)) {
            sb.append(pathSep);
        }
        
        // Construct the unique directory name
        sb.append(PREFIX);
        sb.append("_");
        sb.append(FileUtils.getHostName().trim());
        sb.append("_");
        sb.append(getUniqueToken());
        
        // If the calculated directory exists, recursively call 
        // getOutputDirectory() until we find one that doesn't exist.
        URI  dirPathUri = URIUtils.getInstance().getURI(sb.toString());
        Path dirPath    = Paths.get(dirPathUri);
        if (Files.exists(dirPath)) {
            return getOutputDirectory();
        }
        else {
            Files.createDirectory(dirPath);
            if (!Files.exists(dirPath)) {
                LOGGER.error("Unable to create the output archive directory.  "
                        + "Attempted to create [ " 
                        + dirPathUri.toString()
                        + " ].");
            }
        }
        return dirPathUri;
    }
    
    /**
     * Calculate a regular expression that can be used to search for 
     * PDFMerge staging directories.  
     * 
     * @return A REGEX used to search for staging directories.
     */
    public static String getRegEx() {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX);
        sb.append("_");
        sb.append(FileUtils.getHostName());
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
    public URI getOutputPath(String outputFile) throws IOException {
        
        StringBuilder sb      = new StringBuilder();
        String        pathSep = System.getProperty("file.separator");
        
        // Append the temporary output directory
        sb.append(getOutputDirectory().toString());
        if (!sb.toString().endsWith(pathSep)) {
            sb.append(pathSep);
        }
        
        if ((outputFile == null) || (outputFile.isEmpty())) {
            sb.append(getDefaultOutputFile());
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
        return URIUtils.getInstance().getURI(sb.toString());
    }
    
    /**
     * Getter method for the default output file name.
     * @return The default output file name.
     */
    public String getDefaultOutputFile() {
    	return defaultOutputFile;
    }
    
    /**
     * Getter method for the location of the staging area to be used.
     * 
     * @return The default staging area. 
     */
    public URI getStagingArea() {
        return stagingArea;
    }
    
    
    /**
     * Setter method for the location of the staging area to be used.
     * 
     * @param value The default staging area. 
     */
    public void setStagingArea(String value) {
        if ((value == null) || (value.isEmpty())) {
        	stagingArea = 
            		URIUtils.getInstance().getURI(
            				System.getProperty("java.io.tmpdir"));
        }
        else {
        	stagingArea = URIUtils.getInstance().getURI(value);
        }
    }
    
    /**
     * Setter method for the default output file.
     * @param value The default output file.
     */
    public void setDefaultOutputFile(String value) {
        if ((value == null) || (value.isEmpty())) {
            defaultOutputFile = DEFAULT_OUTPUT_FILE_NAME;
        }
        else {
            defaultOutputFile = value;
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
