package mil.nga;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import mil.nga.PropertyLoader;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.interfaces.PDFMergeI;

/**
 * This class is responsible for ensuring that all required NIO2 file systems
 * are available.
 * 
 * @author L. Craig Carpenter
 */
public class FileSystemFactory 
        extends PropertyLoader 
        implements PDFMergeI {

    /**
     * Set up the Log4j system for use throughout the class
     */        
    static final Logger LOGGER = LoggerFactory.getLogger(
            FileSystemFactory.class);
    
    /**
     * The default endpoint for AWS S3.
     */
    private static final String DEFAULT_S3_ENDPOINT = "s3.amazonaws.com";
    
    /**
     * Flag indicating whether or not the S3 file system has been loaded.  This 
     * was added to ensure that in any application the file system is loaded 
     * once and only once.
     */
    private static boolean s3FileSystemLoaded = false;
    
    /**
     * The IAM role that will be used to for authentication to AWS.
     */
    private String iamRole;
    
    /**
     * The access key that will be used to for authentication to AWS.
     */
    private String accessKey;
    
    /**
     * The secret key that will be used to for authentication to AWS.
     */
    private String secretKey;
    
    /**
     * This is the default host name for the s3 end-point.  
     */
    private String s3EndPoint;
    
    /**
     * Default constructor used to load the required properties.
     */
    private FileSystemFactory() { 
        super(PROPERTY_FILE_NAME);
        
        String accessKey  = null;
        String iamRole    = null;
        String s3EndPoint = null;
        String secretKey  = null;
        
        try {
            iamRole    = getProperty(IAM_ROLE_PROPERTY);
            s3EndPoint = getProperty(S3_END_POINT_PROPERTY);
            accessKey  = getProperty(ACCESS_KEY_PROPERTY);
            secretKey  = getProperty(SECRET_KEY_PROPERTY);
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.warn("An unexpected PropertiesNotLoadedException " 
                    + "was encountered.  Please ensure the application "
                    + "is properly configured.  Exception message [ "
                    + pnle.getMessage()
                    + " ].");
        }
        setIAMRole(iamRole);
        setAccessKey(accessKey);
        setSecretKey(secretKey);
        setS3EndPoint(s3EndPoint);
    } 
    
    
    /**
     * Method to list the available <code>FileSystemProvider</code> objects.
     */
    public void listFileSystemsAvailable() {
        List<FileSystemProvider> availableProviders = 
                FileSystemProvider.installedProviders();
        for (FileSystemProvider provider : availableProviders) {
            LOGGER.info("Provider scheme => " + provider.getScheme());
        }
    }
    
    /**
     * Ensure that the S3 FileSystem provider is loaded and available.  Either 
     * the IAM Role must be defined OR BOTH the access key and secret key must
     * be defined.  
     */
    public void loadS3Filesystem() throws IllegalStateException {
        
        String         uriString = "s3://" + getS3EndPoint() + "/";
        Map<String, ?> env       = null;
        
        // Ensure the s3 filesystem is not already loaded.
        if (!s3FileSystemLoaded) {
            
            LOGGER.info("Initializing the NIO2 S3 file system provider");
            
            if ((getIAMRole() != null) && 
                    (!getIAMRole().isEmpty())) {
                
                env = ImmutableMap.<String, Object> builder()
                        .put(com.upplication.s3fs.AmazonS3Factory.IAM_ROLE, 
                            getIAMRole())
                        .build();
        
            }
            else if ((getAccessKey() != null) && 
                        (!getAccessKey().isEmpty()) && (getSecretKey() != null) && 
                        (!getSecretKey().isEmpty())) {
                
                env = ImmutableMap.<String, Object> builder()
                        .put(com.upplication.s3fs.AmazonS3Factory.ACCESS_KEY, 
                            getAccessKey())
                        .put(com.upplication.s3fs.AmazonS3Factory.SECRET_KEY, 
                            getSecretKey())
                        .build();
                
            } 
            else {
                throw new IllegalStateException("AWS authentication "
                        + "properties not defined.  Either [ "
                        + IAM_ROLE_PROPERTY
                        + " ] or BOTH [ "
                        + ACCESS_KEY_PROPERTY 
                        + " ] and [ "
                        + SECRET_KEY_PROPERTY
                        + " ] must be defined.");
                
            }
            
            if (env != null) {
                
                try {
                    if (LOGGER.isDebugEnabled()) {
                        
                        StringBuilder sb   = new StringBuilder();
                        Set<String>   keys = env.keySet();
                        
                        for (String key : keys) {
                            sb.append(" Key => [ ");
                            sb.append(key);
                            sb.append(" ], Value => [ ");
                            sb.append((String)env.get(key));
                            sb.append(" ] ");
                        }
                        LOGGER.debug("Initializing S3 filesystem with => "
                                + sb.toString());
                    }
                    
                    // Add the s3 file system provider
                    FileSystems.newFileSystem(
                        new URI(uriString), 
                        env, 
                        Thread.currentThread().getContextClassLoader());
                    
                    // If no exceptions, the file system provider was loaded 
                    // successfully.
                    s3FileSystemLoaded = true;
                    
                    LOGGER.info("NIO2 s3 File System provider initialized successfully.");
                    
                } 
                catch (IOException ioe) {
                    LOGGER.error("Unexpected IOException while loading the "
                            + "S3 filesystem provider.  Exception "
                            + "message => [ "
                            + ioe.getMessage()
                            + " ].");
                }
                // URISyntaxException cannot happen because we're converting a 
                // static String.  Just eat the exception.
                catch (URISyntaxException use) { }
            }
            else {
                LOGGER.error("Unexpected situation encountered in which the "
                        + "Map object used to authenticate to AWS is "
                        + "null.  Unable to load the S3 file system.");
            }
        }
        else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("s3 file system provider already loaded.");
            }
        }
    }
    
    /**
     * Getter method for the access key that will be used for authentication to
     * AWS.
     * @return The access key.
     */
    public String getAccessKey() {
        return accessKey;
    }

    /**
     * Getter method for the IAM Role that will be used for authentication to
     * AWS.
     * @return The IAM Role to use.
     */
    public String getIAMRole() {
        return iamRole;
    }
    
    /** 
     * Getter method for the target S3 end-point.
     * @return The target S3 end-point.
     */
    public String getS3EndPoint() {
        return s3EndPoint;
    }
    
    /**
     * Getter method for the secret key that will be used for authentication to
     * AWS.
     * @return The secret key.
     */
    public String getSecretKey() {
        return secretKey;
    }
    
    /**
     * Getter method for the singleton instance of the FileSystemFactory.
     * @return Handle to the singleton instance of the FileSystemFactory.
     */
    public static FileSystemFactory getInstance() {
        return FileSystemFactoryHolder.getFactorySingleton();
    }
    
    /**
     * Setter method for the access key that will be used for authentication to
     * AWS.
     * @param value The access key to provide for authentication.
     */
    public void setAccessKey(String value) {
        accessKey = value;
    }
    
    /**
     * Setter method for the IAM Role that will be used for authentication to
     * AWS.
     * @param value The IAM Role to use.
     */
    public void setIAMRole(String value) {
        iamRole = value;
    }
    
    /**
     * Setter method for the S3 end-point that will be used for construction 
     * of the file system.
     * @param value The target S3 end-point.
     */
    public void setS3EndPoint(String value) {
        if ((value == null) || (value.isEmpty())) {
            s3EndPoint = DEFAULT_S3_ENDPOINT;
        }
        else {
            s3EndPoint = value.trim();
        }
    }
    
    /**
     * Setter method for the secret key that will be used for authentication to
     * AWS.
     * @param value The secret key to provide for authentication.
     */
    public void setSecretKey(String value) {
        secretKey = value;
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class FileSystemFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static FileSystemFactory factory = new FileSystemFactory();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * @return The singleton instance of the factory.
         */
        public static FileSystemFactory getFactorySingleton() {
            return factory;
        }
    }
    
    public static void main(String[] args) {
        FileSystemFactory.getInstance().listFileSystemsAvailable();
    }
}
