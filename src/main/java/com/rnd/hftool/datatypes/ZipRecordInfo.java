package com.rnd.hftool.datatypes;

import java.io.File;

/**
 * Created by Nirav Khandhedia on 6/7/2017.
 */
public class ZipRecordInfo
{
    private final File sourceFile;

    private final String filePathWithinZip;

    public ZipRecordInfo(File sourceFile, String filePathWithinZip) {
        this.sourceFile = sourceFile;
        this.filePathWithinZip = filePathWithinZip;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getFilePathWithinZip() {
        return filePathWithinZip;
    }

    @Override
    public String toString() {
        return "ZipRecordInfo{" +
                "sourceFile=" + sourceFile +
                ", filePathWithinZip='" + filePathWithinZip + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ZipRecordInfo that = (ZipRecordInfo) o;

        return filePathWithinZip != null ? filePathWithinZip.equals(that.filePathWithinZip) : that.filePathWithinZip == null;
    }

    @Override
    public int hashCode() {
        return filePathWithinZip != null ? filePathWithinZip.hashCode() : 0;
    }
}
