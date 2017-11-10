package mil.nga.exceptions;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple class to wrap the error message before JAX-RS converts
 * it to JSON.
 * 
 * @author L. Craig Carpenter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessageHolder implements Serializable {

    /**
     * Eclipse-generated serialVersionUID
     */
    private static final long serialVersionUID = 1769111177308345441L;
    private String error = null;
    
    /**
     * Default no-arg constructor required for JAX-B
     */
    public ErrorMessageHolder(){} 
    
    /**
     * Default constructor.
     * @param value The error message string.
     */
    public ErrorMessageHolder (String value) {
        error = value;
    }
    
    /**
     * Getter method for the error message.
     * @return the error message.
     */
    @JsonProperty(value="error")
    public String getError() {
        return error;
    }
    
    /**
     * Setter method for the error message.
     * @param value the error message.
     */
    public void setError(String value) {
        error = value;
    }
    
    /**
     * Concert the simple object to a String representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{ \"error\" : \"");
        sb.append(getError());
        sb.append("\" }");
        return sb.toString();
    }
}
