package com.rnd.tools;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Created by NirMit on 5/31/2017.
 */

@Mojo(name = "createHF")
public class CreateHFMojo extends AbstractMojo {

    @Parameter(property = "currentPath", defaultValue = "${current.path}")
    String path;

    public void execute() throws MojoExecutionException, MojoFailureException {

        System.out.println("Hi from CreateHFMojo");

        System.out.println("Path : " + path);

    }
}
