package com.beesightsoft.caf.models;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by kietnh on 9/20/2016.
 */
public class CursorPaginatedCollection<T> {
    private Cursor cursor;
    private Collection<T> items;

    public CursorPaginatedCollection() {
        this(Collections.<T>emptyList());
    }

    public CursorPaginatedCollection(Collection<T> items) {
        this.items = items;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public Collection<T> getItems() {
        return items;
    }

    public void setItems(Collection<T> items) {
        this.items = items;
    }
}
