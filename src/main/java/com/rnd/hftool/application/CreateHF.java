package com.rnd.hftool.application;

import com.rnd.hftool.datatypes.FileInfo;
import com.rnd.hftool.datatypes.JarRecordInfo;
import com.rnd.hftool.datatypes.ZipRecordInfo;
import com.rnd.hftool.enums.ArtifactType;
import com.rnd.hftool.properties.HotFixProperties;
import com.rnd.hftool.utilities.JarUtilities;
import com.rnd.hftool.utilities.PatchFileParser;
import com.rnd.hftool.utilities.SearchUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rnd.hftool.constants.HotFixConstants.*;
import static com.rnd.hftool.utilities.HotfixUtilities.setUnixPathSeparator;
import static java.lang.System.*;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Nirav Khandhedia on 6/8/2017.
 */
public class CreateHF {


    private String componentPath;
    private String modulePaths;
    private String classesPath;
    private String resourcePath;
    private String otherPaths;
    private File patchFile;
    private List<FileInfo> fileInfoList;
    private List<String> modulePathList;
    private List<String> basePaths;
    private List<String> parsedPatchFileRecords;
    private SimpleDateFormat simpleDateFormat;
    private Map<String, List<JarRecordInfo>> moduleWiseJarRecordDTOSMap;
    private Set<ZipRecordInfo> singleZipRecordInfos;
    private Set<ZipRecordInfo> zipRecordInfos;
    private JarUtilities jarUtilities;
    private String suffix;
    private Logger log;
    private HotFixProperties hotFixProperties;
    private String localHotfixDirectoryName;

    public CreateHF() {
        configureDateFormat();
        readSystemProperties();
        configureLogging();
        logParameters();
        init();

    }

