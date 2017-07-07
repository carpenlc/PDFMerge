package mil.nga.exceptions;

/**
 * Exception thrown to the caller when an exception is encountered while
 * merging PDF files into a single output file.
 * 
 * @author L. Craig Carpenter 
 *
 */
public class PDFException extends Exception {
	
	/**
	 * Eclipse-generated serialVersionUID
	 */
	private static final long serialVersionUID = 5052200290553305343L;

	/** 
	 * Default constructor requiring a message String.
	 * @param msg Information identifying why the exception was raised.
	 */
	public PDFException(String msg) {
		super(msg);
	}
}
