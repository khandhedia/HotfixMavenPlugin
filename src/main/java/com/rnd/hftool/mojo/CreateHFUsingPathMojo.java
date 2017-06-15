package com.rnd.hftool.mojo;

import com.rnd.hftool.application.CreateHF;
import com.rnd.hftool.enums.ArtifactType;
import com.rnd.hftool.properties.HotFixProperties;
import com.rnd.hftool.utilities.SearchUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by NirMit on 5/31/2017.
 */

@Mojo(name = "createHFUsingPath", inheritByDefault = false, aggregator = true)
public class CreateHFUsingPathMojo extends AbstractMojo {

    public static final String WINDOWS_SEPARATOR = "\\";
    public static final String UNIX_SEPARATOR = "/";
    private String currentPath;

    private String classesPath;

    private String resourcesPath;

    private String otherPaths;

    private String modulePaths;

    private SimpleDateFormat simpleDateFormat;

    public void execute() throws MojoExecutionException, MojoFailureException {

        configureDateFormat();

        setParameters();

        printParameters();

        replacePathSeparators();

        setSystemProperties();

        createHF();
    }

    public static void main(String[] args) {
        CreateHFUsingPathMojo createHFUsingPathMojo = new CreateHFUsingPathMojo();
        try {
            createHFUsingPathMojo.execute();
        } catch (MojoExecutionException e) {
            e.printStackTrace();
        } catch (MojoFailureException e) {
            e.printStackTrace();
        }
    }

    private void setParameters() {

        Path path = Paths.get("E:\\IJWorkSpace\\HFTool\\HFTool1");
        currentPath = path.toAbsolutePath().toString();
        File file = path.toAbsolutePath().toFile();
        if(!file.exists())
            throw new RuntimeException("Path " + currentPath + " doesn't exist.");

        configureLogging(currentPath);

        HotFixProperties hotFixProperties = new HotFixProperties(currentPath);

        classesPath = hotFixProperties.getClassesPath();
        resourcesPath = hotFixProperties.getResourcesPath();
        otherPaths = hotFixProperties.getOtherPaths();
        modulePaths = analyzeModulePaths(path);

    }

    private void configureLogging(String componentPath) {
        if (isEmpty(getProperty("log.level"))) System.setProperty("log.level", "INFO");
        System.setProperty("logfile.name", componentPath + "/hfplugin" + simpleDateFormat.format(currentTimeMillis()) + ".log");
    }

    private void configureDateFormat() {
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private String analyzeModulePaths(Path searchPath) {

        SearchUtilities searchUtilities = new SearchUtilities(true);
        List<Path> pathList = searchUtilities.search(searchPath, "pom.xml", ArtifactType.REGULAR_FILE, 999);
        if(CollectionUtils.isEmpty(pathList))
            return EMPTY;
        StringBuilder sb = new StringBuilder();
        pathList.forEach(
                path -> {
                    String relativePath = replace(searchPath.relativize(path).toString(), WINDOWS_SEPARATOR, UNIX_SEPARATOR);
                    if(!relativePath.startsWith("pom.xml"))
                    sb.append(substringBeforeLast(relativePath, "/pom.xml")).append(",");
                }
        );
        return substringBeforeLast(sb.toString(), ",");
    }

    private void createHF() {
        CreateHF createHF = new CreateHF();
        createHF.createHF();
    }

    private void setSystemProperties() {
        setProperty("current.path", currentPath);
        setProperty("classes.path", classesPath);
        setProperty("resources.path", resourcesPath);
        setProperty("other.paths", otherPaths);
        setProperty("module.paths", modulePaths);
    }

    private void replacePathSeparators() {
        currentPath = replace(currentPath, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
        classesPath = replace(classesPath, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
        resourcesPath = replace(resourcesPath, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
        otherPaths = replace(otherPaths, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
        modulePaths = replace(modulePaths, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    private void printParameters() {

        Path path = Paths.get("");
        System.out.println(path.toAbsolutePath());

        System.out.println(currentPath);
        System.out.println(classesPath);
        System.out.println(resourcesPath);
        System.out.println(otherPaths);
        System.out.println(modulePaths);
    }
}
