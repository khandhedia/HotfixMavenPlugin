package com.rnd.hftool.properties;

import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

import static com.rnd.hftool.constants.HotFixConstants.PROPERTIES_FILE_NAME;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_CLASSES_PATH;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_CURRENT_PATH;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_DEBUG_MODE;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_OTHER_PATHS;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_PATCH_RECORD_PREFIX;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_RESOURCES_PATH;
import static com.rnd.hftool.constants.HotFixConstants.PROPERTY_SINGLE_ZIP_PREFIX_PATH;
import static com.rnd.hftool.constants.HotFixConstants.UNIX_SEPARATOR;
import static com.rnd.hftool.utilities.HotfixUtilities.setUnixPathSeparator;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.nio.file.Paths.get;

/**
 * Created by Nirav Khandhedia on 6/15/2017.
 */
public class HotFixProperties {

    private static HotFixProperties hotFixPropertiesObject;

    public static HotFixProperties getInstance() {
        if (null == hotFixPropertiesObject) {
            hotFixPropertiesObject = new HotFixProperties();
        }

        return hotFixPropertiesObject;
    }

    private final Properties defaultHotFixProperties = new Properties();
    private final Properties customHotFixProperties = new Properties();

    private HotFixProperties() {
        loadProperties(getProperty(PROPERTY_CURRENT_PATH));
    }

    public String getClassesPath() {
        return getPreferredProperty(PROPERTY_CLASSES_PATH);
    }

    public String getResourcesPath() {
        return getPreferredProperty(PROPERTY_RESOURCES_PATH);
    }

    public String getOtherPaths() {
        return getPreferredProperty(PROPERTY_OTHER_PATHS);
    }

    public String getSingleZipPrefixPath() {
        return getPreferredProperty(PROPERTY_SINGLE_ZIP_PREFIX_PATH);
    }

    public String getPatchRecordPrefix() {
        return getPreferredProperty(PROPERTY_PATCH_RECORD_PREFIX);
    }

    public boolean isDebugMode() {
        return BooleanUtils.toBoolean(getPreferredProperty(PROPERTY_DEBUG_MODE));
    }

    private void loadProperties(String propertiesPath) {
        String customPropertiesFilePath = propertiesPath + UNIX_SEPARATOR + PROPERTIES_FILE_NAME;
        File file = get(customPropertiesFilePath).toFile();
        if (file.exists()) {
            System.out.println("Processing Custom Properties.");
            try {
                InputStream is = new FileInputStream(setUnixPathSeparator(file.getAbsolutePath()));
                customHotFixProperties.load(is);
                System.out.println("Custom Properties loaded from file : " + file);
            } catch (IOException e) {
                System.out.println("ERROR: Custom Properties loading failed, from file: " + file + ". Exception: " + e);
            }
        }

        try {
            defaultHotFixProperties.load(currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME));
        } catch (IOException e) {
            String message = "Default Properties loading failed. ";
            System.out.println("ERROR: " + message + e.getMessage());
            throw new RuntimeException(message + e);
        }
    }

    private String getPreferredProperty(String property) {
        String value = Optional.ofNullable(customHotFixProperties.getProperty(property)).orElse(defaultHotFixProperties.getProperty(property));
        if (null == value) {
            String message = "Property " + property + " could not be read from default or custom properties.";
            System.out.println("ERROR: " + message);
            throw new RuntimeException(message);
        }
        return value;
    }
}
