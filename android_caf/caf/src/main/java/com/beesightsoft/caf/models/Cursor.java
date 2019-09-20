package com.beesightsoft.caf.models;

/**
 * Created by kietnh on 9/19/2016.
 */
public class Cursor {
    private final long sinceId;
    private final int count;
    private final long maxId;

    public Cursor(long sinceId, long maxId, int count) {
        this.sinceId = sinceId;
        this.maxId = maxId;
        this.count = count;
    }

    public static Cursor withSinceAndCount(long sinceId, int count) {
        return new Cursor(sinceId, -1, count);
    }

    public static Cursor withMaxAndCount(long maxId, int count) {
        return new Cursor(-1, maxId, count);
    }

    public long getSinceId() {
        return sinceId;
    }

    public long getMaxId() {
        return maxId;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Cursor{" +
               "sinceId=" + sinceId +
               ", count=" + count +
               ", maxId=" + maxId +
               '}';
    }
}
