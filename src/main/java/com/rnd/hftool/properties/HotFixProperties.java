package com.rnd.hftool.properties;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static com.sun.xml.internal.bind.WhiteSpaceProcessor.replace;

/**
 * Created by NirMit on 6/15/2017.
 */
public class HotFixProperties {

    public HotFixProperties(String propertiesPath) {
        loadProperties(propertiesPath);
    }

    private Properties hotFixProperties = new Properties();

    private final String CLASSES_PATH = "classesPath";
    private final String RESOURCES_PATH = "resourcesPath";
    private final String OTHER_PATHS= "otherPaths";

    public String getClassesPath() {
        return hotFixProperties.getProperty(CLASSES_PATH);
    }

    public String getResourcesPath() {
        return hotFixProperties.getProperty(RESOURCES_PATH);
    }

    public String getOtherPaths() {
        return hotFixProperties.getProperty(OTHER_PATHS);
    }

    private static final String PROPERTIES_FILE_NAME ="hotfix.properties";

    private void loadProperties(String propertiesPath)
    {
        File file = Paths.get(propertiesPath+"/"+ PROPERTIES_FILE_NAME).toFile();
        if(file.exists())
        {
            try {
                InputStream is = new FileInputStream(StringUtils.replace(file.getAbsolutePath(), "\\","/"));
                hotFixProperties.load(is);
                System.out.println("Properties loaded from file : " + file);
                return;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Failed loading properties from file : " + file);
            }
        }

        try {
            hotFixProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
