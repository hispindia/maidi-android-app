package com.beesightsoft.caf.models;

import com.beesightsoft.caf.exceptions.ErrorCodes;

/**
 * Created by kietngo on 4/13/2016.
 */
public class ServiceResultError {
    private int errorCode;
    private String errorMessage;
    private Exception exception;

    public ServiceResultError(int errorCode, String errorMessage, Exception exception) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public ServiceResultError(int errorCode, String errorMessage) {
        this(errorCode, errorMessage, new Exception(errorMessage));
    }

    public ServiceResultError(String errorMessage) {
        this(ErrorCodes.GENERAL_ERROR, errorMessage);
    }

    public ServiceResultError(int errorCode) {
        this(errorCode, String.format("ERROR MESSAGE OF CODE %s", errorCode));
    }

    public ServiceResultError(Exception exception) {
        this(ErrorCodes.GENERAL_ERROR, exception.getMessage(), exception);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
