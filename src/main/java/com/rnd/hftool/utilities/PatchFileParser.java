package com.rnd.hftool.utilities;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;

/**
 * Created by nirk0816 on 6/2/2017.
 */
public class PatchFileParser {

    public static final String PATCH_RECORD_PREFIX = "Index:";

    public LinkedList<String> parsePatchFile(File file) {

        if (!file.exists()) throw new RuntimeException("Unable to access file: " + file);

        System.out.println("File successfully read from " + file);

        LinkedList<String> inputFileRecords = new LinkedList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (startsWith(line, PATCH_RECORD_PREFIX)) {
                    inputFileRecords.add(substringAfter(line, PATCH_RECORD_PREFIX));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception occurred while parsing file " + file + " MESSAGE:" + e.getMessage());
        }

        inputFileRecords.forEach(System.out::println);
        return inputFileRecords;
    }
}
