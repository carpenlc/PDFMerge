package mil.nga;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.interfaces.PDFMergeI;
import mil.nga.util.URIUtils;

/**
 * This class is mainly for debugging purposes.  As input it takes a 
 * <code>MergeRequest</code> object and serializes the data in JSON format 
 * to an on disk file.  
 * 
 * @author L. Craig Carpenter
 */
public class RequestArchiveService 
        extends PropertyLoader 
        implements PDFMergeI {
	
    /**
     * Set up the LogBack system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            RequestArchiveService.class);
    
    /**
     * Default date format added to generated job IDs.
     */
    private static final String DATE_FORMAT = "yyyyMMdd_HH-mm-ss";
    
    /**
     * Filename extension to add to the output JSON file.
     */
    private static final String EXTENSION = ".json";
    
    /**
     * String to prepend to the front of generated job IDs. 
     */
    private static final String DEFAULT_FILE_PREPEND = "MergeRequest";
    
    /**
     * Calculated path in which the request data will be stored.
     */
    private URI outputPath = null;
    
    /**
     * Default private constructor used in production to enforce the singleton
     * design pattern and ensure that properties are retrieved from the 
     * system properties file. 
     */
    private RequestArchiveService() { 
        super(MERGE_REQUEST_DIRECTORY_PROP);
        try {
            setOutputPath(getProperty(MERGE_REQUEST_DIRECTORY_PROP));
            checkOutputPath();
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Exception message => [ "
                    + pnle.getMessage()
                    + " ].");
        }
    }

    /**
     * Alternate public constructor used to for generating unit tests. 
     * @param props Clients must supply the populated properties 
     * object.
     */
    public RequestArchiveService(Properties props) {
    	if (props != null) {
    		setOutputPath(props.getProperty(MERGE_REQUEST_DIRECTORY_PROP));
            checkOutputPath();
    	}
    }
    
    /**
     * Ensure that the output path exists.
     */
    private void checkOutputPath() {

    	if (getOutputPath() != null) {
    		Path p = Paths.get(getOutputPath());
    		if (!Files.exists(p)) {
    			try {
    				Files.createDirectory(p);
    			}
    			catch (IOException ioe) {
    				LOGGER.error("System property [ "
    						+ MERGE_REQUEST_DIRECTORY_PROP
    						+ " ] is set to directory [ "
    						+ getOutputPath().toString()
    						+ " ] but the directory does not exist and "
    						+ "cannot be created.  Exception message => [ "
    						+ ioe.getMessage()
    						+ " ].");
    				outputPath = null;
    			}
    		}
    	}
    	else {
    		LOGGER.info("Request archive service is disabled.");
    	}
    }
    
    /**
     * If the job ID is not supplied we'll still export the request data but
     * the job ID will be generated from the current system time.
     * 
     * @return A default job ID.
     */
    private String generateFilename() {
        
        StringBuilder sb = new StringBuilder();
        DateFormat  df = new SimpleDateFormat(DATE_FORMAT);
        
        sb.append(DEFAULT_FILE_PREPEND);
        sb.append("_");
        sb.append(df.format(System.currentTimeMillis()));
        
        return sb.toString();
    }
    
    /**
     * Method to assemble the full path to the target output file.
     * 
     * @param jobID The "main" part of the filename.
     * @return The full path to the target output file.
     */
    private URI getFilePath() {
        
    	StringBuilder sb = new StringBuilder();
        
        if (getOutputPath() != null) {
        	sb.append(getOutputPath().toString());
            if (!sb.toString().endsWith(File.separator)) {
                sb.append(File.separator);
            }
        }
        else {
        	LOGGER.warn("Output path is not defined!");
        }
        sb.append(generateFilename());
        sb.append(EXTENSION);
        return URIUtils.getInstance().getURI(sb.toString());
    }
    
    /**
     * Save the input String containing pretty-printed JSON data to an output 
     * file.  The output file path is calculated using the input jobID.
     * 
     * @param request The "pretty-printed" JSON data.
     * @param jobID The job ID (used to calculate the output file name)
     */
    private void saveToFile(String request) {
        
        URI outputFile = getFilePath();
        
        if ((request != null) && (!request.isEmpty())) {
            LOGGER.info("Saving PDF merge request information "
                        + "to [ "
                        + outputFile.toString()
                        + " ].");
            Path p = Paths.get(outputFile);
            try (BufferedWriter writer = 
            		Files.newBufferedWriter(p, Charset.forName("UTF-8"))) {
                writer.write(request);
                writer.flush();
            }
            catch (IOException ioe) {
                LOGGER.error("Unexpected IOException encountered while " 
                        + "attempting to archive the PDF merge request.  "
                        + "Exception message => [ "
                        + ioe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Unable to serialise the PDF merge request.  "
                    + "The output String is null or empty.");
        }
    }
    
    /**
     * External interface used to marshal a BundleRequest into a JSON-based
     * String and then store the results in an on-disk file.
     * 
     * @param request Incoming BundleRequest object.
     * @param jobID The job ID assigned to input BundleRequest object.
     */
    public void archiveRequest(MergeRequest request) {
        if (getOutputPath() != null) {
	        if (request != null) {
	            
	            if (LOGGER.isDebugEnabled()) {
	                LOGGER.debug("Archiving incoming PDF merge request.");
	            }
                String requestString = MergeRequestSerializer
                				.getInstance()
                				.serializePretty(request);
                saveToFile(requestString);
	        }
	        else {
	            LOGGER.error("The input BundleRequest is null.  Unable to "
	                    + "archive the incoming request information.");
	        }
        }
        else {
        	if (LOGGER.isDebugEnabled()) {
        		LOGGER.debug("PDF Merge request archive feature is disabled.");
        	}
        }
    }
    
    /**
     * Return a singleton instance to the UrlGenerator object.
     * @return The UrlGenerator
     */
    public static RequestArchiveService getInstance() {
        return RequestArchiveServiceHolder.getFactorySingleton();
    }
    
    /**
     * Getter method for the target output path.
     * 
     * @return The location to use for storing the incoming request.
     */
    private URI getOutputPath() {
        return outputPath;
    }
    
    /**
     * Setter method for the output path.  
     * 
     * @param dir Location for storing the output data.
     */
    private void setOutputPath(String dir) {
    	if ((dir != null) && (!dir.isEmpty())) { 
    		outputPath = URIUtils.getInstance().getURI(dir);
    		if (outputPath != null) {
    			LOGGER.info("Incoming requests will be archived to [ "
    					+ outputPath.toString()
    					+ " ].");
    		}
    		else {
    			LOGGER.error("System property [ "
    					+ MERGE_REQUEST_DIRECTORY_PROP 
    					+ " ] is set to [ "
    					+ dir 
    					+ " ] which cannot be converted to a URI.  "
    					+ "Incoming requests will not be archived.");
    		}	
    	}
    	else {
    		LOGGER.warn("Output path specified by system property [ "
    				+ MERGE_REQUEST_DIRECTORY_PROP
    				+ " ] is null or empty.  Request archive service "
    				+ "is disabled.");
    	}
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class RequestArchiveServiceHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static RequestArchiveService _factory = new RequestArchiveService();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * 
         * @return The singleton instance of the factory.
         */
        public static RequestArchiveService getFactorySingleton() {
            return _factory;
        }
    }
}