    private void configureDateFormat() {
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private void readSystemProperties() {
        componentPath = getProperty(PROPERTY_CURRENT_PATH);
        modulePaths = getProperty(PROPERTY_MODULE_PATHS);
        classesPath = getProperty(PROPERTY_CLASSES_PATH);
        resourcePath = getProperty(PROPERTY_RESOURCES_PATH);
        otherPaths = getProperty(PROPERTY_OTHER_PATHS);
    }

    private void configureLogging() {
        if (StringUtils.isEmpty(getProperty(LOG_LEVEL))) {
            setProperty(LOG_LEVEL, INFO);
        }
        if (StringUtils.isEmpty(getProperty(LOGFILE_NAME))) {
            setProperty(LOGFILE_NAME, componentPath + UNIX_SEPARATOR + LOG_FILE_PREFIX + simpleDateFormat.format(currentTimeMillis()) + EXTENSION_LOG);
        }

        log = getLogger(CreateHF.class);
    }

    private void logParameters() {
        log.info("Component Path:" + componentPath);
        log.info("Module Paths:" + modulePaths);
        log.info("Classes Path:" + classesPath);
        log.info("Resources Path:" + resourcePath);
        log.info("Other Paths:" + otherPaths);
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

    private void init() {
        hotFixProperties = HotFixProperties.getInstance();
        jarUtilities = new JarUtilities(hotFixProperties.isDebugMode());
        fileInfoList = new ArrayList<>();
        basePaths = new ArrayList<>();
        moduleWiseJarRecordDTOSMap = new HashMap<>();
        singleZipRecordInfos = new HashSet<>();
        zipRecordInfos = new HashSet<>();
    }

    private void getPatchFile() {
        File currentPathFile = new File(componentPath);
        if (!currentPathFile.exists() || currentPathFile.isFile()) {
            throw new RuntimeException(componentPath + " refers to a FILE or doesn't exist.");
        }

        File patchFile = null;
        File[] files = currentPathFile.listFiles((dir, name) -> endsWithIgnoreCase(name, EXTENSION_PATCH));
        if (isNotEmpty(files)) {
            patchFile = Arrays.stream(files).sorted((o1, o2) -> (int) (o2
                    .lastModified() - o1.lastModified())).findFirst().orElse(null);
        }

        if (null == patchFile) {
            throw new RuntimeException("No patch patchFile found at " + componentPath);
        }

        log.info("Considering Last Modified Patch File: " + patchFile.getAbsolutePath());
        this.patchFile = patchFile;
    }

    private void prepareModulePathList() {
        modulePathList = asList(modulePaths.split(COMMA_SEPARATOR));
    }

    private void prepareBasePathList() {
        basePaths.add(classesPath);
        basePaths.add(resourcePath);
        basePaths.addAll(asList(otherPaths.split(COMMA_SEPARATOR)));
    }

    private void parsePatchFile() {
        PatchFileParser patchFileParser = new PatchFileParser();
        parsedPatchFileRecords = patchFileParser.parsePatchFile(patchFile);
    }

    private void treatPatchFileRecords() {
        List<String> treatedPatchFileRecords = new LinkedList<>();
        parsedPatchFileRecords
                .forEach(record -> treatedPatchFileRecords.add(setUnixPathSeparator(replace(replace(record, SOURCE_JAVA_PACKAGE, TARGET_CLASSES_PACKAGE), EXTENSION_JAVA, EXTENSION_CLASS)).trim()));
        parsedPatchFileRecords = treatedPatchFileRecords;
    }

    private void parsePatchFileRecords() {
        log.debug("Parsed File Records: START ");
        parsedPatchFileRecords.forEach(recordPath ->
        {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setComponentPath(componentPath);
            fileInfo.setFilePath(componentPath + UNIX_SEPARATOR + recordPath);

            String moduleName = inferModuleNameFromRecordPath(recordPath);

            if (null != moduleName) {
                fileInfo.setModulePath(moduleName);
                recordPath = substringAfter(recordPath, UNIX_SEPARATOR);

            }

            String finalPath = recordPath;
            String basePath = inferBasePathFromRecordPath(finalPath);
            if (null != basePath) {
                fileInfo.setBasePath(basePath);
            }

            fileInfoList.add(fileInfo);

            log.debug(fileInfo);
        });
        log.debug("Parsed File Records: END ");
    }

    private String inferBasePathFromRecordPath(String finalPath) {
        return basePaths.stream().filter(basePath -> startsWithIgnoreCase(finalPath, basePath)).findFirst().orElse(null);
    }

    private String inferModuleNameFromRecordPath(String recordPath) {
        final String firstPathToken = substringBefore(recordPath, UNIX_SEPARATOR);
        return modulePathList.stream().filter(modulePath -> equalsIgnoreCase(modulePath, firstPathToken)).findFirst().orElse(null);
    }

    private void prepareJarRecords() {

        SearchUtilities searchUtilities = new SearchUtilities(hotFixProperties.isDebugMode());
        fileInfoList.forEach(fileInfo ->
        {
            String filePath = fileInfo.getFilePath();
            String componentPath = fileInfo.getComponentPath();
            String modulePath = fileInfo.getModulePath();
            List<JarRecordInfo> jarRecordInfos = Optional.ofNullable(moduleWiseJarRecordDTOSMap.get(modulePath)).orElse(new ArrayList<>());
            String prefixPath = setUnixPathSeparator(componentPath + (isNotBlank(modulePath)
                    ? UNIX_SEPARATOR + modulePath
                    : EMPTY) + (isNotBlank(fileInfo.getBasePath())
                    ? UNIX_SEPARATOR + fileInfo.getBasePath()
                    : EMPTY) + UNIX_SEPARATOR);
            if (endsWithIgnoreCase(filePath, EXTENSION_CLASS)) {
                filePath = replaceIgnoreCase(filePath, EXTENSION_CLASS, EMPTY);
                File searchPathFile = new File(substringBeforeLast(filePath, UNIX_SEPARATOR));
                if (!searchPathFile.exists()) {
                    log.error("Path not found " + searchPathFile);
                } else {
                    List<Path> pathList = searchUtilities
                            .search(searchPathFile.toPath(), substringAfterLast(filePath, UNIX_SEPARATOR), ArtifactType.CLASS_FILE, 1);
                    if (isEmpty(pathList)) {
                        log.warn("File not found " + filePath + EXTENSION_CLASS);
                    } else {
                        if (pathList.size() > 1) {
                            log.warn("Multiple Files found for " + filePath + "$*.class");
                        }
                        pathList.forEach(path ->
                        {
                            log.warn(path);
                            createJarAndSingleZipRecordDTO(jarRecordInfos, prefixPath, path.toFile());
                        });
                    }
                }
            } else {
                File file = new File(filePath);
                if (!file.exists()) {
                    log.error("File not found " + file);
                } else {
                    createJarAndSingleZipRecordDTO(jarRecordInfos, prefixPath, file);
                }
            }
            moduleWiseJarRecordDTOSMap.put(modulePath, jarRecordInfos);
        });
    }

    private void createJarAndSingleZipRecordDTO(List<JarRecordInfo> jarRecordInfos, String prefixPath, File file) {
        String foundFilePath = setUnixPathSeparator(file.toString());
        JarRecordInfo jarRecordInfo = new JarRecordInfo();
        jarRecordInfo.setSourceFile(file);
        jarRecordInfo.setFilePathWithinJar(substringAfterLast(foundFilePath, prefixPath));
        jarRecordInfos.add(jarRecordInfo);
        singleZipRecordInfos.add(convertJarRecordToZipRecord(jarRecordInfo));
        log.debug("Jar Record: " + jarRecordInfo);
    }

    private ZipRecordInfo convertJarRecordToZipRecord(JarRecordInfo jarRecordInfo) {
        String zipRecordPrefixPath = hotFixProperties.getSingleZipPrefixPath();
        return new ZipRecordInfo(jarRecordInfo.getSourceFile(), zipRecordPrefixPath + UNIX_SEPARATOR + jarRecordInfo.getFilePathWithinJar());
    }

    private void packJarAndZip() {

        localHotfixDirectoryName = HOTFIX_DIRECTORY_NAME;
        File hotfixDirectory = new File(componentPath + localHotfixDirectoryName);
        if (!hotfixDirectory.exists()) {
            log.info("Directory " + hotfixDirectory.getAbsolutePath() + " doesn't exist. Creating Directory.");
            if (!hotfixDirectory.mkdir()) {
                log.error("Directory " + hotfixDirectory.getAbsolutePath() + " could not be created.");
                localHotfixDirectoryName = EMPTY;
            } else {
                log.info("Directory " + hotfixDirectory.getAbsolutePath() + " successfully created.");
            }
        }

        suffix = "HotFix_" + simpleDateFormat.format(currentTimeMillis());

        log.info("JARs Creation : START");
        String finalLocalHotfixDirectoryName = localHotfixDirectoryName;
        moduleWiseJarRecordDTOSMap.keySet().forEach(moduleName ->
        {
            List<JarRecordInfo> jarRecordInfos = moduleWiseJarRecordDTOSMap.get(moduleName);
            if (isNotEmpty(jarRecordInfos)) {
                String jarPath = componentPath + finalLocalHotfixDirectoryName + UNIX_SEPARATOR + (isNotBlank(moduleName) ? replace(
                        moduleName, UNIX_SEPARATOR, UNDERSCORE) + UNDERSCORE : EMPTY) + suffix + EXTENSION_JAR;
                try {
                    File jar = jarUtilities.createJar(jarPath, jarRecordInfos);
                    log.info(jar.getAbsolutePath());
                    zipRecordInfos.add(new ZipRecordInfo(jar.getAbsoluteFile(), jar.getAbsoluteFile().getName()));
                } catch (IOException e) {
                    log.error("Jar Creation Failed " + e);
                }
            }
        });

        log.info("JARs Creation : END");

        if (isNotEmpty(zipRecordInfos)) {
            String aggregateZipPath = componentPath + localHotfixDirectoryName + UNIX_SEPARATOR + AGGREGATE + suffix + EXTENSION_ZIP;
            try {
                jarUtilities.compressFilesToZip(zipRecordInfos, aggregateZipPath);
                log.info("Aggregate Zip Created : " + aggregateZipPath);
            } catch (IOException e) {
                log.error("Aggregate Zip Creation Failed " + e);
            }
        }
    }

    private void packSingleZip() {
        if(isNotEmpty(singleZipRecordInfos)) {
            String zipPath = componentPath + localHotfixDirectoryName + UNIX_SEPARATOR + suffix + EXTENSION_ZIP;
            try {
                jarUtilities.compressFilesToZip(singleZipRecordInfos, zipPath);
                log.info("Single zip created: " + zipPath);
            } catch (IOException e) {
                log.error("Single Zip Creation Failed: " + e);
            }
        }
    }
}

