package mil.nga.interfaces;

/**
 * Interface containing constants used throughout the PDFMerge application.
 * 
 * @author L. Craig Carpenter
 */
public interface PDFMergeI {
    /**
     * The name of the application
     */
    public static final String APPLICATION_NAME = "PDFMerge";
    
    /**
     * Name of the properties file to load.
     */
    public static final String PROPERTY_FILE_NAME = "pdf_merge.properties";
    
    /**
     * Property identifying the IAM role to use for accessing the S3 
     * file system.
     */
    public static final String ACCESS_KEY_PROPERTY = "aws.acess_key";
    
    /**
     * Property identifying the IAM role to use for accessing the S3 
     * file system.
     */
    public static final String IAM_ROLE_PROPERTY = "aws.iam_role";
    
    /**
     * Property identifying the AWS s3 end-point.
     */
    public static final String S3_END_POINT_PROPERTY = "aws.s3_endpoint";
    
    /**
     * Property identifying the IAM role to use for accessing the S3 
     * file system.
     */
    public static final String SECRET_KEY_PROPERTY = "aws.secret_key";
    
    /**
     * Property defining the "base" URL for the output archives (i.e. 
     * the base staging directory will be replaced with this URL allowing
     * HTTP/HTTPS access to the output archives).
     */
    public static final String BASE_URL_PROPERTY = "mergePDF.base_url";
    
    /**
     * System property identifying the target staging directory.  
     */
    public static final String STAGING_DIRECTORY_PROPERTY = 
            "mergePDF.staging_directory";
    
    /**
     * Property defining the "base" staging directory (i.e. the portion
     * of the staging directory that will be replaced with a URL).  This
     * property should exist in the default system properties file.
     */
    public static final String STAGING_DIRECTORY_BASE_PROPERTY = 
            "mergePDF.staging_directory_base";

}
