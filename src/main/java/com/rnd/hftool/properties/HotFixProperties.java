package com.rnd.hftool.properties;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.lang.System.getProperty;

/**
 * Created by Nirav Khandhedia on 6/15/2017.
 */
public class HotFixProperties
{

    private static HotFixProperties hotFixPropertiesObject;

    public static HotFixProperties getInstance()
    {
        if(null == hotFixPropertiesObject)
        {
            hotFixPropertiesObject = new HotFixProperties();
        }

        return hotFixPropertiesObject;
    }

    private HotFixProperties()
    {
        loadProperties(getProperty("current.path").toString());
    }

    private static Properties hotFixProperties = new Properties();

    private final String CLASSES_PATH = "classesPath";
    private final String RESOURCES_PATH = "resourcesPath";
    private final String OTHER_PATHS = "otherPaths";
    private final String SINGLE_ZIP_PREFIX_PATH = "singleZipPrefixPath";

    public String getClassesPath()
    {
        return hotFixProperties.getProperty(CLASSES_PATH);
    }

    public String getResourcesPath()
    {
        return hotFixProperties.getProperty(RESOURCES_PATH);
    }

    public String getOtherPaths()
    {
        return hotFixProperties.getProperty(OTHER_PATHS);
    }

    public String getSingleZipPrefixPath()
    {
        return hotFixProperties.getProperty(SINGLE_ZIP_PREFIX_PATH);
    }

    private static final String PROPERTIES_FILE_NAME = "hotfix.properties";

    private static void loadProperties(String propertiesPath)
    {
        String customPropertiesFilePath = propertiesPath + "/" + PROPERTIES_FILE_NAME;
        File file = Paths.get(customPropertiesFilePath).toFile();
        if (file.exists())
        {
            try
            {
                InputStream is = new FileInputStream(StringUtils.replace(file.getAbsolutePath(), "\\", "/"));
                hotFixProperties.load(is);
                System.out.println("Properties loaded from file : " + file);
                return;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("Failed loading properties from file : " + file);
            }
        }

        System.out.println("Custom properties " + customPropertiesFilePath + " not found. Processing with default values.");

        try
        {
            hotFixProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
