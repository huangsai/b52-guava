package com.mobile.guava.android.mvvm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.mobile.guava.jvm.coroutines.Bus

object AndroidX {

    const val APK_PACKAGE_ARCHIVE_TYPE = "application/vnd.android.package-archive"
    const val SQL_DB3 = "sql.db3"
    const val ASSETS = "file:///android_asset/"

    const val BUS_EXIT = 9000
    const val BUS_LOGOUT = 9001
    const val BUS_LOGIN = 9002
    const val BUS_DIALOG_COUNT = 9003

    @get:JvmName("appDialogCount")
    val appDialogCount: MutableLiveData<Int> = MutableLiveData(0)

    @get:JvmName("isNetworkConnected")
    val isNetworkConnected: MutableLiveData<Boolean> = MutableLiveData(false)

    @get:JvmName("isAppInForeground")
    val isAppInForeground: MutableLiveData<Boolean> = MutableLiveData(false)

    @get:JvmName("isSocketConnected")
    val isSocketConnected: MutableLiveData<Boolean> = MutableLiveData(false)

    @get:JvmName("myApp")
    lateinit var myApp: Application
        private set

    fun setup(app: Application) {
        myApp = app
    }

    fun exitSystem() = Bus.offer(BUS_EXIT)

    fun notifyLogin() = Bus.offer(BUS_LOGIN)

    fun notifyLogout() = Bus.offer(BUS_LOGOUT)

    fun notifyDialogShow() {
        appDialogCount.value = appDialogCount.value!! + 1
        Bus.offer(BUS_DIALOG_COUNT)
    }

    fun notifyDialogDismiss() {
        appDialogCount.value = appDialogCount.value!! - 1
        Bus.offer(BUS_DIALOG_COUNT)
    }
}