package mil.nga;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.interfaces.PDFMergeI;

public class UrlGenerator 
        extends PropertyLoader implements PDFMergeI {
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            UrlGenerator.class);
    
    /**
     * The base staging directory
     */
    private String baseDir = null;
    
    /**
     * The base URL of the archive.
     */
    private String baseUrl = null;
    
    /** 
     * The URI scheme that will be utilized in the output URL.
     */
    private String scheme = null;
    
    /**
     * The URI Authority that will be utilized in the output URL.
     */
    private String authority = null;
    
    /**
     * Default constructor
     */
    public UrlGenerator(Properties props) {
        setBaseDir(props.getProperty(STAGING_DIRECTORY_BASE_PROPERTY));
        setBaseURL(props.getProperty(BASE_URL_PROPERTY));
    }
    
    /**
     * Getter method for the base directory used in the translation.
     * @return The client defined base directory.
     */
    public String getBaseDir() {
        return baseDir;
    }
    
    /**
     * Getter method for the base URL used in the translation.
     * @return The client defined base URL.
     */
    public String getBaseURL() {
        return baseUrl;
    }
    
    /**
     * Typically, the base directory will be read from the system properties.
     * However, this method was implemented to facilitate unit testing.
     * 
     * @param value The value for the base directory.
     */
    private void setBaseDir(String value) {
        baseDir = value;
    }
    
    /**
     * Typically, the base URL will be read from the system properties.
     * However, this method was implemented to facilitate unit testing.
     * 
     * @param value The value for the baseURL.
     */
    private void setBaseURL(String value) {
        baseUrl = value;
        try {
        	URL url = new URL(value);
        	scheme = url.getProtocol();
        	authority = url.getAuthority();
        }
        catch (MalformedURLException mue) {
        	LOGGER.error("The input value for property [ "
        			+ BASE_URL_PROPERTY
        			+ " ] which is [ "
        			+ value
        			+ " ] is not a valid URL. Exception message => [ "
        			+ mue.getMessage()
        			+ " ].");
        }
    }
    
    /**
     * Convert the input local file String to a full URL.
     * TODO: This probably needs to be made fancier.
     * 
     * @param localFile The full path to the local archive file.
     * @return The associated URL
     */
    public String toURL(String localFile) {
    	StringBuilder sb = new StringBuilder();
    	try {
	    	sb.append(scheme);
	    	sb.append("://");
	    	sb.append(authority);
	    	URI uri = new URI(localFile);
	    	sb.append(uri.getPath().replace(getBaseDir(), "").replace('\\', '/'));
    	}
    	catch (URISyntaxException use) {
    		
    	}
        return sb.toString();
    }
    
    /**
     * Convert the input local file String to a full URL.
     * TODO: This probably needs to be made fancier.
     * 
     * @param uri The full URI to the local 
     * @return The associated URL
     */
    public String toURL(URI uri) {
    	StringBuilder sb = new StringBuilder();

    	sb.append(scheme);
    	sb.append("://");
    	sb.append(authority);
    	sb.append(uri.getPath().replace(getBaseDir(), "").replace('\\', '/'));
    	
    	return sb.toString();
    }

}

