package com.rnd.hftool.datatypes;

import java.io.File;

/**
 * Created by Nirav Khandhedia on 5/29/2017.
 */
public class JarRecordInfo
{
    private File sourceFile;

    private String filePathWithinJar;

    public File getSourceFile()
    {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile)
    {
        this.sourceFile = sourceFile;
    }

    public String getFilePathWithinJar()
    {
        return filePathWithinJar;
    }

    public void setFilePathWithinJar(String filePathWithinJar)
    {
        this.filePathWithinJar = filePathWithinJar;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("JarRecordInfo{");
        sb.append("sourceFile=").append(sourceFile);
        sb.append(", filePathWithinJar='").append(filePathWithinJar).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
