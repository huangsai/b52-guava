package com.mobile.guava.android.mvvm.dagger

import androidx.lifecycle.ViewModelProvider

interface ViewModelFactoryComponent {

    fun viewModelFactory(): ViewModelProvider.Factory
}