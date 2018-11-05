# PDFMerge
Simple web service used to merge a list of PDF files together. The exposed endpoints accept POST requests with a JSON message body in the following format:
```JSON
{
    "file_name" : "output_file.pdf",
    "files" : [
        "/local/path/to/file1.pdf",
        "/local/path/to/file2.pdf"
        ...
        "/local/path/to/fileN.pdf"
    ]
}
```
The PDFMerge utility is also capable of merging data that exists on an S3 filesystem.  In order to leverage S3 the input list of files should specify target files using URI syntax.  Example:
```JSON
{
    "file_name" : "output_file.pdf",
    "files" : [
        "s3:///local/path/to/file1.pdf",
        "file:///local/path/to/file2.pdf"
        ...
        "s3:///local/path/to/fileN.pdf"
    ]
}
```
## REST Endpoints
Two separate end-points are provided:
* **/PDFMerge/rest/merge** endpoint: The code will them merge together the identified files and return a JSON message with a URL link to the output file.  The return message would look like the following:
```JSON
{ "url" : "https://localhost/path/to/output_file.pdf" }
```
* **/PDFMerge/rest/mergeAndDownload** endpoint:  The code will them merge together the identified files and return the output file as an attachment.  

## Download the Source
* Minimum requirements:
    * Java Development Kit (v1.8.0 or higher)
    * GIT (v1.7 or higher)
    * Maven (v3.3 or higher)
* Download source
```
# cd /var/local
# git clone https://github.com/carpenlc/PDFMerge.git
```

## Customizations
Two properties files are located in the following directory: 
```
~/PDFMerge/src/main/resources
```
* **logback.xml:** Contains the log settings including the log-level settings and the location of the output log file.
* **pdf_merge.properties:** Contains application specific settings including AWS S3 settings.

## Build the Application
Execute the following Maven command to build the output WAR file.
```
# mvn clean package
```
## Build the Application with all Reporting Enabled
```
# mvn clean package checkstyle:checkstyle pmd:pmd findbugs:findbugs
```
Deployable WAR file will reside at:
```
~/PDFMerge/target/PDFMerge.war
```
## Notes
* The actual merging of input PDF files is handled by the open source [PDFBox](https://pdfbox.apache.org/) library.  Testing against large production PDFs revealed that PDFBox requires a large stack size.  Whatever container the PDFMerge.war is deployed to should have a stack size of 1g or larger (hint: -Xss1g).
