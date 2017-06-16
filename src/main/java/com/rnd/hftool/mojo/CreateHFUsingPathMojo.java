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

import static com.rnd.hftool.constants.HotFixConstants.COMMA;
import static com.rnd.hftool.constants.HotFixConstants.DEBUG_MODE_TRUE;
import static com.rnd.hftool.constants.HotFixConstants.EXTENSION_LOG;
import static com.rnd.hftool.constants.HotFixConstants.LOG_FILE_PREFIX;
import static com.rnd.hftool.constants.HotFixConstants.POM_XML;
import static com.rnd.hftool.constants.HotFixConstants.UNIX_SEPARATOR;
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

    HotFixProperties hotFixProperties;

    public void execute() throws MojoExecutionException, MojoFailureException
    {

        configureDateFormat();

        identifyCurrentPath();

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

    private void identifyCurrentPath()
    {
        Path path = Paths.get(EMPTY);
        currentPath = path.toAbsolutePath();
        if (!currentPath.toFile().exists()) { throw new RuntimeException("Path " + currentPath + " doesn't exist."); }
    }

    private void configureLogging()
    {
        if (isEmpty(getProperty("log.level"))) { System.setProperty("log.level", "INFO"); }
        System.setProperty("logfile.name", currentPath + UNIX_SEPARATOR + LOG_FILE_PREFIX + simpleDateFormat.format(currentTimeMillis()) + EXTENSION_LOG);
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
        SearchUtilities searchUtilities = new SearchUtilities(DEBUG_MODE_TRUE);
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
        setProperty("current.path", setUnixPathSeparator(currentPath.toString()));
        setProperty("classes.path", setUnixPathSeparator(classesPath));
        setProperty("resources.path", setUnixPathSeparator(resourcesPath));
        setProperty("other.paths", setUnixPathSeparator(otherPaths));
        setProperty("module.paths", setUnixPathSeparator(modulePaths));
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
        catch (MojoExecutionException e)
        {
            e.printStackTrace();
        }
        catch (MojoFailureException e)
        {
            e.printStackTrace();
        }
    }

}
