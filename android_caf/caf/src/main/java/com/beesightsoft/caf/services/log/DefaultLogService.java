package com.beesightsoft.caf.services.log;

import android.content.Context;

import com.logentries.logger.AndroidLogger;

import java.io.File;
import java.io.IOException;

/**
 * Created by kietnh on 5/27/2016.
 */
public class DefaultLogService implements LogService {
    protected AndroidLogger logger;

    public DefaultLogService() {
    }

    @Override
    public void init(Context context, boolean useHttpPost, boolean useSsl, boolean isUsingDataHub, String dataHubAddr,
                     int dataHubPort, String token, boolean logHostName) throws IOException {

        File logFile = new File(context.getFilesDir(), "LogentriesLogStorage.log");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.logger = AndroidLogger
                .createInstance(context, useHttpPost, useSsl, isUsingDataHub, dataHubAddr, dataHubPort, token,
                                logHostName);
    }

    @Override
    public void init(Context context, String token) throws IOException {
        init(context, true, false, false, null, 0, token, false);
    }

    @Override
    public void log(String message) {
        if (this.logger != null) {
            this.logger.log(message);
        }
    }
}
