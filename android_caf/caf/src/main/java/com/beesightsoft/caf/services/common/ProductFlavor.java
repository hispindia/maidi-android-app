package com.beesightsoft.caf.services.common;

/**
 * Created by nhancao on 4/16/17.
 */

public enum ProductFlavor {
    INTEGRATION("integration", "INVALID"),
    STAGING("staging", "INVALID"),
    PRODUCTION("production", "INVALID"),
    LIVE("live", "INVALID");

    private String id;
    private String url;

    ProductFlavor(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public static ProductFlavor getFlavor(String flavor) {
        for (ProductFlavor productFlavor : ProductFlavor.values()) {
            if (flavor.equals(productFlavor.getId())) {
                return productFlavor;
            }
        }
        return INTEGRATION;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public ProductFlavor setUrl(String url) {
        this.url = url;
        return this;
    }

}
