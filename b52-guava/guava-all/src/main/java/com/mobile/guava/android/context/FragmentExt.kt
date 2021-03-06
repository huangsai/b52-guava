package com.mobile.guava.android.context

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import com.mobile.guava.android.ime.ImeUtils

fun Fragment.instantiate(className: String): Fragment {
    return childFragmentManager.fragmentFactory.instantiate(
            activity?.classLoader ?: javaClass.classLoader!!,
            className
    )
}

fun Fragment.hideSoftInput() = this.view?.let { ImeUtils.hideIme(it) }

fun Fragment.applyNavigationBarColor(@ColorRes colorRes: Int) {
    requireActivity().applyStatusBarColor(colorRes)
}

fun Fragment.applyStatusBarColor(@ColorRes colorRes: Int) {
    requireActivity().applyStatusBarColor(colorRes)
}

@Suppress("UNCHECKED_CAST")
fun <T : Activity> Fragment.ofActivity() = requireActivity() as T

@Suppress("UNCHECKED_CAST")
fun <T : Fragment> Fragment.ofParent() = requireParentFragment() as T