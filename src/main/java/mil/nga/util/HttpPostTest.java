package mil.nga.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpPostTest {

    /**
     * Static logger for use throughout the class.
     */
    public static final Logger LOG = LoggerFactory.getLogger(
            HttpPostTest.class);
    
    /**
     * NIPR test environment servers
     */
    public static String[] TARGET_URLs = {
        "http://ndwqsngwlvapp01.arn.gov:8080/PDFMerge/rest/merge",
        "http://ndwdsegdlvgeo01.arn.gov:9080/PDFMerge/rest/merge",
        "http://ndwqsngwlvapp01.arn.gov:9080/PDFMerge/rest/mergeAndDownload"
    };
    

    private String getJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"file_name\" : \"output_pdf_file.pdf\",");
        sb.append("\"files\" : [");
        sb.append("\"/mnt/fbga/datasets/2017_10_12/supplements/PAA_12OCT17.pdf\",");
        sb.append("\"/mnt/fbga/datasets/2017_10_12/supplements/IFR_BOOK_12OCT17.pdf\"");
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }
    
    
    public void execute(String target) {
        
        String     method       = "execute() - ";
        String     jsonResponse = null;
        HttpClient client       = new DefaultHttpClient();
        HttpPost   request      = new HttpPost(target);
        String     body         = getJSON();
        
        try {
            
            StringEntity params = new StringEntity(body);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            
            LOG.info(method 
                    + "Executing HTTP POST to target [ "
                    + target
                    + " ] with message body [ " 
                    + body
                    + " ].");
            
            HttpResponse response = client.execute(request);
            
            LOG.info(method 
                    + "Server response [ "
                    + response.toString()
                    + " ].");
            
            HttpEntity responseEntity = response.getEntity();
            
            if (responseEntity != null) {
                long responseLength = responseEntity.getContentLength();
                if (responseLength > 0) {
                    jsonResponse = EntityUtils.toString(responseEntity);
                    LOG.info(method 
                            + "JSON response from server [ "
                            + jsonResponse
                            + " ].");
                }
                else {
                    LOG.warn(method 
                            + "Response from server has a content length "
                            + "of [ "
                            + responseLength
                            + " ].");
                }
                
            }
        }
        catch (UnsupportedEncodingException uee) {
            LOG.error(method 
                    + "Unexpected UnsupportedEncodingException raised wile "
                    + "executing the HTTP POST.  Exception message [ "
                    + uee.getMessage()
                    + " ].");
            if (LOG.isDebugEnabled()) {
                uee.printStackTrace();
            }
        }
        catch (ClientProtocolException cpe) {
            LOG.error(method 
                    + "Unexpected ClientProtocolException raised wile "
                    + "executing the HTTP POST.  Exception message [ "
                    + cpe.getMessage()
                    + " ].");
            if (LOG.isDebugEnabled()) {
                cpe.printStackTrace();
            }
        }
        catch (IOException ioe) {
            LOG.error(method 
                    + "Unexpected IOException raised wile executing the HTTP "
                    + "POST.  Exception message [ "
                    + ioe.getMessage()
                    + " ].");
            if (LOG.isDebugEnabled()) {
                ioe.printStackTrace();
            }
        }
        finally {
            if (client != null) {
                client.getConnectionManager().shutdown();
            }
        }
    }
    
    public void postToOne() {
        this.execute(TARGET_URLs[0]);
    }
    
    public void postToAll() {
        
    }
    
    public static void main(String[] args) {
        (new HttpPostTest()).postToOne();
    }
    
}
