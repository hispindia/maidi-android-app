package com.beesightsoft.caf.views;

import android.content.Context;

import com.beesightsoft.caf.exceptions.ApiThrowable;

/**
 * Created by kietnh on 8/17/2016.
 */
public interface ApplicationView {
    Context getContext();

    void showError(ApiThrowable exception);
}
