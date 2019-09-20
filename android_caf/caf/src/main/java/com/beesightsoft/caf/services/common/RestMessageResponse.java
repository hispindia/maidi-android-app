package com.beesightsoft.caf.services.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kietngo on 6/3/2016.
 */
public class RestMessageResponse<T> {

    private T data;

    private List<RestErrorResponse> errors = new ArrayList<>();

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<RestErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<RestErrorResponse> errors) {
        this.errors = errors;
    }
}
