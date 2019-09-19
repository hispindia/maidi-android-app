package com.beesightsoft.caf.services.filter;

import com.beesightsoft.caf.exceptions.ApiThrowable;
import com.beesightsoft.caf.models.ServiceResultError;
import com.beesightsoft.caf.exceptions.ErrorCodes;
import com.beesightsoft.caf.services.common.MessageResponse;
import com.beesightsoft.caf.services.common.RestErrorResponse;
import com.beesightsoft.caf.services.common.RestMessageResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

/**
 * Created by nhancao on 4/16/17.
 */

public class ApiThrowableFilter<T> implements Filter<Throwable, Observable<T>> {

    public ApiThrowableFilter() {
    }

    public ApiThrowable onHandleFailedResponse(int responseCode, String rawString) {
        ApiThrowable exception;
        try {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<RestMessageResponse>() {
            }.getType();
            RestMessageResponse<RestErrorResponse> responseMessage = gson.fromJson(rawString, collectionType);

            List<RestErrorResponse> errors = responseMessage.getErrors();
            if (errors == null || errors.size() == 0) {
                try {
                    MessageResponse messageResponse = gson.fromJson(rawString, MessageResponse.class);
                    exception = ApiThrowable.from(responseCode, messageResponse.getMessage());
                } catch (Exception ex) {
                    exception = ApiThrowable.from(responseCode, rawString);
                }
            } else {
                List<ServiceResultError> serviceResultErrors = new ArrayList<>();
                for (RestErrorResponse error : errors) {
                    serviceResultErrors.add(new ServiceResultError(error.getErrorCode(), error.getErrorMessage()));
                }

                exception = ApiThrowable.from(serviceResultErrors);
            }
        } catch (Exception e) {
            exception = ApiThrowable.from(e);
        }
        return exception;
    }

    @Override
    public Observable<T> execute(Throwable throwable) {

        if (throwable instanceof HttpException) {
            ResponseBody failedResponse = ((HttpException) throwable).response().errorBody();
            int responseCode = ((HttpException) throwable).response().code();

            if (failedResponse == null) {
                return Observable.error(ApiThrowable.from(responseCode,
                                                          "Response Error Body is empty"));
            } else {
                String rawString = "";
                try {
                    rawString = failedResponse.string();
                    return Observable.error(onHandleFailedResponse(responseCode, rawString));
                } catch (Exception ex) {
                    return Observable
                            .error(ApiThrowable.from(ErrorCodes.GENERAL_ERROR, rawString, ex));
                }
            }
        }
        return Observable.error(throwable);
    }

}
