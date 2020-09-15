package com.mobile.guava.android.mvvm.dagger

import androidx.lifecycle.ViewModelProvider
import com.mobile.guava.android.mvvm.lifecycle.ViewModelFactory
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class SingletonScopeViewModelFactoryBinder {

    @Binds
    @Singleton
    abstract fun provideViewModelFactory(it: ViewModelFactory): ViewModelProvider.Factory
}