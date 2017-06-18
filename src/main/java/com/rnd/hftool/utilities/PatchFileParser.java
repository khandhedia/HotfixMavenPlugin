package com.rnd.hftool.utilities;

import com.rnd.hftool.properties.HotFixProperties;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.log4j.Logger.getLogger;

/**
 * Created by Nirav Khandhedia on 6/2/2017.
 */
public class PatchFileParser {

    private final String patchRecordPrefix;

    private final Logger log;

    public PatchFileParser() {
        log = getLogger(PatchFileParser.class);
        patchRecordPrefix = HotFixProperties.getInstance().getPatchRecordPrefix();
    }

    public LinkedList<String> parsePatchFile(File file) {

        if (!file.exists()) throw new RuntimeException("Unable to access Patch File: " + file);

        log.info("Processing Patch File: " + file);

        LinkedList<String> inputFileRecords = new LinkedList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (startsWith(line, patchRecordPrefix)) {
                    inputFileRecords.add(substringAfter(line, patchRecordPrefix));
                }
            }
        } catch (IOException e) {
            String message = "Exception occurred while parsing Patch File: " + file + " Exception: ";
            log.error(message + e.getMessage());
            throw new RuntimeException(message+ e);
        }

        if(isNotEmpty(inputFileRecords))
        {
            log.debug("Patch Records: START");
            inputFileRecords.forEach(log::debug);
            log.debug("Patch Records: END");
        }
        else
        {
            log.warn("No Records found in Patch file.");
        }

        return inputFileRecords;
    }
}
