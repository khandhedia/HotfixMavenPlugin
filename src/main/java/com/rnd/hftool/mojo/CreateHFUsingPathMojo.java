package com.rnd.hftool.mojo;

import com.rnd.hftool.application.CreateHF;
import com.rnd.hftool.enums.ArtifactType;
import com.rnd.hftool.properties.HotFixProperties;
import com.rnd.hftool.utilities.SearchUtilities;
import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static com.rnd.hftool.constants.HotFixConstants.*;
import static com.rnd.hftool.utilities.HotfixUtilities.setUnixPathSeparator;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * Created by Nirav Khandhedia on 5/31/2017.
 */

@Mojo(name = "createHFUsingPath", aggregator = true, defaultPhase = LifecyclePhase.INSTALL)
public class CreateHFUsingPathMojo extends AbstractMojo
{

    private Path currentPath;

    private String classesPath;

    private String resourcesPath;

    private String otherPaths;

    private String modulePaths;

    private SimpleDateFormat simpleDateFormat;

    private HotFixProperties hotFixProperties;

    public void execute() throws MojoExecutionException, MojoFailureException
    {

        configureDateFormat();

        identifyCurrentPathAndSetAsSytemProperty();

        configureLogging();

        loadHotFixProperties();

        setParameters();

        setSystemProperties();

        createHF();
    }

    private void configureDateFormat()
    {
        simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private void identifyCurrentPathAndSetAsSytemProperty()
    {
        Path path = Paths.get(EMPTY);
        currentPath = path.toAbsolutePath();
        if (!currentPath.toFile().exists()) { throw new RuntimeException("Path " + currentPath + " doesn't exist."); }
        setProperty(PROPERTY_CURRENT_PATH, setUnixPathSeparator(currentPath.toString()));
    }

    private void configureLogging()
    {
        if (isEmpty(getProperty(LOG_LEVEL))) { setProperty(LOG_LEVEL, INFO); }
        if (isEmpty(getProperty(LOGFILE_NAME)))
        {
            setProperty(LOGFILE_NAME, currentPath + UNIX_SEPARATOR + LOG_FILE_PREFIX + simpleDateFormat.format(currentTimeMillis()) + EXTENSION_LOG);
        }
    }

    private void loadHotFixProperties()
    {
        hotFixProperties = HotFixProperties.getInstance();
    }

    private void setParameters()
    {
        classesPath = hotFixProperties.getClassesPath();
        resourcesPath = hotFixProperties.getResourcesPath();
        otherPaths = hotFixProperties.getOtherPaths();
        modulePaths = analyzeModulePaths();
    }

    private String analyzeModulePaths()
    {
        SearchUtilities searchUtilities = new SearchUtilities(hotFixProperties.isDebugMode());
        List<Path> pomLocations = searchUtilities.search(currentPath, POM_XML, ArtifactType.REGULAR_FILE, 999);
        if (CollectionUtils.isEmpty(pomLocations)) { return EMPTY; }
        StringBuilder sb = new StringBuilder();
        pomLocations.forEach(path ->
                             {
                                 String relativePath = setUnixPathSeparator(currentPath.relativize(path).toString());
                                 if (!relativePath.startsWith(POM_XML))
                                 {
                                     sb.append(substringBeforeLast(relativePath, UNIX_SEPARATOR + POM_XML)).append(COMMA);
                                 }
                             });
        return substringBeforeLast(sb.toString(), COMMA);
    }

    private void setSystemProperties()
    {
        setProperty(PROPERTY_CLASSES_PATH, setUnixPathSeparator(classesPath));
        setProperty(PROPERTY_RESOURCES_PATH, setUnixPathSeparator(resourcesPath));
        setProperty(PROPERTY_OTHER_PATHS, setUnixPathSeparator(otherPaths));
        setProperty(PROPERTY_MODULE_PATHS, setUnixPathSeparator(modulePaths));
    }

    private void createHF()
    {
        new CreateHF().createHF();
    }

    public static void main(String[] args)
    {
        CreateHFUsingPathMojo createHFUsingPathMojo = new CreateHFUsingPathMojo();
        try
        {
            createHFUsingPathMojo.execute();
        }
        catch (MojoExecutionException | MojoFailureException e)
        {
            e.printStackTrace();
        }
    }

}
