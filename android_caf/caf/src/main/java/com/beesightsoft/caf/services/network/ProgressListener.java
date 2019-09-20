package com.beesightsoft.caf.services.network;

/**
 * Created by nhancao on 7/18/17.
 */

public interface ProgressListener {
    void update(long bytesRead, long contentLength, boolean done);
}

