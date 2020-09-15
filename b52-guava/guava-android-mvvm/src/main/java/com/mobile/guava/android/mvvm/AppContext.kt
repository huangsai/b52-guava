package com.mobile.guava.android.mvvm

import com.mobile.guava.https.PlatformContext
import java.io.File

class AppContext : PlatformContext {

    override fun getCacheDir(): File = AndroidX.myApp.cacheDir

    override fun getFilesDir(): File = AndroidX.myApp.filesDir

    override fun getDatabasePath(name: String): File = AndroidX.myApp.getDatabasePath(name)!!

    override fun getExternalCacheDir(): File = AndroidX.myApp.externalCacheDir!!

    override fun getExternalFilesDir(type: String?): File {
        return AndroidX.myApp.getExternalFilesDir(type)!!
    }
}