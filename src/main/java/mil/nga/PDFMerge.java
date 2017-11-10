package mil.nga;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.MergeRequest;
import mil.nga.exceptions.PDFException;

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
public class PDFMerge {

    /**
     * Static logger for use throughout the class.
     */
    Logger LOG = LoggerFactory.getLogger(PDFMerge.class);

    private Properties _pdfMergeProps = new Properties();
    
    public static final String PROPERTIES_FILE_NAME = "pdf_merge.properties";
    
    /** 
     * Container-injected ServletContext object.
     */
    @Context ServletContext _context;
    
    /**
     * Initialization method used to load the system properties.
     */
    @PostConstruct 
    private void init() {
        
        String      method  = "init() - ";
        String      newLine = System.getProperty("line.separator");
        InputStream is      = null;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(method + "method invoked.");
        }
        
        if (_context != null) {
            try {
                
                is = _context.getResourceAsStream(
                        "/WEB-INF/" + PROPERTIES_FILE_NAME);
                _pdfMergeProps.load(is);
                
            }
            catch (IOException ioe) {
                
                LOG.error(method 
                        + "Unexpected IOException encountered while "
                        + "attempting to load the properties file [ "
                        + "/WEB-INF/" + PROPERTIES_FILE_NAME
                        + " ].");
                _pdfMergeProps = null;
                
            }
            finally {
                
                if (is != null) {
                    try { is.close(); } catch (Exception e) {}
                }
                
            }
            if (_pdfMergeProps != null) {
                
                StringBuilder sb = new StringBuilder();
                for (String key : _pdfMergeProps.stringPropertyNames()) {
                    sb.append("  key [ ");
                    sb.append(key);
                    sb.append(" ] => value [ ");
                    sb.append(_pdfMergeProps.getProperty(key));
                    sb.append(" ]");
                    sb.append(newLine);
                }
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug(method 
                            + "System properties: " 
                            + newLine
                            + sb.toString());
                }
                
            }
        }
        else {
            LOG.error(method 
                    + "The container failed to inject the ServletContext.  "
                    + "Unable to continue.");
        }
    }

    
    /**
     * Not really of any use.  Added for testing purposes.
     * @return A hard-coded String object.
     */
    @GET
    @Path("/info")
    public String info() {
        String method = "info() - ";
        if (LOG.isDebugEnabled()) {
            LOG.debug(method + "method invoked.");
        }
        return new String("PDFMerge application.");
    }
    
    /**
     * Accepts a merge request object (unmarshalled via JAX-B) and generates 
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
    public UrlHolder merge(MergeRequest request) 
            throws PDFException {
        
        String    method = "merge() - ";
        UrlHolder holder = new UrlHolder();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(method + "method invoked.");
        }
        
        LOG.info(request.toString());
        
        PDFFactory pdfFact = new PDFFactory(_pdfMergeProps);
        String output = pdfFact.merge(request);
        
        if ((output != null) && (!output.isEmpty())) {
        
            UrlGenerator urlFact = new UrlGenerator(_pdfMergeProps);
            String url = urlFact.toURL(output);
            holder.setURL(url);
            
        }
        else {
            LOG.error(method 
                    + "The output file returned by the PDFFactory object is "
                    + "null or empty.");
        }
        return holder;
    }
    
    /**
     * Alternate form of the merge method.  Accepts a merge request object 
     * (unmarshalled via JAX-B) and generates a merged PDF.  It then returns
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
        
        String method = "mergeAndDownload() - ";
        
        LOG.info(request.toString());
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(method + "method invoked.");
        }
        
        PDFFactory pdfFact = new PDFFactory(_pdfMergeProps);
        String output = pdfFact.merge(request);
        
        if ((output != null) && (!output.isEmpty())) {
            
            File pdfFile = new File(output);
            
            // If the output file exists, start the download.
            if (pdfFile.exists()) {
                return Response.ok(pdfFile)
                        .header("Content-Disposition", 
                                "attachment; filename=" + pdfFile.getName() + "\"")
                        .build();
            }
            else {
                LOG.error(method 
                        + "The output file returned by the PDFFactory object does "
                        + "not exist.  File specified [ "
                        + output
                        + " ].");
                return Response.serverError().build();
            }
        }
        else {
            LOG.error(method 
                    + "The output file returned by the PDFFactory object is "
                    + "null or empty.");
            return Response.serverError().build();
        }
    }
    
}
