package com.rnd.hftool.utilities;

import com.rnd.hftool.datatypes.JarRecordInfo;
import com.rnd.hftool.datatypes.ZipRecordInfo;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.jar.Attributes.Name.MANIFEST_VERSION;
import static org.apache.commons.lang3.StringUtils.replaceChars;
import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Nirav Khandhedia on 5/26/2017.
 */
public class JarUtilities
{
    private final boolean debugMode;

    private final Logger log;

    public JarUtilities(boolean debugMode)
    {
        this.debugMode = debugMode;
        log = getLogger(JarUtilities.class);
    }

    public File createJar(String jarPath, List<JarRecordInfo> jarRecordInfos) throws IOException
    {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(MANIFEST_VERSION, "1.0");

        File jarFile = new File(jarPath);
        FileOutputStream fileOutputStream = new FileOutputStream(jarFile);
        try (JarOutputStream jar = new JarOutputStream(fileOutputStream, manifest))
        {
            addMultipleFilesToJar(jarRecordInfos, jar);
        }

        return jarFile;
    }

    private void addMultipleFilesToJar(List<JarRecordInfo> jarRecordInfos, JarOutputStream jar)
    {
        jarRecordInfos.forEach(jarRecordInfo ->
                              {
                                  try
                                  {
                                      addSingleFileToJar(jar, jarRecordInfo);
                                  }
                                  catch (IOException e)
                                  {
                                      if (debugMode) { e.printStackTrace(); }
                                      log.error("Error adding " + jarRecordInfo + " in jar: " + e.getMessage());
                                  }
                              });
    }

    private void addSingleFileToJar(JarOutputStream jar, JarRecordInfo jarRecordInfo) throws IOException
    {

        File sourceFile = jarRecordInfo.getSourceFile();
        String filePathWithinJar = jarRecordInfo.getFilePathWithinJar().replace("\\","/");
        filePathWithinJar = replaceChars( filePathWithinJar, "\\", "/");

        BufferedInputStream in = null;
        try
        {
            if (sourceFile.isDirectory()) { return; }

            JarEntry entry = new JarEntry(filePathWithinJar);
            entry.setTime(sourceFile.lastModified());
            jar.putNextEntry(entry);
            FileInputStream sourceInputStream = new FileInputStream(sourceFile);
            in = new BufferedInputStream(sourceInputStream);

            byte[] buffer = new byte[1024];
            while (true)
            {
                int count = in.read(buffer);
                if (count == -1) { break; }
                jar.write(buffer, 0, count);
            }
            jar.closeEntry();
        }
        finally
        {
            if (in != null) { in.close(); }
        }
    }

    public void compressFilesToZip(Set<ZipRecordInfo> zipRecordInfos, String zipFilePath) throws IOException
    {
        byte[] buffer = new byte[1024];
        FileOutputStream fos = new FileOutputStream(zipFilePath);
        ZipOutputStream zos = new ZipOutputStream(fos);

        zipRecordInfos.forEach(zipRecordInfo -> {

            try
            {
                File srcFile = zipRecordInfo.getSourceFile();
                String name = zipRecordInfo.getFilePathWithinZip();

                FileInputStream fis = new FileInputStream(srcFile);

                // begin writing a new ZIP entry, positions the stream to the start of the entry data
                zos.putNextEntry(new ZipEntry(name));

                int length;

                while ((length = fis.read(buffer)) > 0) { zos.write(buffer, 0, length); }

                zos.closeEntry();

                // close the InputStream
                fis.close();
            }
            catch (IOException e)
            {
                if (debugMode) { e.printStackTrace(); }
                log.error("Error creating zip file : " + e.getMessage());
            }
        });

        zos.close();
    }
}
