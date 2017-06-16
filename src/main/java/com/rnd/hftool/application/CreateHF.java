package com.rnd.hftool.application;

import com.rnd.hftool.datatypes.FileInfo;
import com.rnd.hftool.datatypes.JarRecordDTO;
import com.rnd.hftool.datatypes.ZipRecordDTO;
import com.rnd.hftool.enums.ArtifactType;
import com.rnd.hftool.properties.HotFixProperties;
import com.rnd.hftool.utilities.JarUtilities;
import com.rnd.hftool.utilities.PatchFileParser;
import com.rnd.hftool.utilities.SearchUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.rnd.hftool.constants.HotFixConstants.COMMA_SEPARATOR;
import static com.rnd.hftool.constants.HotFixConstants.DEBUG_MODE_TRUE;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_CLASS;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_JAR;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_JAVA;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_LOG;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_PATCH;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_ZIP;
import static com.rnd.hftool.constants.HotFixConstants.HOTFIX_DIRECTORY_NAME;
import static com.rnd.hftool.constants.HotFixConstants.INFO;
import static com.rnd.hftool.constants.HotFixConstants.LOGFILE_NAME;
import static com.rnd.hftool.constants.HotFixConstants.LOG_FILE_PREFIX;
import static com.rnd.hftool.constants.HotFixConstants.LOG_LEVEL;
import static com.rnd.hftool.constants.HotFixConstants.SOURCE_JAVA_PACKAGE;
import static com.rnd.hftool.constants.HotFixConstants.TARGET_CLASSES_PACKAGE;
import static com.rnd.hftool.constants.HotFixConstants.UNDERSCORE;
import static com.rnd.hftool.constants.HotFixConstants.UNIX_SEPARATOR;
import static com.rnd.hftool.constants.HotFixConstants.WINDOWS_SEPARATOR;
import static com.rnd.hftool.utilities.HotfixUtilities.setUnixPathSeparator;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Nirav Khandhedia on 6/8/2017.
 */
public class CreateHF
{

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

    private String zipRecordPrefixPath;

    private JarUtilities jarUtilities;

    private String suffix;

    private Logger log;

    public CreateHF()
    {
        configureDateFormat();
        readSystemProperties();
        configureLogging();
        logParameters();
    }

