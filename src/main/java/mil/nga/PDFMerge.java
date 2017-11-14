package mil.nga;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.MergeRequest;
import mil.nga.FileSystemFactory;
import mil.nga.exceptions.PDFException;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.interfaces.PDFMergeI;
import mil.nga.util.FileUtils;

/**
 * Simple application that will merge PDF documents.
 * 
 * This application is a JAX-RS (i.e. REST-based) end point that will accept 
 * a list of PDF documents that reside on the back-end file system and a 
 * requested file name.  This application will then merge the list of 
 * documents into a single PDF file.  
 * 
 * Two separate merging functions are provided, but the only differ by output.
 * Both functions perform the merge operation synchronously.
 * <li><code>merge</code> Merges the requested documents and returns JSON 
 * containing a valid URL to the merged PDF document.</li>
 * <li><code>mergeAndDownload</code> Merges the requested documents and 
 * forces the browser to download the merged PDF document.  This version 
 * mimics the ColdFusion-based PDF merge process that this code was meant
 * to replace</li> 
 * 
 * Most errors are thrown as an HTML 400 (bad request) with a response body
 * made up of JSON with a relevant error message.
 * 
 * @author L. Craig Carpenter
 */
@Path("")
public class PDFMerge extends PropertyLoader implements PDFMergeI {

    /**
     * Static logger for use throughout the class.
     */
    final Logger LOGGER = LoggerFactory.getLogger(PDFMerge.class);
    
    /**
     * Common header names in which the client CN is inserted
     */
    public static final String[] CERT_HEADERS = {
        "X-SSL-Client-CN",
        "SSL_CLIENT_S_DN_CN",
        "SM_USER",
        "SM_USER_CN"
    };
    
    /** 
     * Container-injected ServletContext object.
     */
    @Context 
    ServletContext _context;
    
    /**
     * Default constructor.
     */
    public PDFMerge() {
        super(PROPERTY_FILE_NAME);
    }

    /**
     * Ensure the S3 file system provider is loaded.
     */
    @PostConstruct
    public void init() {
        // Ensure the S3 file system provider is loaded.
        FileSystemFactory.getInstance().loadS3Filesystem();
    }
    
    /**
     * Try a couple of different headers to see if we can get a user 
     * name for the incoming request.  Most of the time this function 
     * doesn't actually obtain the user because the AJAX callers do 
     * not insert the request headers.
     * 
     * @param headers HTTP request headers
     * @return The username if it could be extracted from the headers
     */
    public String getUser(HttpHeaders headers) {
        
        String method = "getUser() - ";
        String user   = null;
        
        if (headers != null) {
            MultivaluedMap<String, String> map = headers.getRequestHeaders();
            for (String key : map.keySet()) {
                for (String header : CERT_HEADERS) {
                    if (header.equalsIgnoreCase(key)) {
                        user = map.get(key).get(0);
                        break;
                    }
                }
            }
        }
        else {
            LOGGER.warn(method 
                    + "HTTP request headers are not available.");
        }
        if ((user == null) || (user.isEmpty())) {
            user = "unavailable";
        }
        return user;
    }
    
    /**
     * Simple method used to determine whether or not the  
     * application is responding to requests.
     */
    @GET
    @Path("/isAlive")
    public Response isAlive(@Context HttpHeaders headers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Application [ ");
        sb.append(APPLICATION_NAME);
        sb.append(" ] on host [ ");
        sb.append(FileUtils.getHostName());
        sb.append(" ] and called by user [ ");
        sb.append(getUser(headers));
        sb.append(" ] is alive!");
        return Response.status(Status.OK).entity(sb.toString()).build();
    }
    
    /**
     * Accepts a merge request object (deserialized via JAX-B) and generates 
     * a merged PDF.  It then returns a JSON response containing a full URL 
     * to be used to download the generated PDF file.
     * 
     * @param request An incoming PDF merge request.
     * @return A Response object that forces the browser to start a download 
     * of the output PDF.
     */
    @POST
    @Path("/merge")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response merge(MergeRequest request) 
            throws PDFException {
        
        UrlHolder holder = new UrlHolder();
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("merge() invoked.");
        }
        if (request != null) {
            
            LOGGER.info(request.toString());
            RequestArchiveService.getInstance().archiveRequest(request);
            
            try {
                
                PDFFactory pdfFact = new PDFFactory(super.getProperties());
                URI output = pdfFact.merge(request);
                
                if (output != null) {
                    UrlGenerator urlFact = new UrlGenerator(super.getProperties());
                    String url = urlFact.toURL(output);
                    holder.setURL(url);
                }
                else {
                    LOGGER.error("The output file returned by the PDFFactory "
                            + "object is null or empty.");
                    return Response.serverError().build();
                }
            }
            catch (PropertiesNotLoadedException pnle) {
                LOGGER.error("Unable to load the required properties file [ "
                        + PROPERTY_FILE_NAME 
                        + " ].  Exception message => [ "
                        + pnle.getMessage());
                return Response.serverError().build();
            }
        }
        else {
            LOGGER.error("Unable to deserialize the inputPDF merge request.  "
                    + "The merge request was null.");
            return Response.serverError().build();
        }
        return Response.ok(holder, MediaType.APPLICATION_JSON).build();
    }
    
    /**
     * Alternate form of the merge method.  Accepts a merge request object 
     * (de-serialized via JAX-B) and generates a merged PDF.  It then returns
     * a Response object that forces a download.
     * 
     * @param request An incoming PDF merge request.
     * @return A Response object that forces the browser to start a download 
     * of the output PDF.
     */
    @POST
    @Path("/mergeAndDownload")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    public Response mergeAndDownload(MergeRequest request) 
            throws PDFException {
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("mergeAndDownload() invoked.");
        }
        
        if (request != null) {
            
            LOGGER.info(request.toString());
            RequestArchiveService.getInstance().archiveRequest(request);
        
            try {
                PDFFactory pdfFact = new PDFFactory(super.getProperties());
                URI output = pdfFact.merge(request);
                
                if (output != null) {
                    
                    java.nio.file.Path p = Paths.get(output);
                    // If the output file exists, start the download.
                    if (Files.exists(p)) {
                        return Response.ok(p)
                                .header("Content-Disposition", 
                                        "attachment; filename=" + p.toString() + "\"")
                                .build();
                    }
                    else {
                        LOGGER.error("The output file returned by the PDFFactory object "
                                + "does not exist.  File specified [ "
                                + output
                                + " ].");
                        return Response.serverError().build();
                    }
                }
                else {
                    LOGGER.error("The output file returned by the PDFFactory "
                            + "object is null or empty.");
                    return Response.serverError().build();
                }
            }
            catch (PropertiesNotLoadedException pnle) {
                LOGGER.error("Unable to load the required properties file [ "
                        + PROPERTY_FILE_NAME 
                        + " ].  Exception message => [ "
                        + pnle.getMessage()
                        + " ].");
                return Response.serverError().build();
            }
        }
        else {
            LOGGER.error("Unable to deserialize the inputPDF merge request.  "
                    + "The merge request was null.");
            return Response.serverError().build();
        }
    }
}
