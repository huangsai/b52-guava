package com.mobile.guava.android.mvvm

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import com.mobile.guava.jvm.coroutines.Bus

abstract class BaseAppCompatDialogFragment : AppCompatDialogFragment() {

    var applyDialogCount = false
        protected set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true
        if (applyDialogCount) {
            AndroidX.notifyDialogShow()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (applyDialogCount) {
            AndroidX.notifyDialogDismiss()
        }
    }

    open fun onBackPressed(): Boolean = false
}