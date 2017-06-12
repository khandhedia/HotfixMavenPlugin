package com.rnd.hftool.mojo;

import com.rnd.hftool.application.CreateHF;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Created by NirMit on 5/31/2017.
 */

@Mojo(name = "createHF")
public class CreateHFMojo extends AbstractMojo {

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

    private static File file = new File("temp/a.txt");

    public void execute() throws MojoExecutionException, MojoFailureException {

        printParameters();

        replacePathSeparators();

        setSystemProperties();

        CreateHF createHF = new CreateHF();
        createHF.createHF();

        /*
        PatchFileProcessor patchFileProcessor = new PatchFileProcessor();
        patchFileProcessor.processPatchFile("D:\\\\RO-Global\\\\Tickets\\\\NFVO-11521-8_1\\\\NFVO_11521_RO_v1.patch");
*/


    }

    private void setSystemProperties() {
        System.setProperty("current.path", currentPath);
        System.setProperty("classes.path", classesPath);
        System.setProperty("resources.path", resourcesPath);
        System.setProperty("other.paths", otherPaths);
        System.setProperty("module.paths", modulePaths);
    }

    private void replacePathSeparators() {
        currentPath = StringUtils.replace(currentPath, "\\", "/");
        classesPath = StringUtils.replace(classesPath, "\\", "/");
        resourcesPath = StringUtils.replace(resourcesPath, "\\", "/");
        otherPaths = StringUtils.replace(otherPaths, "\\", "/");
        modulePaths = StringUtils.replace(modulePaths, "\\", "/");
    }

    private void printParameters() {
        System.out.println(currentPath);
        System.out.println(classesPath);
        System.out.println(resourcesPath);
        System.out.println(otherPaths);
        System.out.println(modulePaths);
    }
}
