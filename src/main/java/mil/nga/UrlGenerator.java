package mil.nga;

import java.util.Properties;

import mil.nga.util.FileUtils;


public class UrlGenerator implements UrlGeneratorI {

    private String _baseDir = null;
    private String _baseUrl = null;
    
    /**
     * Default constructor used to extract default properties from the input
     * Properties object.
     * 
     * @param props System properties object.
     */
    public UrlGenerator(Properties props) {
        if (props != null) {
            setBaseDir(props.getProperty(STAGING_DIRECTORY_BASE_PROPERTY));
            setBaseURL(props.getProperty(BASE_URL_PROPERTY));
        }
    }
    
    /**
     * Getter method for the base directory used in the translation.
     * 
     * @return The client defined base directory.
     */
    public String getBaseDir() {
        return _baseDir;
    }
    
    /**
     * Getter method for the base URL used in the translation.
     * 
     * @return The client defined base URL.
     */
    public String getBaseURL() {
        return _baseUrl;
    }
    
    /**
     * Typically, the base directory will be read from the system properties.
     * However, this method was implemented to facilitate unit testing.
     * 
     * @param value The value for the base directory.
     */
    public void setBaseDir(String value) {
        this._baseDir = value;
    }
    
    /**
     * Typically, the base URL will be read from the system properties.
     * However, this method was implemented to facilitate unit testing.
     * 
     * @param value The value for the baseURL.
     */
    public void setBaseURL(String value) {
        this._baseUrl = value;
    }
    
    /**
     * Convert the input local file String to a full URL.
     * 
     * @param localFile The full path to the local archive file.
     * @return The associated URL
     */
    public String toURL(String localFile) {
        // For Windows modify the file separator
        String target = FileUtils.getEntryPath(localFile, getBaseDir())
                            .replace('\\', '/');
        return getBaseURL() + target;
    }
    
}
