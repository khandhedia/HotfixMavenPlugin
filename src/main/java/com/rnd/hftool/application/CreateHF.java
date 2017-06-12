package com.rnd.hftool.application;

import com.rnd.hftool.datatypes.FileInfo;
import com.rnd.hftool.utilities.PatchFileParser;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by NirMit on 6/8/2017.
 */
public class CreateHF {

    private String componentPath;

    private String modulePaths;

    private String classesPath;

    private String resourcePath;

    private String otherPaths;

    private File patchFile;

    List<String> pathList = new ArrayList<>();

    List<FileInfo> fileInfos = new ArrayList<>();

    List<String> modulePathList;

    List<String> basePaths = new ArrayList<>();

    List<String> basePathsTruncated = new ArrayList<>();

    List<String> parsedPatchFileRecords;

    public static void main(String[] args) {
        CreateHF createHF = new CreateHF();
        createHF.createHF();
    }

    public void createHF() {

        getPatchFile();

        getEnvProperties();

        prepareModulePathList();

        prepareBasePathList();

        parsePatchFile();

        treatPatchFileRecords();

        pathList.stream().forEach(
                path ->
                {
                    final String firstPathToken = StringUtils.substringBefore(path, "/");
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setComponentPath(componentPath);
                    String moduleName = modulePathList.stream().filter(modulePath -> StringUtils.equalsIgnoreCase(modulePath, firstPathToken)).findFirst().orElse(null);
                    if (null != moduleName) {
                        fileInfo.setModulePath(moduleName);
                        //path = StringUtils.substringAfter(path, "/");
                    }

                    String finalPath = path;
                    String basePath1 = basePathsTruncated.stream().filter(basePath -> StringUtils.startsWithIgnoreCase(finalPath, basePath)).findFirst().orElse(null);
                    if (null != basePath1) {
                        fileInfo.setBasePath(basePath1);
                        path = StringUtils.substringAfter(path, basePath1);
                    }

                    fileInfo.setFilePath(path);

                    fileInfos.add(fileInfo);

                    System.out.println(fileInfo);


                }

        );

    }

    private void treatPatchFileRecords() {

        parsedPatchFileRecords.stream().forEach(
                record ->
                {
                    StringUtils.replace(record, "src/main/java", "target/classes");
                }
        );
    }

    private void parsePatchFile() {
        PatchFileParser patchFileParser = new PatchFileParser();
        parsedPatchFileRecords = patchFileParser.parsePatchFile();
    }

    private void prepareBasePathList() {
        basePaths.add(classesPath);
        basePaths.add(resourcePath);
        basePaths.addAll(Arrays.asList(otherPaths.split(",")));
    }

    private void prepareModulePathList() {
        modulePathList = Arrays.asList(modulePaths.split(","));
    }

    private void getPatchFile() {
        File currentPathFile = new File(componentPath);
        if(!currentPathFile.exists() || currentPathFile.isFile())
            throw new RuntimeException(componentPath + " refers to a FILE or doesn't exist.");

        File patchFile = Arrays.asList(currentPathFile.listFiles((dir, name) -> StringUtils.endsWithIgnoreCase(name, ".patch"))).stream().sorted((o1, o2) -> (int)(o1.lastModified() - o2.lastModified())).findFirst().orElse(null);

        if(null == patchFile)
            throw new RuntimeException("No patch patchFile found at " + componentPath);

        System.out.println("Processing patch patchFile : " + patchFile.getAbsolutePath());
        this.patchFile = patchFile;
    }


    private void getEnvProperties()
    {
        componentPath = System.getProperty("current.path");
        modulePaths = System.getProperty("module.paths");
        classesPath = System.getProperty("classes.path");
        resourcePath = System.getProperty("resources.path");
        otherPaths = System.getProperty("other.paths");

        System.out.println("After reading system properties");

        System.out.println(componentPath);
        System.out.println(modulePaths);
        System.out.println(classesPath);
        System.out.println(resourcePath);
        System.out.println(otherPaths);
    }

}

