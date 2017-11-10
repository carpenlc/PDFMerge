package mil.nga.exceptions;

import java.io.Serializable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Simple JAX-RS provider to convert PDFExceptions raised from within the 
 * merge process to an HTML "BAD_REQUEST" (400) with a JSON body to return 
 * to the caller.
 *  
 * @author L. Craig Carpenter
 */
@Provider
public class PDFExceptionHandler 
        implements ExceptionMapper<PDFException>, Serializable {

    /**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 176264663111977573L;

	/**
     * Convert the Exception to a JAX-RS Response object to return to 
     * the caller.
     * 
     * @return HTML error 400 with a JSON message body.
     */
    @Override
    public Response toResponse(PDFException exception) {
        return Response.status(Status.BAD_REQUEST)
                .entity(new ErrorMessageHolder(exception.getMessage()))
                .type(MediaType.APPLICATION_JSON).build();
    }

    
}
