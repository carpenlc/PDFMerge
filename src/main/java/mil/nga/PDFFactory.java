package mil.nga;

import java.io.IOException;
import java.util.Properties;
import java.util.List;

import mil.nga.exceptions.PDFException;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFMergerUtility;

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
	Logger LOG = LoggerFactory.getLogger(PDFFactory.class);
	
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
	public boolean isValidPDF (String filename) {
		
		String     method    = "validPDF() - ";
		boolean    valid     = false;
		long       startTime = System.currentTimeMillis();
		PDDocument pdf       = null;
		
		if ((filename != null) && (!filename.isEmpty())) {
			try {
				pdf = PDDocument.load(filename);
				valid = true;
			}
			catch (IOException ioe) {
				LOG.warn("IOException encountered while checking the validity "
						+ "of file [ "
						+ filename
						+ " ].  Error message [ "
						+ ioe.getMessage()
						+ " ].  Target file is not a valid PDF.");
			}
			finally {
				if (pdf != null) {
					try { pdf.close(); } catch (Exception e) {}
				}
			}
		}
		else {
			LOG.warn(method + "Input filename is null or empty.");
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(method 
					+ "Validation of file [ "
					+ filename 
					+ " ] completed in [ "
					+ (System.currentTimeMillis() - startTime)
					+ " ] ms, result was [ "
					+ valid 
					+ " ].");
		}
		return valid;
	}
	
	
	/**
	 * 
	 * @param inputFiles
	 * @param outputFileName
	 */
	public String merge (List<String> inputFiles, String outputFileName) 
			throws PDFException {
		
		String           method     = "merge() - ";
		String           outputPath = super.getOutputPath(outputFileName);
		PDFMergerUtility pmut       = new PDFMergerUtility();
		int              validPDFs  = 0;
		
		if ((inputFiles != null) && (inputFiles.size() > 0)) {
			
			try {
				
				long validateStartTime = System.currentTimeMillis();
				for (String pdfFile : inputFiles) {
					if (isValidPDF(pdfFile)) {
						validPDFs++;
						pmut.addSource(pdfFile);
					}
				}
				
				// Log the time it took to validate the PDF files.
				if (LOG.isDebugEnabled()) {
					LOG.debug(method 
							+ "PDF validation operation completed in [ "
							+ (System.currentTimeMillis() - validateStartTime)
							+ " ] ms.");
				}
				
				if (validPDFs >= 1) {
					
					long mergeStartTime = System.currentTimeMillis();
					LOG.info(method 
							+ "Merging specified PDFs into output file [ "
							+ outputPath
							+ " ].");
					pmut.setDestinationFileName(outputPath);
					pmut.mergeDocuments();
					
					// Log the time it took to merge the PDF documents.
					if (LOG.isDebugEnabled()) {
						LOG.debug(method 
								+ "PDF merge operation completed in [ "
								+ (System.currentTimeMillis() - mergeStartTime)
								+ " ] ms.");
					}
				}
				else {
					String msg = "The validation of the input PDF files resulted in "
							+ "less than one valid PDF for merging.  Processing "
						    + "will not continue.";
					LOG.error(method 
							+ "Exception to be thrown to the client [ "
							+ msg
							+ " ].");
					throw new PDFException (msg);
				}
				
			}
			catch (COSVisitorException cosEx) {
				String msg = "Unexpected COSVisitorException encountered "
						+ "while attempting to merged the requested PDF "
						+ "files.  Target file was [ "
						+ outputPath 
						+ " ] error message was [ "
						+ cosEx.getMessage() 
						+ " ].";
				LOG.error(method 
						+ "Exception to be thrown to the client [ "
						+ msg
						+ " ].");
				throw new PDFException (msg);
			}
			catch (IOException ioe) {
				String msg = "Unexpected IOException encountered while "
						+ "attempting to generate the output merged PDF file."
						+ "Target file was [ "
						+ outputPath 
						+ " ] error message was [ "
						+ ioe.getMessage() 
						+ " ].";
				LOG.error(method 
						+ "Exception to be thrown to the client [ "
						+ msg
						+ " ].");
				throw new PDFException (msg);
			}
		}
		else {
			String msg = "There are no input files to merge.";
			LOG.error(method 
					+ "Exception to be thrown to the client [ "
					+ msg
					+ " ].");
			throw new PDFException(msg);
		}
		return outputPath;
		
	}
	
	
	public String merge (MergeRequest request) throws PDFException {
		return merge(request.getFiles(), request.getFilename());
	}
}
