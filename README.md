# PDFMerge
Simple web service used to merge PDF files together.  

Two seperate end-points are provided:
```
/PDFMerge/rest/merge
/PDFMerge/rest/mergeAndDownload
```
## Download and Build the Source
* Minimum requirements:
    * Java Development Kit (v1.8.0 or higher)
    * GIT (v1.7 or higher)
    * Maven (v3.3 or higher)
* Download source
```
# cd /var/local
# git clone https://github.com/carpenlc/PDFMerge.git
```
* Build the output WAR file
```
# mvn clean package
```
## Notes
* The actual merging of input PDF files is handled by the open source [PDFBox](https://pdfbox.apache.org/) library.  Testing against large production PDFs revealed that PDFBox requires a large stack size.  Whatever container the PDFMerge.war is deployed to should have a stack size of 1g or larger (hint: -Xss1g).
