package mil.nga;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

import mil.nga.exceptions.PDFException;
import mil.nga.util.URIUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for performing the actual merging of a list 
 * of PDF files.  It will accept a list of PDF files and possibly an output 
 * filename.  It will generate a single merged file and return the full path 
 * to the generated output file.
 * 
 * @author L. Craig Carpenter
 *
 */
public class PDFFactory extends FileGenerator {

    /**
     * Static logger for use throughout the class.
     */
    private final Logger LOG = LoggerFactory.getLogger(PDFFactory.class);
    
    /**
     * Default constructor requiring clients to supply a system properties 
     * object.
     * @param props System properties object.
     */
    public PDFFactory(Properties props) {
        super(props);
    }
    
    /**
     * Test a client-supplied filename to ensure it is a valid PDF before
     * adding the file to the merge operation.
     * 
     * @param filename Full path to a candidate file.
     * @return Boolean indicating whether the input file is a valid PDF
     */
    public boolean isValidPDF (URI pdfFile) {
        
        boolean    valid     = false;
        long       startTime = System.currentTimeMillis();
        
        if (pdfFile != null) {
            
            Path p = Paths.get(pdfFile);
            
            try (InputStream is = Files.newInputStream(p);
                    PDDocument pdf = PDDocument.load(is)) {
                valid = true;
            }
            catch (IOException ioe) {
                LOG.warn("IOException encountered while checking the validity "
                        + "of file [ "
                        + pdfFile.toString()
                        + " ].  Error message [ "
                        + ioe.getMessage()
                        + " ].  Target file is not a valid PDF.");
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Validation of file [ "
                        + pdfFile.toString() 
                        + " ] completed in [ "
                        + (System.currentTimeMillis() - startTime)
                        + " ] ms, result was [ "
                        + valid 
                        + " ].");
            }
            
        }
        else {
            LOG.warn("Input filename is null or empty.");
        }
        

        return valid;
    }
    
    
    /**
     * 
     * @param inputFiles List of input PDF files to merge.
     * @param outputFileName The name of the output PDF file to create.
     * @return The URI of the output merged PDF file (may be null).
     */
    public URI merge (List<String> inputFiles, String outputFileName) 
            throws PDFException {
        
        URI               output      = null;
        PDFMergerUtility  pmut        = new PDFMergerUtility();
        List<InputStream> pdfsToMerge = new ArrayList<InputStream>();
        OutputStream      os          = null;
        
        if ((inputFiles != null) && (inputFiles.size() > 0)) {
            
            try {
                
                long validateStartTime = System.currentTimeMillis();
                
                // Ensure the client-supplied list of PDF files is valid.
                for (String pdfFile : inputFiles) {
                    URI uri = URIUtils.getInstance().getURI(pdfFile);
                    if (isValidPDF(uri)) {
                        pdfsToMerge.add(Files.newInputStream(Paths.get(uri)));
                    }
                }
                
                // Log the time it took to validate the PDF files.
                if (LOG.isDebugEnabled()) {
                    LOG.debug("PDF validation operation completed in [ "
                            + (System.currentTimeMillis() - validateStartTime)
                            + " ] ms.");
                }
                
                if (pdfsToMerge.size() >= 1) {
                    
                    long mergeStartTime = System.currentTimeMillis();
                    output = super.getOutputPath(outputFileName);
                    LOG.info("Merging specified PDFs into output file [ "
                            + output.toString()
                            + " ].");
                    os = Files.newOutputStream(Paths.get(output));
                    pmut.addSources(pdfsToMerge);
                    pmut.setDestinationStream(os);
                    pmut.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
                    
                    // Log the time it took to merge the PDF documents.
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("PDF merge operation completed in [ "
                                + (System.currentTimeMillis() - mergeStartTime)
                                + " ] ms.");
                    }
                }
                else {
                    String msg = "The validation of the input PDF files resulted in "
                            + "less than one valid PDF for merging.  Processing "
                            + "will not continue.";
                    LOG.error("Exception to be thrown to the client [ "
                            + msg
                            + " ].");
                    throw new PDFException (msg);
                }
                
            }
            catch (IOException ioe) {
                String msg = "Unexpected IOException encountered while "
                        + "attempting to generate the output merged PDF file."
                        + "  Exception message => [ "
                        + ioe.getMessage() 
                        + " ].";
                LOG.error("Exception to be thrown to the client [ "
                        + msg
                        + " ].");
                throw new PDFException (msg);
            }
            finally {
                // Not sure if the PDFMergerUtility closes the streams so 
                // manually close them here.
                if (os != null) {
                    try { os.close(); } catch (Exception e) {}
                }
                if ((pdfsToMerge != null) && (pdfsToMerge.size() > 0)) {
                    for (InputStream stream : pdfsToMerge) {
                        try { stream.close(); } catch (Exception e) {}
                    }
                }
            }
        }
        else {
            String msg = "There are no input files to merge.";
            LOG.error("Exception to be thrown to the client [ "
                    + msg
                    + " ].");
            throw new PDFException(msg);
        }
        return output;
    }
    
    
    public URI merge (MergeRequest request) throws PDFException {
        return merge(request.getFiles(), request.getFilename());
    }
}
