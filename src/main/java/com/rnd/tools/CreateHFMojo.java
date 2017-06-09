package com.rnd.tools;

import org.apache.commons.io.FileUtils;
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

    @Parameter(property = "currentPath") private FileDetails currentPath;

    @Parameter(property = "classesPath") private FileDetails classesPath;

    @Parameter(property = "resourcesPath") private FileDetails resourcesPath;

    @Parameter(property = "otherPaths") private FileDetails otherPaths;

    private static File file = new File("temp/a.txt");

    public void execute() throws MojoExecutionException, MojoFailureException {

        System.out.println("Hi from CreateHFMojo");

        System.out.println(currentPath);

        System.out.println(classesPath);

        System.out.println(resourcesPath);

        System.out.println(otherPaths);

        File path = currentPath.getPath();
        System.out.println("Current Path - NULL? " + (path == null));


        System.out.println(path);
        System.out.println(path.exists());

        /*System.out.println("Current Path - path " + currentPath.getPath().getPath());*/

/*        String currentPath1 = StringUtils.replace(currentPath.getPath().toString(), "\\", "/");
        String classesPath1 = StringUtils.replace(classesPath.getPath(), "\\", "/");
        String resourcesPath1 = StringUtils.replace(resourcesPath.getPath(), "\\", "/");
        String otherPaths1 = StringUtils.replace(otherPaths.getPath(), "\\", "/");

        System.out.println(currentPath1);

        System.out.println(classesPath1);

        System.out.println(resourcesPath1);

        System.out.println(otherPaths1);*/
/*
        FileCreator fileCreator = new FileCreator();
        fileCreator.createFile(currentPath1);*/

        System.out.println("888888888888888888888888888888888");



        System.out.println("88888888888888888888888888888888811111111");

/*
        PatchFileProcessor patchFileProcessor = new PatchFileProcessor();
        patchFileProcessor.processPatchFile("D:\\\\RO-Global\\\\Tickets\\\\NFVO-11521-8_1\\\\NFVO_11521_RO_v1.patch");
*/






    }
}
