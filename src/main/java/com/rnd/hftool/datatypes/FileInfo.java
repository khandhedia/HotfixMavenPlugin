package com.rnd.hftool.datatypes;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by Nirav Khandhedia on 6/8/2017.
 */
public class FileInfo {

    String componentPath = EMPTY;

    String modulePath = EMPTY;

    String basePath = EMPTY;

    String filePath = EMPTY;

    File file;

    public String getComponentPath() {
        return componentPath;
    }

    public void setComponentPath(String componentPath) {
        this.componentPath = componentPath;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(String modulePath) {
        this.modulePath = modulePath;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("FileInfo{");
        sb.append("componentPath='").append(componentPath).append('\'');
        sb.append(", modulePath='").append(modulePath).append('\'');
        sb.append(", basePath='").append(basePath).append('\'');
        sb.append(", filePath='").append(filePath).append('\'');
        sb.append(", file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
