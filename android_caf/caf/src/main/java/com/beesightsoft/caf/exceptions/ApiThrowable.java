package com.beesightsoft.caf.exceptions;

import com.beesightsoft.caf.models.ServiceResultError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kietnh on 6/16/2016.
 */
public class ApiThrowable extends Throwable {
    private List<ServiceResultError> errors = new ArrayList<>();

    public static ApiThrowable from(List<ServiceResultError> errors) {
        ApiThrowable exception = new ApiThrowable();
        exception.errors = errors;

        return exception;
    }

    public static ApiThrowable from(ServiceResultError error) {
        return from(new ArrayList<>(Collections.singletonList(error)));
    }

    public static ApiThrowable from(int errorCode, String errorMessage, Exception ex) {
        return from(new ServiceResultError(errorCode, errorMessage, ex));
    }

    public static ApiThrowable from(int errorCode, String errorMessage) {
        return from(errorCode, errorMessage, new Exception(errorMessage));
    }

    public static ApiThrowable from(String errorMessage) {
        return from(ErrorCodes.GENERAL_ERROR, errorMessage, new Exception(errorMessage));
    }

    public static ApiThrowable from(int errorCode) {
        return from(errorCode, String.format("ERROR MESSAGE OF CODE %s", errorCode));
    }

    public static ApiThrowable from(Exception exception) {
        return from(ErrorCodes.GENERAL_ERROR,
                    exception.getMessage() == null ? exception.toString() : exception.getMessage(), exception);
    }

    public List<ServiceResultError> getErrors() {
        return errors;
    }

    public boolean hasMultipleErrors() {
        return this.errors.size() > 1;
    }

    public ServiceResultError firstError() {
        ServiceResultError error = this.errors.get(0);
        return error;
    }

    public int firstErrorCode() {
        ServiceResultError error = firstError();
        return error.getErrorCode();
    }

    public String firstErrorMessage() {
        ServiceResultError error = firstError();
        return error.getErrorMessage();
    }

    @Override
    public String getMessage() {
        StringBuilder builder = new StringBuilder();
        for (ServiceResultError error : this.getErrors()) {
            int errorCode = error.getErrorCode();
            String errorMessage = error.getErrorMessage();
            String exceptionMessage = error.getException().getMessage();

            builder.append(String.format("ERROR CODE: %s\nERROR MESSAGE: %s\nEXCEPTION MESSAGE: %s\n=====", errorCode,
                                         errorMessage, exceptionMessage));
        }

        return builder.toString();
    }

    public String firstErrorMessageIfAny() {
        ServiceResultError firstError = firstErrorIfAny();
        return firstError == null ? "NO ERROR TO DISPLAY" :
               String.format("GENERIC ERROR MSG: %s\nDETAIL ERROR MSG: %s", firstError.getErrorMessage(),
                             firstError.getException() == null ? "N/A" : firstError.getErrorMessage());
    }

    public int firstErrorCodeIfAny() {
        ServiceResultError firstError = firstErrorIfAny();
        return firstError == null ? -1 : firstError.getErrorCode();
    }

    private ServiceResultError firstErrorIfAny() {
        ServiceResultError error = null;

        if (this.errors.size() > 0) {
            error = this.errors.get(0);
        }

        return error;
    }
}
