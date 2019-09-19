package com.beesightsoft.caf.services.network;

import android.content.Context;

import com.beesightsoft.caf.services.abp_adapter.ThreeTenGsonAdapter;
import com.beesightsoft.caf.services.common.RestMessageResponse;
import com.beesightsoft.caf.services.filter.ApiThrowableFilter;
import com.beesightsoft.caf.services.filter.Filter;
import com.beesightsoft.caf.services.filter.FilterChain;
import com.beesightsoft.caf.services.filter.InterceptFilter;
import com.beesightsoft.caf.services.filter.NetworkFilter;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by nhancao on 3/11/17.
 */

public class DefaultNetworkProvider extends AbstractNetworkProvider implements NetworkProvider {
    private static final String TAG = DefaultNetworkProvider.class.getSimpleName();

    public static EventBus PROGRESS_BUS = new EventBus();

    private boolean isDebug;
    private Map<String, String> headers;
    private List<Interceptor> interceptorList;
    private List<Interceptor> networkInterceptorList;
    private FilterChain filterChain;
    private boolean enableFilter;
    private boolean enableCookie;

    public DefaultNetworkProvider(Context context, boolean isDebug) {
        super(context);
        this.isDebug = isDebug;
        this.headers = new HashMap<>();
        this.interceptorList = new ArrayList<>();
        this.networkInterceptorList = new ArrayList<>();
        this.filterChain = new FilterChain();
    }

    @Override
    public Gson gson() {
        return createBuilder().create();
    }

    @Override
    public boolean isDebug() {
        return isDebug;
    }

    /**
     * <pre>
     * Setting for Realm
     * .setExclusionStrategies(new ExclusionStrategy() {
     * @Override
     * public boolean shouldSkipField(FieldAttributes f) {
     * return f.getDeclaringClass().equals(RealmObject.class);
     * }
     *
     * @Override
     * public boolean shouldSkipClass(Class<?> clazz) {
     * return false;
     * }
     * })
     * </pre>
     */
    @Override
    public GsonBuilder createBuilder() {
        return ThreeTenGsonAdapter.registerLocalTime(new GsonBuilder());
    }

    @Override
    public NetworkProvider addDefaultHeader() {
        addHeader("Content-Type", "application/json");
        return this;
    }

    @Override
    public NetworkProvider addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    @Override
    public NetworkProvider addFilter(InterceptFilter interceptFilter) {
        filterChain.addFilter(interceptFilter);
        return this;
    }

    @Override
    public NetworkProvider clearFilter() {
        filterChain.clearFilter();
        return this;
    }

    @Override
    public NetworkProvider enableFilter(boolean enableFilter) {
        this.enableFilter = enableFilter;
        return this;
    }

    @Override
    public NetworkProvider enableCookie(boolean enableCookie) {
        this.enableCookie = enableCookie;
        return this;
    }

    @Override
    public NetworkProvider addInterceptor(Interceptor interceptor) {
        interceptorList.add(interceptor);
        return this;
    }

    @Override
    public NetworkProvider addNetworkInterceptor(Interceptor interceptor) {
        networkInterceptorList.add(interceptor);
        return this;
    }

    @Override
    public <T> T provideApi(String baseUrl, Class<T> apiClass) {
        return provideApi(baseUrl, apiClass, false);
    }

    @Override
    public <T> T provideApi(String baseUrl, Class<T> apiClass, boolean enableProgress) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //Set timeout
        int timeOut = getTimeout();
        builder.connectTimeout(timeOut, TimeUnit.SECONDS);
        builder.readTimeout(timeOut, TimeUnit.SECONDS);
        builder.writeTimeout(timeOut, TimeUnit.SECONDS);

        //Set interceptor
        builder.addInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            if (headers == null || headers.size() == 0) {
                addDefaultHeader();
            }
            for (Map.Entry<String, String> keyValueEntry : headers.entrySet()) {
                requestBuilder.addHeader(keyValueEntry.getKey(), keyValueEntry.getValue());
            }
            return chain.proceed(requestBuilder.build());
        });

        //Enable log
        if (isDebug()) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(getLevel());
            builder.addInterceptor(interceptor);
        }

        //Add progress
        if (enableProgress) {
            builder.addNetworkInterceptor(chain -> {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(),
                                (bytesRead, contentLength, done) -> {
                                    PROGRESS_BUS.post(new ProgressBus(apiClass,
                                            bytesRead,
                                            contentLength,
                                            done));
                                }))
                        .build();

            });
        }

        //Enable cookie
        if(enableCookie) {
            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            builder.cookieJar(cookieJar);
        }

        //Provide interceptors
        for (Interceptor interceptor : interceptorList) {
            builder.addInterceptor(interceptor);
        }
        for (Interceptor interceptor : networkInterceptorList) {
            builder.addNetworkInterceptor(interceptor);
        }

        OkHttpClient okHttpClient = builder.build();

        Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        return restAdapter.create(apiClass);
    }

    @Override
    public <TResponse extends RestMessageResponse<TResult>, TResult> Observable<TResult> transformResponse(
            Observable<TResponse> call) {
        return transformResponse(call, true);
    }

    @Override
    public <TResponse extends RestMessageResponse<TResult>, TResult> Observable<TResult> transformResponse(
            Observable<TResponse> call, boolean enableFilter) {

        Observable<TResponse> res = call.observeOn(Schedulers.computation());
        //Filter with data original
        Filter<TResponse, Observable<TResponse>> rootFilter = getRootFilter();
        if (rootFilter != null) {
            res = res.flatMap(rootFilter::execute);
        }
        //Parse error filter
        res = res
                .onErrorResumeNext(throwable -> new NetworkFilter<TResponse>(this).execute(throwable))
                .onErrorResumeNext(throwable -> new ApiThrowableFilter<TResponse>().execute(throwable));
        //Filter with data after error filter
        Filter<TResponse, Observable<TResponse>> commonFilter = getCommonFilter();
        if (commonFilter != null) {
            res = res.flatMap(commonFilter::execute);
        }
        Observable<TResult> result = res
                .flatMap(tResponse -> Observable.just(tResponse.getData()));

        if (this.enableFilter && enableFilter) {
            result = filterChain.execute(result);
        }
        return result.onExceptionResumeNext(Observable.empty());
    }

    @Override
    public <TResponse> Observable<TResponse> verifyResponse(Observable<TResponse> call) {
        return verifyResponse(call, true);
    }

    @Override
    public <TResponse> Observable<TResponse> verifyResponse(Observable<TResponse> call, boolean enableFilter) {
        Observable<TResponse> res = call.observeOn(Schedulers.computation());
        //Filter with data original
        Filter<TResponse, Observable<TResponse>> rootFilter = getRootFilter();
        if (rootFilter != null) {
            res = res.flatMap(rootFilter::execute);
        }
        //Parse error filter
        res = res
                .onErrorResumeNext(throwable -> new NetworkFilter<TResponse>(this).execute(throwable))
                .onErrorResumeNext(throwable -> new ApiThrowableFilter<TResponse>().execute(throwable));
        //Filter with data after error filter
        Filter<TResponse, Observable<TResponse>> commonFilter = getCommonFilter();
        if (commonFilter != null) {
            res = res.flatMap(commonFilter::execute);
        }
        res = res.flatMap(Observable::just);

        if (this.enableFilter && enableFilter) {
            res = filterChain.execute(res);
        }
        return res.onExceptionResumeNext(Observable.empty());
    }

    protected FilterChain getFilterChain() {
        return filterChain;
    }

    protected boolean isEnableFilter() {
        return enableFilter;
    }

}
