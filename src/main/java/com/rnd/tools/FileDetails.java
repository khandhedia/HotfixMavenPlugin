package com.rnd.tools;

import java.io.File;

/**
 * Created by nirk0816 on 6/9/2017.
 */
public class FileDetails
{
    File path;

    public File getPath()
    {
        return path;
    }

    public void setPath(File path)
    {
        this.path = path;
    }


    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("FileDetails{");
        sb.append("path=").append(path);
        sb.append('}');
        return sb.toString();
    }
}
