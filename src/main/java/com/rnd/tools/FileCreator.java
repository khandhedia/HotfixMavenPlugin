package com.rnd.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;

/**
 * Created by nirk0816 on 6/9/2017.
 */
public class FileCreator
{
    public File createFile(String path)
    {
        System.out.println(path);

        File file = FileUtils.getFile(path);
        if (null != file) { System.out.println(file.getAbsolutePath()); }
        else { System.out.println("NULL"); }

        return file;
    }
}
