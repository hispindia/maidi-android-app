package com.app.maidi.infrastructures.scope

import javax.inject.Qualifier
import javax.inject.Scope

@Qualifier
@Retention(value = AnnotationRetention.RUNTIME)
@Scope
annotation class ApplicationScope {
}
