package com.beesightsoft.caf.models;

/**
 * Created by kietngo on 8/5/2016.
 */
public abstract class BaseEntity {
    private long id;
    private long createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
