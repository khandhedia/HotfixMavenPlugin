package com.rnd.hftool.utilities;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by nirk0816 on 6/2/2017.
 */
public class PatchFileParser
{
    public LinkedList<String> parsePatchFile()
    {

        File file  = new File("D:\\RO-Global\\Tickets\\NFVO-11521-8_1\\NFVO_11521_RO_v1.patch");
        if (file.exists()) { System.out.println("File successfully read from " + file); }

        LinkedList<String> inputFileRecords = new LinkedList<>();
        try
        {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine())
            {
                String trim = scanner.nextLine().trim();
                if(StringUtils.startsWith(trim, "Index:"))
                {
                    if(StringUtils.endsWithIgnoreCase(trim, ".java") || StringUtils.endsWithIgnoreCase(trim, ".class") )
                    {
                        inputFileRecords.add(StringUtils.substringBeforeLast(StringUtils.substringAfter(trim, "Index: "), "."));
                    }
                    else
                    {
                        inputFileRecords.add(StringUtils.substringAfter(trim, "Index: "));
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        inputFileRecords.stream().forEach(System.out::println);
        return inputFileRecords;
    }
}
