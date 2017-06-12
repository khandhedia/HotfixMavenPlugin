package com.rnd.hftool;

import com.rnd.hftool.utilities.PatchFileParser;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by nirk0816 on 6/2/2017.
 */
public class Main
{
    public static void main(String[] args)
    {



        PatchFileParser patchFileParser = new PatchFileParser();
        patchFileParser.parsePatchFile();

        Path path = Paths.get("D:\\Workspaces\\RO-7_2");

        File file = new File("D:/Workspaces/RO-7_2/nfvi-resource-orchestration_9.5.3.1_R7.2");
        System.out.println(file.getAbsolutePath());


    }
}
