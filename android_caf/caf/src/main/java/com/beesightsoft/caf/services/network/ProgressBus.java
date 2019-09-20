package com.beesightsoft.caf.services.network;

/**
 * Created by nhancao on 7/18/17.
 */

public class ProgressBus {
    private Class apiClass;
    private long bytesRead;
    private long contentLength;
    private boolean done;

    public ProgressBus(Class apiClass, long bytesRead, long contentLength, boolean done) {
        this.apiClass = apiClass;
        this.bytesRead = bytesRead;
        this.contentLength = contentLength;
        this.done = done;
    }

    public Class getApiClass() {
        return apiClass;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public long getContentLength() {
        return contentLength;
    }

    public boolean isDone() {
        return done;
    }

    @Override
    public String toString() {
        return "ProgressBus{" +
               "bytesRead=" + bytesRead +
               ", contentLength=" + contentLength +
               ", done=" + done +
               '}';
    }
}
