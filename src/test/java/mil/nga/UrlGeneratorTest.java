package mil.nga;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import mil.nga.interfaces.PDFMergeI;
import mil.nga.util.URIUtils;

/**
 * JUnit tests for the UrlGenerator class.
 * 
 * @author L. Craig Carpenter
 */
public class UrlGeneratorTest implements PDFMergeI {

    Properties testProperties = new Properties();

    @Before
    public void init() {
        testProperties.setProperty(BASE_URL_PROPERTY, "https://localhost/");
        testProperties.setProperty(PDFMergeI.STAGING_DIRECTORY_BASE_PROPERTY, "/tmp");
        testProperties.setProperty(PDFMergeI.STAGING_DIRECTORY_PROPERTY, "/tmp/staging_directory");
    }
    
    @Test
    public void testConstruction() {
        UrlGenerator generator = new UrlGenerator(testProperties);
        assertEquals(generator.getBaseDir(), "/tmp");
        assertEquals(generator.getBaseURL(), "https://localhost/");
    }
    
    @Test
    public void testURLGenerationFromString() {
        UrlGenerator generator = new UrlGenerator(testProperties);
        String URL = generator.toURL("/tmp/staging_directory/abcdefghijkl/merged.pdf");
        assertEquals(URL, "https://localhost/staging_directory/abcdefghijkl/merged.pdf");
    }
    
    @Test
    public void testURLGenerationFromURI() {
        String uriString = "file:///tmp/staging_directory/abcdefghijkl/merged.pdf";
        URI uri = URIUtils.getInstance().getURI(uriString);
        UrlGenerator generator = new UrlGenerator(testProperties);
        String URL = generator.toURL(uri);
        assertEquals(URL, "https://localhost/staging_directory/abcdefghijkl/merged.pdf");
    }
}
