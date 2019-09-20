package com.beesightsoft.caf.services.schedulers;

import rx.Scheduler;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.Schedulers;

/**
 * Created by nhancao on 4/14/17.
 */

public class AndroidSchedulerHook extends RxAndroidSchedulersHook {
    @Override
    public Scheduler getMainThreadScheduler() {
        return Schedulers.immediate();
    }
}
