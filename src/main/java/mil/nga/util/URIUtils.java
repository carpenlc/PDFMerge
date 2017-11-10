package mil.nga.util;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class containing methods that create/manage URIs.  The methods 
 * contained here were moved into a separate class because we kept 
 * needing to call them throughout the the application.
 * 
 * @author L. Craig Carpenter
 */
public class URIUtils implements Serializable {
    
    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = -7224296438362779391L;
    
    /**
     * Set up the Log4j system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
            URIUtils.class);
    
    /**
     * Return a singleton instance to the FileGenerator object.
     * @return The FileGenerator
     */
    public static URIUtils getInstance() {
        return URIUtilsHolder.getFactorySingleton();
    }
    
    /**
     * If clients did not supply the "scheme" for the URI, this method is 
     * invoked to generate a URI with the local file system scheme.
     * 
     * @param uri The input URI (which was lacking a scheme).
     * @return Newly constructed URI pointing to the local file system.
     */
    public URI getFileURI(URI uri) {
        URI newURI = null;
        if (uri != null) {
            try {
                newURI = new URI(
                        "file",
                        uri.getAuthority(),
                        uri.getPath(),
                        uri.getFragment(),
                        uri.getQuery());
            }
            // We're making a URI out of a URI here so this exception can 
            // never be thrown here so just eat it.
            catch (URISyntaxException use) { }
        }
        return newURI;
    }
    
    /**
     * Create a full URI based on an input String-based file path.
     * 
     * @param filePath Path to a target file.
     * @return Associated URI to the same target file.  May be null.
     */
    public URI getURI(String filePath) throws FileSystemNotFoundException {
        URI uri = null;
        
        // TODO : Remove
        LOGGER.info("Converting [ " + filePath + " ] to URI.");
        
        if ((filePath != null) && (!filePath.isEmpty())) {
            // Create the URI from the input file path. 
            uri = URI.create(filePath);
            
            // For backwards compatibility, if the scheme is not supplied, we 
            // make the assumption that it is on the default file system.
            if ((uri.getScheme() == null) || (uri.getScheme().isEmpty())) {
                uri = getFileURI(uri);
            }
        }
        else {
            LOGGER.warn("Input filePath is null or not defined.  Returned "
                    + "URI will be null.");
        }
        return uri;
    }
    
    /** 
     * Static inner class used to construct the factory singleton.  This
     * class exploits that fact that inner classes are not loaded until they 
     * referenced therefore enforcing thread safety without the performance 
     * hit imposed by the use of the "synchronized" keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class URIUtilsHolder {
        
        /**
         * Reference to the Singleton instance of the factory
         */
        private static URIUtils instance = new URIUtils();
        
        /**
         * Accessor method for the singleton instance of the factory object.
         * 
         * @return The singleton instance of the factory.
         */
        public static URIUtils getFactorySingleton() {
            return instance;
        }
    }
}
