package mil.nga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple Java bean used to hold the contents of a client-initiated merge
 * request.  This object utilizes JAX-B annotations and is used in conjunction
 * with a RESTful (JAX-RS) service call via POST.
 *  
 * Had some problems deploying this application to JBoss.  Though the Jersey
 * annotations (Xml*) should have been sufficient, JBoss would not 
 * interpret the input as JSON.  We added the the Jackson annotations to work
 * around the issue.
 * 
 * Had more issues deploying to Wildfly.  Had to change from org.codehaus to 
 * com.fasterxml.jackson.
 * 
 * @author L. Craig Carpenter
 */
@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeRequest implements Serializable {
	
	/**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 5756980308604826451L;

	/**
	 * The name to use for the output archive files
	 */
	private String _filename = null;
	
	/**
	 * Annotated list of files that will be processed by the bundler.
	 */
	private List<String> _files = new ArrayList<String>();
	
	/**
	 * No argument constructor required by JAX-B
	 */
	public MergeRequest() {}
	
	/**
	 * Method used to add files to the target internal list of files.
	 * 
	 * @param file A full path to a String
	 */
	public void add(String file) {
		if (_files == null) {
			_files = new ArrayList<String>();
		}
		_files.add(file);
	}
	
	/**
	 * Getter method for the name of the output archive filename.  This is 
	 * an optional parameter.  If it is not supplied, a default filename will
	 * be calculated.
	 * 
	 * @return The suggested name for the output archive files.
	 */
	@XmlElement(name="file_name")
	@JsonProperty(value="file_name")
	public String getFilename() {
		return _filename;
	}
	
	/**
	 * Getter method for the list of files to archive/compress.
	 * 
	 * @return The list of filenames to archive/compress.
	 */
	@XmlElement(name="files")
	@JsonProperty(value="files")
	public List<String> getFiles() {
		return _files;
	}
	
	/**
	 * Setter method for the list of files that will be merged.
	 * 
	 * @param files The list of files to be merged.
	 */
	public void setFiles(List<String> files) {
		if (files != null) {
			_files = files;
		}
	}
	
	/**
	 * Setter method for the name of the output archive filename.  This is 
	 * an optional parameter.  If it is not supplied, a default filename will
	 * be calculated.
	 * 
	 * @param value The suggested name for the output archive files.
	 */
	public void setFilename(String value) {
		_filename = value;
	}
	
	/**
	 * Overridden toString method to dump the request into a human-readable format.
	 * 
	 * @return Printable string
	 */
	public String toString() {
		String        newLine = System.getProperty("line.separator");
		StringBuilder sb      = new StringBuilder();
		sb.append(newLine);
		sb.append("----------------------------------------");
		sb.append("----------------------------------------");
		sb.append(newLine);
		sb.append("          PDF Merge Request: ");
		sb.append(newLine);
		sb.append("----------------------------------------");
		sb.append("----------------------------------------");
		sb.append(newLine);
		sb.append("Output Filename : ");
		sb.append(_filename);
		sb.append(newLine);
		if ((_files != null) && (_files.size() > 0)) {
			for (String file : _files) {
				sb.append("File            : ");
				sb.append(file);
				sb.append(newLine);
			}
		}
		sb.append("----------------------------------------");
		sb.append("----------------------------------------");
		sb.append(newLine);
		return sb.toString();
	}
}

