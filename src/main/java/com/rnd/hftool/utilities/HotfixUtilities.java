package com.rnd.hftool.utilities;

import static com.rnd.hftool.constants.HotFixConstants.UNIX_SEPARATOR;
import static com.rnd.hftool.constants.HotFixConstants.WINDOWS_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.replace;

/**
 * Created by Nirav Khandhedia on 6/16/2017.
 */
public class HotfixUtilities
{
    public static String setUnixPathSeparator(String inputString)
    {
        return replace(inputString, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }
}