    private void configureDateFormat()
    {
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private void readSystemProperties()
    {
        componentPath = getProperty("current.path");
        modulePaths = getProperty("module.paths");
        classesPath = getProperty("classes.path");
        resourcePath = getProperty("resources.path");
        otherPaths = getProperty("other.paths");
    }

    private void configureLogging()
    {
        if (isEmpty(getProperty(LOG_LEVEL))) { setProperty(LOG_LEVEL, INFO); }
        if (isEmpty(getProperty(LOGFILE_NAME)))
        {
            setProperty(LOGFILE_NAME, componentPath + UNIX_SEPARATOR + LOG_FILE_PREFIX + simpleDateFormat.format(currentTimeMillis()) + EXTENSION_LOG);
        }

        log = getLogger(CreateHF.class);
    }

    private void logParameters()
    {
        log.info("Component Path:" + componentPath);
        log.info("Module Paths:" + modulePaths);
        log.info("Classes Path:" + classesPath);
        log.info("Resources Path:" + resourcePath);
        log.info("Other Paths:" + otherPaths);
    }

    public void createHF()
    {
        init();

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

    private void init()
    {
        jarUtilities = new JarUtilities(DEBUG_MODE_TRUE);
    }

    private void getPatchFile()
    {
        File currentPathFile = new File(componentPath);
        if (!currentPathFile.exists() || currentPathFile.isFile()) { throw new RuntimeException(componentPath + " refers to a FILE or doesn't exist."); }

        File patchFile = asList(currentPathFile.listFiles((dir, name) -> endsWithIgnoreCase(name, EXTENSION_PATCH))).stream().sorted((o1, o2) -> (int) (o2
                .lastModified() - o1.lastModified())).findFirst().orElse(null);

        if (null == patchFile) { throw new RuntimeException("No patch patchFile found at " + componentPath); }

        log.info("Processing patch patchFile: " + patchFile.getAbsolutePath());
        this.patchFile = patchFile;
    }

    private void prepareModulePathList()
    {
        modulePathList = asList(modulePaths.split(COMMA_SEPARATOR));
    }

    private void prepareBasePathList()
    {
        basePaths.add(classesPath);
        basePaths.add(resourcePath);
        basePaths.addAll(asList(otherPaths.split(COMMA_SEPARATOR)));
    }

    private void parsePatchFile()
    {
        PatchFileParser patchFileParser = new PatchFileParser();
        parsedPatchFileRecords = patchFileParser.parsePatchFile(patchFile);
    }

    private void treatPatchFileRecords()
    {
        List<String> treatedPatchFileRecords = new LinkedList<>();
        parsedPatchFileRecords
                .forEach(record -> treatedPatchFileRecords.add(setUnixPathSeparator(replace(record, SOURCE_JAVA_PACKAGE, TARGET_CLASSES_PACKAGE)).trim()));
        parsedPatchFileRecords = treatedPatchFileRecords;
    }

    private void parsePatchFileRecords()
    {
        log.debug("Parsed File Records: START ");
        parsedPatchFileRecords.forEach(recordPath ->
                                       {
                                           FileInfo fileInfo = new FileInfo();

                                           fileInfo.setComponentPath(componentPath);
                                           fileInfo.setFilePath(componentPath + UNIX_SEPARATOR + recordPath);

                                           String moduleName = inferModuleNameFromRecordPath(recordPath);

                                           if (null != moduleName)
                                           {
                                               fileInfo.setModulePath(moduleName);
                                               recordPath = substringAfter(recordPath, UNIX_SEPARATOR);

                                           }

                                           String finalPath = recordPath;
                                           String basePath = inferBasePathFromRecordPath(finalPath);
                                           if (null != basePath)
                                           {
                                               fileInfo.setBasePath(basePath);
                                           }

                                           fileInfos.add(fileInfo);

                                           log.debug(fileInfo);
                                       });
        log.debug("Parsed File Records: END ");
    }

    private String inferBasePathFromRecordPath(String finalPath)
    {
        return basePaths.stream().filter(basePath -> startsWithIgnoreCase(finalPath, basePath)).findFirst().orElse(null);
    }

    private String inferModuleNameFromRecordPath(String recordPath)
    {
        final String firstPathToken = substringBefore(recordPath, UNIX_SEPARATOR);
        return modulePathList.stream().filter(modulePath -> equalsIgnoreCase(modulePath, firstPathToken)).findFirst().orElse(null);
    }

    private void packSingleZip()
    {

        String zipPath = componentPath + UNIX_SEPARATOR + suffix + EXTENSION_ZIP;

        try
        {
            jarUtilities.compressFilesToZip(singleZipRecordDTOs, zipPath);
            System.out.println("Single zip created : " + zipPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void packJarAndZip()
    {

        File hotfixDirectory = new File(componentPath + HOTFIX_DIRECTORY_NAME);
        if (!hotfixDirectory.exists())
        {
            System.out.println();
            hotfixDirectory.mkdir();
        }

        suffix = "HotFix_" + simpleDateFormat.format(currentTimeMillis());

        moduleWiseJarRecordDTOSMap.keySet().forEach(moduleName ->
                                                    {
                                                        List<JarRecordDTO> jarRecordDTOS = moduleWiseJarRecordDTOSMap.get(moduleName);
                                                        String jarPath = componentPath + HOTFIX_DIRECTORY_NAME + UNIX_SEPARATOR + moduleName + UNIX_SEPARATOR + replace(
                                                                moduleName, UNIX_SEPARATOR, UNDERSCORE) + UNDERSCORE + suffix + EXTENSION_JAR;
                                                        try
                                                        {
                                                            File jar = jarUtilities.createJar(jarPath, jarRecordDTOS);
                                                            System.out.println("Jar file created: " + jar.getAbsolutePath());
                                                            zipRecordDTOS.add(new ZipRecordDTO(jar, jarPath));
                                                        }
                                                        catch (IOException e)
                                                        {
                                                            e.printStackTrace();
                                                        }


                                                    });

        String aggregateZipPath = componentPath + HOTFIX_DIRECTORY_NAME + UNIX_SEPARATOR + "Aggregate_" + suffix + ".zip";
        try
        {
            jarUtilities.compressFilesToZip(zipRecordDTOS, aggregateZipPath);
            System.out.println("Aggregate Zip created : " + aggregateZipPath);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private ZipRecordDTO convertJarRecordToZipRecord(JarRecordDTO jarRecordDTO)
    {
        zipRecordPrefixPath = HotFixProperties.getInstance().getSingleZipPrefixPath();
        return new ZipRecordDTO(jarRecordDTO.getSourceFile(), zipRecordPrefixPath + UNIX_SEPARATOR + jarRecordDTO.getFilePathWithinJar());
    }

    private void prepareJarRecords()
    {

        SearchUtilities searchUtilities = new SearchUtilities(DEBUG_MODE_TRUE);
        fileInfos.forEach(fileInfo ->
                          {
                              String filePath = fileInfo.getFilePath();
                              String componentPath = fileInfo.getComponentPath();
                              String modulePath = fileInfo.getModulePath();
                              List<JarRecordDTO> jarRecordDTOS = Optional.ofNullable(moduleWiseJarRecordDTOSMap.get(modulePath)).orElse(new ArrayList<>());
                              String prefixPath = replace(componentPath + (isNotBlank(modulePath)
                                                                           ? UNIX_SEPARATOR + modulePath
                                                                           : EMPTY) + (isNotBlank(fileInfo.getBasePath())
                                                                                       ? UNIX_SEPARATOR + fileInfo.getBasePath()
                                                                                       : EMPTY) + UNIX_SEPARATOR, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                              System.out.println("filePath" + filePath + " PrefixPath " + prefixPath);
                              if (endsWithIgnoreCase(filePath, EXTENSION_CLASS))
                              {
                                  filePath = replaceIgnoreCase(filePath, EXTENSION_CLASS, EMPTY);
                                  File searchPathFile = new File(substringBeforeLast(filePath, UNIX_SEPARATOR));
                                  if (!searchPathFile.exists())
                                  {
                                      System.out.println("Path not found " + searchPathFile);
                                  }
                                  else
                                  {
                                      List<Path> pathList = searchUtilities
                                              .search(searchPathFile.toPath(), substringAfterLast(filePath, UNIX_SEPARATOR), ArtifactType.CLASS_FILE, 1);
                                      if (CollectionUtils.isEmpty(pathList))
                                      {
                                          System.out.println("File not found " + filePath + EXTENSION_CLASS);
                                      }
                                      else
                                      {
                                          if (pathList.size() > 1)
                                          {
                                              System.out.println("Multiple Files found for " + filePath + "$*.class");
                                          }
                                          pathList.forEach(path ->
                                                           {
                                                               System.out.println(path);
                                                               JarRecordDTO jarRecordDTO = new JarRecordDTO();
                                                               jarRecordDTO.setSourceFile(path.toFile());
                                                               String foundFilePath = replace(path.toString(), WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                                                               jarRecordDTO.setFilePathWithinJar(substringAfterLast(foundFilePath, prefixPath));
                                                               jarRecordDTOS.add(jarRecordDTO);
                                                               singleZipRecordDTOs.add(convertJarRecordToZipRecord(jarRecordDTO));
                                                               System.out.println(jarRecordDTO);
                                                           });
                                      }
                                  }
                              }
                              else
                              {
                                  File file = new File(filePath);
                                  if (!file.exists())
                                  {
                                      System.out.println("File not found " + file);
                                  }
                                  else
                                  {
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

                          });

    }


}

