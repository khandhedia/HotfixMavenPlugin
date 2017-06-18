package com.rnd.hftool.utilities;


import com.rnd.hftool.properties.HotFixProperties;
import org.apache.log4j.Logger;

/**
 * Created by NirMit on 6/17/2017.
 */
public class LoggingUtility {

    private LoggingUtility loggingUtility;
    private boolean isDebugMode;
    public LoggingUtility getInstance()
    {
        if(null == loggingUtility)
        {
            loggingUtility = new LoggingUtility();
        }

        return loggingUtility;
    }

    private LoggingUtility(){
        isDebugMode = HotFixProperties.getInstance().isDebugMode();
    }

    public void logger(Logger log, String level, String message)
    {
        if(isDebugMode)
            log.debug(message);
        else
            log.info(message);
    }
}
