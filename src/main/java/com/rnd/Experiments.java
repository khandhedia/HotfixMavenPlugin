package com.rnd;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by NirMit on 6/8/2017.
 */
public class Experiments {

    List<String> pathList = new ArrayList<>();

    String one = "HFTool1/target/classes/com/rnd/hftool/application/TextInputFileParser.java";
    String two = "HFTool1/src/main/resources/log4j.properties";
    String three = "HFTool1Copy/target/classes/com/rnd/hftool/enums/FileTokens.java";

    List<FileInfo> fileInfos = new ArrayList<>();

    String componentPath = "E:/IJWorkSpace/HFTool/";

    List<String> modulePaths = Arrays.asList("HFTool1", "HFTool1Copy");

    List<String> basePaths = Arrays.asList("E:/IJWorkSpace/HFTool/HFTool1/target/classes", "E:/IJWorkSpace/HFTool/HFTool1/src/main/resources", "E:/IJWorkSpace/HFTool/HFTool1Copy/src/main/resources", "E:/IJWorkSpace/HFTool/HFTool1Copy/target/classes");

    List<String> basePathsTruncated = new ArrayList<>();

    public static void main(String[] args) {
        Experiments experiments = new Experiments();
        experiments.method1();
    }

    private void method1() {

        pathList.add(one);
        pathList.add(two);
        pathList.add(three);

        truncate(basePaths);

        pathList.stream().forEach(
                path ->
                {
                    final String firstPathToken = StringUtils.substringBefore(path, "/");
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setComponentPath(componentPath);
                    String moduleName = modulePaths.stream().filter(modulePath -> StringUtils.equalsIgnoreCase(modulePath, firstPathToken)).findFirst().orElse(null);
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

    private void truncate(List<String> basePaths) {
        basePaths.stream().forEach(basePath -> basePathsTruncated.add(StringUtils.substringAfter(basePath, componentPath)));
    }


}

