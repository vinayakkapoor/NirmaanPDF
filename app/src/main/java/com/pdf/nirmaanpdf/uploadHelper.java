package com.pdf.nirmaanpdf;

public class uploadHelper {

    public String fileName;
    public String fileUrl;

    public uploadHelper() {
    }

    public uploadHelper(String fileName, String fileUrl) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
