package com.rnd.hftool.application;

import com.rnd.hftool.datatypes.FileInfo;
import com.rnd.hftool.utilities.PatchFileParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by NirMit on 6/8/2017.
 */
public class CreateHF {

    public static final String SOURCE_JAVA_PACKAGE = "src/main/java";
    public static final String TARGET_CLASSES_PACKAGE = "target/classes";
    public static final String EXTENSION_JAVA = ".java";
    public static final String EXTENSION_CLASS = ".class";
    public static final String COMMA_SEPARATOR = ",";
    public static final String EXTENSION_PATCH = ".patch";
    public static final String UNIX_SEPARATOR = "/";

    private String componentPath;

    private String modulePaths;

    private String classesPath;

    private String resourcePath;

    private String otherPaths;

    private File patchFile;

    List<FileInfo> fileInfos = new ArrayList<>();

    List<String> modulePathList;

    List<String> basePaths = new ArrayList<>();

    List<String> parsedPatchFileRecords;

    public static void main(String[] args) {
        CreateHF createHF = new CreateHF();
        createHF.createHF();
    }

    public void createHF() {

        getEnvProperties();

        getPatchFile();

        prepareModulePathList();

        prepareBasePathList();

        parsePatchFile();

        treatPatchFileRecords();

        parsedPatchFileRecords.stream().forEach(
                recordPath ->
                {
                    final String firstPathToken = substringBefore(recordPath, UNIX_SEPARATOR);
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setComponentPath(componentPath);
                    fileInfo.setFilePath(componentPath + UNIX_SEPARATOR + recordPath);
                    String moduleName = modulePathList.stream().filter(modulePath -> equalsIgnoreCase(modulePath, firstPathToken)).findFirst().orElse(null);
                    if (null != moduleName) {
                        fileInfo.setModulePath(moduleName);
                        recordPath = substringAfter(recordPath, UNIX_SEPARATOR);
                    }

                    String finalPath = recordPath;
                    String basePath1 = basePaths.stream().filter(basePath -> startsWithIgnoreCase(finalPath, basePath)).findFirst().orElse(null);
                    if (null != basePath1) {
                        fileInfo.setBasePath(basePath1);
                    }

                    fileInfos.add(fileInfo);

                    System.out.println(fileInfo);
                }

        );

    }

    private void treatPatchFileRecords() {
        List<String> treatedPatchFileRecords = new LinkedList<>();
        parsedPatchFileRecords.forEach(
                record ->
                        treatedPatchFileRecords.add(replace(replace(record, SOURCE_JAVA_PACKAGE, TARGET_CLASSES_PACKAGE), EXTENSION_JAVA, EXTENSION_CLASS).trim())
        );
        parsedPatchFileRecords = treatedPatchFileRecords;
    }

    private void parsePatchFile() {
        PatchFileParser patchFileParser = new PatchFileParser();
        parsedPatchFileRecords = patchFileParser.parsePatchFile(patchFile);
    }

    private void prepareBasePathList() {
        basePaths.add(classesPath);
        basePaths.add(resourcePath);
        basePaths.addAll(Arrays.asList(otherPaths.split(COMMA_SEPARATOR)));
    }

    private void prepareModulePathList() {
        modulePathList = Arrays.asList(modulePaths.split(COMMA_SEPARATOR));
    }

    private void getPatchFile() {
        File currentPathFile = new File(componentPath);
        if (!currentPathFile.exists() || currentPathFile.isFile())
            throw new RuntimeException(componentPath + " refers to a FILE or doesn't exist.");

        File patchFile = Arrays.asList(currentPathFile.listFiles((dir, name) -> endsWithIgnoreCase(name, EXTENSION_PATCH))).stream().sorted((o1, o2) -> (int) (o1.lastModified() - o2.lastModified())).findFirst().orElse(null);

        if (null == patchFile)
            throw new RuntimeException("No patch patchFile found at " + componentPath);

        System.out.println("Processing patch patchFile : " + patchFile.getAbsolutePath());
        this.patchFile = patchFile;
    }


    private void getEnvProperties() {
        componentPath = getProperty("current.path");
        modulePaths = getProperty("module.paths");
        classesPath = getProperty("classes.path");
        resourcePath = getProperty("resources.path");
        otherPaths = getProperty("other.paths");

        System.out.println("After reading system properties");

        System.out.println(componentPath);
        System.out.println(modulePaths);
        System.out.println(classesPath);
        System.out.println(resourcePath);
        System.out.println(otherPaths);
    }

}

