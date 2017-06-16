package com.rnd.hftool.mojo;

import com.rnd.hftool.application.CreateHF;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static com.rnd.hftool.utilities.HotfixUtilities.setUnixPathSeparator;
import static java.lang.System.setProperty;

/**
 * Created by Nirav Khandhedia on 5/31/2017.
 */

@Mojo(name = "createHFUsingPOMConfiguration", aggregator = true, defaultPhase = LifecyclePhase.INSTALL)
public class CreateHFUsingPOMConfigurationMojo extends AbstractMojo
{

    @Parameter(property = "currentPath") private String currentPath;

    @Parameter(property = "classesPath") private String classesPath;

    @Parameter(property = "resourcesPath") private String resourcesPath;

    @Parameter(property = "otherPaths") private String otherPaths;

    @Parameter(property = "modulePaths") private String modulePaths;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        setSystemProperties();
        new CreateHF().createHF();
    }

    private void setSystemProperties()
    {
        setProperty("current.path", setUnixPathSeparator(currentPath));
        setProperty("classes.path", setUnixPathSeparator(classesPath));
        setProperty("resources.path", setUnixPathSeparator(resourcesPath));
        setProperty("other.paths", setUnixPathSeparator(otherPaths));
        setProperty("module.paths", setUnixPathSeparator(modulePaths));
    }

}
