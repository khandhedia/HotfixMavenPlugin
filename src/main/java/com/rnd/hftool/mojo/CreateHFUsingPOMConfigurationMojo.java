package com.rnd.hftool.mojo;

import com.rnd.hftool.application.CreateHF;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.setProperty;
import static org.apache.commons.lang3.StringUtils.replace;

/**
 * Created by NirMit on 5/31/2017.
 */

@Mojo(name = "createHFUsingPOMConfiguration", inheritByDefault = false, aggregator = true)
public class CreateHFUsingPOMConfigurationMojo extends AbstractMojo {

    public static final String WINDOWS_SEPARATOR = "\\";
    public static final String UNIX_SEPARATOR = "/";
    @Parameter(property = "currentPath")
    private String currentPath;

    @Parameter(property = "classesPath")
    private String classesPath;

    @Parameter(property = "resourcesPath")
    private String resourcesPath;

    @Parameter(property = "otherPaths")
    private String otherPaths;

    @Parameter(property = "modulePaths")
    private String modulePaths;

    public void execute() throws MojoExecutionException, MojoFailureException {

        printParameters();

        replacePathSeparators();

        setSystemProperties();

        createHF();
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
