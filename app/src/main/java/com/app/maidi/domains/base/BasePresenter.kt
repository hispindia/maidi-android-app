package com.app.maidi.domains.base

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter
import com.hannesdorfmann.mosby3.mvp.MvpPresenter

open class BasePresenter<V : BaseView> : MvpBasePresenter<V>() {

}