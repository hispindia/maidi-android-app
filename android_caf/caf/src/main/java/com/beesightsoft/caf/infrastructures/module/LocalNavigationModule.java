package com.beesightsoft.caf.infrastructures.module;


import com.beesightsoft.caf.infrastructures.scope.ActivityScope;
import com.beesightsoft.caf.models.LocalCiceroneHolder;

import dagger.Module;
import dagger.Provides;

/**
 * Created by nhancao on 4/20/17.
 */

@Module
public class LocalNavigationModule {

    @Provides
    @ActivityScope
    LocalCiceroneHolder provideLocalNavigationHolder() {
        return new LocalCiceroneHolder();
    }
}
