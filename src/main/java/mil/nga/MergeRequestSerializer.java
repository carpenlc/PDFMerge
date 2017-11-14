package mil.nga;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Class providing methods to serialize/deserialize <code>MergeRequest</code> 
 * messages.
 * 
 * @author L. Craig Carpenter
 */
public class MergeRequestSerializer {

    /**
     * Set up the LogBack system for use throughout the class
     */        
    private static final Logger LOGGER = LoggerFactory.getLogger(
    		MergeRequestSerializer.class);
    
    /** 
     * DateFormat object used when serializing/deserializing dates.  This 
     * overrides the default behavior which depends on the type of date being
     * serialized/deserialized.
     */
    private static final DateFormat dateFormatter = 
            new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    
    /**
     * Ensure that all times are in GMT
     */
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    /**
     * Private constructor enforcing the singleton design pattern.
     */
    private MergeRequestSerializer() {}
    
    /**
     * Accessor method for the singleton instance of the 
     * <code>BundlerMessageSerializer</code> class.
     * 
     * @return The singleton instance of the 
     * <code>BundlerMessageSerializer</code> class.
     */
    public static MergeRequestSerializer getInstance() {
        return MergeRequestSerializerHolder.getSingleton();
    }    
    
    /**
     * Convert the input object into JSON format.  This version of the 
     * serialization process is meant for generating a more human-readable
     * output.
     * 
     * @param obj A populated object.
     * @return A JSON String representation of the input Object.
     */
    public String serializePretty(Object obj) {
        
        String json = "null";
        
        if (obj != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                json = mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(obj);
            }
            catch (JsonProcessingException jpe) {
                LOGGER.error("Unexpected JsonProcessingException encountered "
                        + "while attempting to marshall the input "
                        + "object to JSON.  Exception message => [ "
                        + jpe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input object is null.  Unable to "
                    + "marshall the object to JSON.");
        }
        return json;
    }
    
    /**
     * Convert the input object into JSON format. 
     * 
     * @param obj A populated object.
     * @return A JSON String representation of the input Object.
     */
    public String serialize(Object obj) {
        
        String json = "null";
        
        if (obj != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setDateFormat(dateFormatter);
                json = mapper.writeValueAsString(obj);
            }
            catch (JsonProcessingException jpe) {
                LOGGER.error("Unexpected JsonProcessingException encountered "
                        + "while attempting to marshall the input "
                        + "object to JSON.  Exception message [ "
                        + jpe.getMessage()
                        + " ].");
            }
        }
        else {
            LOGGER.warn("Input object is null.  Unable to "
                    + "marshall the object to JSON.");
        }
        return json;
    }
    
    /**
     * Static inner class used to construct the Singleton object.  This class
     * exploits the fact that classes are not loaded until they are referenced
     * therefore enforcing thread safety without the performance hit imposed
     * by the <code>synchronized</code> keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class MergeRequestSerializerHolder {
        
        /**
         * Reference to the Singleton instance of the 
         * <code>MergeRequestSerializer</code>.
         */
        private static MergeRequestSerializer _instance = 
                new MergeRequestSerializer();
    
        /**
         * Accessor method for the singleton instance of the 
         * <code>MergeRequestSerializer</code>.
         * @return The Singleton instance of the 
         * <code>MergeRequestSerializer</code>.
         */
        public static MergeRequestSerializer getSingleton() {
            return _instance;
        }
    }
}
