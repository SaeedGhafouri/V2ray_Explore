package com.serpider.v2rayexplore.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        handleException(exception)
    }

    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    protected open fun handleException(exception: Throwable) {
        // مدیریت خطاها
    }
}