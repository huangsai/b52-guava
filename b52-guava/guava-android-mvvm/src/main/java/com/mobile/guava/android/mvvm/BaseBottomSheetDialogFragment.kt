package com.mobile.guava.android.mvvm

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

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