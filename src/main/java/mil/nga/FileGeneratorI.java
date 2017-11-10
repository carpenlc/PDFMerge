package mil.nga;

/**
 * Interface file containing constant definitions relevant to the output 
 * file generation algorithm.
 * 
 * @author L. Craig Carpenter
 */
public interface FileGeneratorI {
    
    /**
     * Length of unique token utilized in the construction of a unique
     * staging area for output files. 
     */
    public static final int TOKEN_LENGTH = 8;
    
    public static final String PREFIX = "nga";
    
    public static final String STAGING_AREA_PROPERTY = 
            "mergePDF.staging_directory";
    
    public static final String OUTPUT_FILE_PROPERTY = 
            "mergePDF.default_output_filename";
    
    public static final String DEFAULT_OUTPUT_FILE_NAME = 
            "merged.pdf";
    
    public static final String PDF_FILE_EXTENSION = 
            ".pdf";
}
