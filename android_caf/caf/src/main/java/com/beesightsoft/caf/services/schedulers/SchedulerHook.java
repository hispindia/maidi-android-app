package com.beesightsoft.caf.services.schedulers;

import rx.Scheduler;
import rx.plugins.RxJavaSchedulersHook;
import rx.schedulers.Schedulers;

/**
 * Created by nhancao on 4/14/17.
 */

public class SchedulerHook extends RxJavaSchedulersHook {
    @Override
    public Scheduler getIOScheduler() {
        return Schedulers.immediate();
    }
}