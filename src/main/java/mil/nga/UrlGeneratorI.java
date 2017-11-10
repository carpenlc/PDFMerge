package mil.nga;

/**
 * Interface file containing constant definitions relevant to the URL 
 * generation algorithm.
 * 
 * @author L. Craig Carpenter
 */
public interface UrlGeneratorI {

    /**
     * Property defining the "base" staging directory (i.e. the portion
     * of the staging directory that will be replaced with a URL).  This
     * property should exist in the default system properties file.
     */
    public static final String STAGING_DIRECTORY_BASE_PROPERTY = 
        "mergePDF.staging_directory_base";
    
    /**
     * Property defining the "base" URL for the output archives (i.e. 
     * the base staging directory will be replaced with this URL allowing
     * HTTP/HTTPS access to the output archives).
     */
    public static final String BASE_URL_PROPERTY = "mergePDF.base_url";
    
}
