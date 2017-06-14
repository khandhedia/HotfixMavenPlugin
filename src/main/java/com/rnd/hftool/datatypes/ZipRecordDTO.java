package com.rnd.hftool.datatypes;

import java.io.File;

/**
 * Created by NirMit on 6/7/2017.
 */
public class ZipRecordDTO
{
    private File sourceFile;

    private String filePathWithinZip;

    public ZipRecordDTO(File sourceFile, String filePathWithinZip) {
        this.sourceFile = sourceFile;
        this.filePathWithinZip = filePathWithinZip;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getFilePathWithinZip() {
        return filePathWithinZip;
    }

    public void setFilePathWithinZip(String filePathWithinZip) {
        this.filePathWithinZip = filePathWithinZip;
    }

    @Override
    public String toString() {
        return "ZipRecordDTO{" +
                "sourceFile=" + sourceFile +
                ", filePathWithinZip='" + filePathWithinZip + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZipRecordDTO that = (ZipRecordDTO) o;

        return filePathWithinZip != null ? filePathWithinZip.equals(that.filePathWithinZip) : that.filePathWithinZip == null;
    }

    @Override
    public int hashCode() {
        return filePathWithinZip != null ? filePathWithinZip.hashCode() : 0;
    }
}
