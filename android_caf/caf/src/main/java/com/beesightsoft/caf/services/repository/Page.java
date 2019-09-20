package com.beesightsoft.caf.services.repository;

/**
 * Created by kietnh on 9/19/2016.
 */
public class Page {
    private final int offset;
    private final int limit;

    private Page(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public static Page withOffsetAndLimit(int offset, int limit) {
        return new Page(offset, limit);
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }
}
