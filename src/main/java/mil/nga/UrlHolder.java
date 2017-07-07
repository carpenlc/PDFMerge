package mil.nga;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple class used to hold the relative URL for a generated output product.
 * The class contains JAX-B annotations for constructing JSON output.
 * 
 * @author L. Craig Carpenter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlHolder implements Serializable {

	/**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = -4990515982881171844L;
	private String url = null;
	
	/**
	 * Default constructor required by JAX-B
	 */
	public UrlHolder() {}
	
	/**
	 * Alternate constructor used by client classes to supply the value for the
	 * URL on construction. 
	 * @param value The full URL to the output product. 
	 */
	public UrlHolder(String value) {
		url = value;
	}
	
	/**
	 * Getter method for the URL value.
	 * @return The URL value.
	 */
	@JsonProperty(value="url")
	public String getURL() {
		return url;
	}
	
	/**
	 * Setter method for the URL value.
	 * @param value The URL value.
	 */
	public void setURL(String value) {
		url = value;
	}
	
	/**
	 * Concert the simple object to a String representation.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ \"url\" : \"");
		sb.append(getURL());
		sb.append("\" }");
		return sb.toString();
	}
}
