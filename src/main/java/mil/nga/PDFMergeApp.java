package mil.nga;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Application extending from <code>javax.ws.rs.core.Application</code>
 * used in conjunction with deploying a RestEasy JAX-RS application to 
 * tomcat.  This class was implemented because tomcat was not handling 
 * the <code>resteasy.scan</code> method for deploying JAX-RS end points.
 * 
 * @author L. Craig Carpenter
 */
@ApplicationPath("/bundler")
public class PDFMergeApp extends Application {

	/**
	 * Overridden method used to provide a list of JAX-RS classes.
	 */
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<Class<?>>(Arrays.asList(PDFMerge.class));
	}
	
}
