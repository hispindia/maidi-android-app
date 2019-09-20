package com.beesightsoft.caf.services.schedulers;

import rx.android.plugins.RxAndroidPlugins;
import rx.plugins.RxJavaPlugins;

/**
 * Created by nhancao on 4/14/17.
 */

public class DefaultTest {

    public void setUp() throws Exception {
        RxJavaPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().reset();
        RxJavaPlugins.getInstance().registerSchedulersHook(new SchedulerHook());
        RxJavaPlugins.getInstance().registerObservableExecutionHook(new ExecutionHook());
        RxAndroidPlugins.getInstance().registerSchedulersHook(new AndroidSchedulerHook());
    }

    public void tearDown() throws Exception {
        RxJavaPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().reset();
    }


}
