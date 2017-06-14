package com.rnd.hftool.application;

import com.rnd.hftool.datatypes.FileInfo;
import com.rnd.hftool.datatypes.JarRecordDTO;
import com.rnd.hftool.datatypes.ZipRecordDTO;
import com.rnd.hftool.enums.ArtifactType;
import com.rnd.hftool.utilities.JarUtilities;
import com.rnd.hftool.utilities.PatchFileParser;
import com.rnd.hftool.utilities.SearchUtilities;
import org.apache.commons.collections.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.System.currentTimeMillis;
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
    public static final String WINDOWS_SEPARATOR = "\\";
    public static final String UNDERSCORE = "_";
    public static final String EXTENSION_JAR = ".jar";
    public static final String EXTENSION_ZIP = ".zip";

    private String componentPath;

    private String modulePaths;

    private String classesPath;

    private String resourcePath;

    private String otherPaths;

    private File patchFile;

    private List<FileInfo> fileInfos = new ArrayList<>();

    private List<String> modulePathList;

    private List<String> basePaths = new ArrayList<>();

    private List<String> parsedPatchFileRecords;

    private SimpleDateFormat simpleDateFormat;

    private Map<String, List<JarRecordDTO>> moduleWiseJarRecordDTOSMap = new HashMap<>();

    Set<ZipRecordDTO> singleZipRecordDTOs = new HashSet<>();

    private Set<ZipRecordDTO> zipRecordDTOS = new HashSet<>();

    private String zipRecordPrefixPath = "applications/NetCracker.ear/APP-INF/classes";

    private JarUtilities jarUtilities = new JarUtilities(true);

    private String suffix;


    public CreateHF() {
        getEnvProperties();
        configureDateFormat();
        configureLogging();
    }

    private void configureDateFormat() {
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    public void createHF() {

        getPatchFile();

        prepareModulePathList();

        prepareBasePathList();

        parsePatchFile();

        treatPatchFileRecords();

        parsePatchFileRecords();

        prepareJarRecords();

        packJarAndZip();

        packSingleZip();

    }

    private void configureLogging() {
        if (isEmpty(getProperty("log.level"))) System.setProperty("log.level", "INFO");
        System.setProperty("logfile.name", componentPath + "/hfplugin" + simpleDateFormat.format(currentTimeMillis()) + ".log");
    }


    private void packSingleZip() {

        String zipPath = componentPath+UNIX_SEPARATOR+suffix+ EXTENSION_ZIP;

        try {
            jarUtilities.compressFilesToZip(singleZipRecordDTOs, zipPath);
            System.out.println("Single zip created : " + zipPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void packJarAndZip() {

        suffix = "HotFix_" + simpleDateFormat.format(currentTimeMillis());

        moduleWiseJarRecordDTOSMap.keySet().stream().forEach(
                moduleName -> {
                    List<JarRecordDTO> jarRecordDTOS = moduleWiseJarRecordDTOSMap.get(moduleName);
                    String jarPath = moduleName + UNIX_SEPARATOR + replace(moduleName, UNIX_SEPARATOR, UNDERSCORE) + UNDERSCORE + suffix + EXTENSION_JAR;
                    try {
                        File jar = jarUtilities.createJar(jarPath, jarRecordDTOS);
                        System.out.println("Jar file created: " + jar.getAbsolutePath());
                        zipRecordDTOS.add(new ZipRecordDTO(jar, jarPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
        );

        String aggregateZipPath = componentPath + UNIX_SEPARATOR + "Aggregate_"+suffix+".zip";
        try {
            jarUtilities.compressFilesToZip(zipRecordDTOS, aggregateZipPath);
            System.out.println("Aggregate Zip created : " + aggregateZipPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ZipRecordDTO convertJarRecordToZipRecord(JarRecordDTO jarRecordDTO) {
        return new ZipRecordDTO(jarRecordDTO.getSourceFile(), zipRecordPrefixPath + UNIX_SEPARATOR + jarRecordDTO.getFilePathWithinJar());
    }

    private void prepareJarRecords() {

        SearchUtilities searchUtilities = new SearchUtilities(true);
        fileInfos.forEach(
                fileInfo -> {
                    String filePath = fileInfo.getFilePath();
                    String componentPath = fileInfo.getComponentPath();
                    String modulePath = fileInfo.getModulePath();
                    List<JarRecordDTO> jarRecordDTOS = Optional.ofNullable(moduleWiseJarRecordDTOSMap.get(modulePath)).orElse(new ArrayList<>());
                    String prefixPath = replace(componentPath + (isNotBlank(modulePath) ? UNIX_SEPARATOR + modulePath : EMPTY) + (isNotBlank(fileInfo.getBasePath()) ? UNIX_SEPARATOR + fileInfo.getBasePath() : EMPTY) + UNIX_SEPARATOR, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                    System.out.println("filePath" + filePath + " PrefixPath " + prefixPath);
                    if (endsWithIgnoreCase(filePath, EXTENSION_CLASS)) {
                        filePath = replaceIgnoreCase(filePath, EXTENSION_CLASS, EMPTY);
                        File searchPathFile = new File(substringBeforeLast(filePath, UNIX_SEPARATOR));
                        if (!searchPathFile.exists()) {
                            System.out.println("Path not found " + searchPathFile);
                        } else {
                            List<Path> pathList = searchUtilities.search(searchPathFile.toPath(), substringAfterLast(filePath, UNIX_SEPARATOR), ArtifactType.CLASS_FILE, 1);
                            if (CollectionUtils.isEmpty(pathList)) {
                                System.out.println("File not found " + filePath + EXTENSION_CLASS);
                            } else {
                                if (pathList.size() > 1) {
                                    System.out.println("Multiple Files found for " + filePath + "$*.class");
                                }
                                pathList.forEach(
                                        path -> {
                                            System.out.println(path);
                                            JarRecordDTO jarRecordDTO = new JarRecordDTO();
                                            jarRecordDTO.setSourceFile(path.toFile());
                                            String foundFilePath = replace(path.toString(), WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                                            jarRecordDTO.setFilePathWithinJar(substringAfterLast(foundFilePath, prefixPath));
                                            jarRecordDTOS.add(jarRecordDTO);
                                            singleZipRecordDTOs.add(convertJarRecordToZipRecord(jarRecordDTO));
                                            System.out.println(jarRecordDTO);
                                        }
                                );
                            }
                        }
                    } else {
                        File file = new File(filePath);
                        if (!file.exists()) {
                            System.out.println("File not found " + file);
                        } else {
                            JarRecordDTO jarRecordDTO = new JarRecordDTO();
                            jarRecordDTO.setSourceFile(file);
                            String foundFilePath = replace(file.toString(), WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                            jarRecordDTO.setFilePathWithinJar(substringAfterLast(foundFilePath, prefixPath));
                            jarRecordDTOS.add(jarRecordDTO);
                            singleZipRecordDTOs.add(convertJarRecordToZipRecord(jarRecordDTO));
                            System.out.println(jarRecordDTO);
                        }


                    }

                    moduleWiseJarRecordDTOSMap.put(modulePath, jarRecordDTOS);

                }
        );

    }

    private void parsePatchFileRecords() {
        parsedPatchFileRecords.stream().forEach(
                recordPath ->
                {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setComponentPath(componentPath);
                    fileInfo.setFilePath(componentPath + UNIX_SEPARATOR + recordPath);

                    final String firstPathToken = substringBefore(recordPath, UNIX_SEPARATOR);
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

        File patchFile = Arrays.asList(currentPathFile.listFiles((dir, name) -> endsWithIgnoreCase(name, EXTENSION_PATCH))).stream().sorted((o1, o2) -> (int) (o2.lastModified() - o1.lastModified())).findFirst().orElse(null);

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

