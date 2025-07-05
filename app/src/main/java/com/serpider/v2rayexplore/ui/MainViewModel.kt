package com.serpider.v2rayexplore.ui

import androidx.lifecycle.viewModelScope
import com.serpider.v2rayexplore.data.model.*
import com.serpider.v2rayexplore.data.repository.V2RayRepository
import com.serpider.v2rayexplore.ui.BaseViewModel
import com.serpider.v2rayexplore.utils.ConfigType
import com.serpider.v2rayexplore.utils.DetectConfigType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: V2RayRepository = V2RayRepository()
) : BaseViewModel() {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _pingResult = MutableStateFlow<PingResult?>(null)
    val pingResult = _pingResult.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage = _toastMessage.asStateFlow()

    init {
        observeConnectionState()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                _connectionState.value = state
                when (state) {
                    is ConnectionState.Connected -> {
                        showToast("Successfully connected to V2Ray!")
                    }
                    is ConnectionState.Error -> {
                        showToast("Connection failed: ${state.message}")
                    }
                    else -> {}
                }
            }
        }
    }

    fun connect(configText: String) {
        if (configText.isEmpty()) {
            showToast("Please paste V2Ray config")
            return
        }

        val configType = DetectConfigType.checkType(configText)
        if (configType == ConfigType.UNKNOWN) {
            showToast("Invalid config format")
            return
        }

        val config = V2RayConfig(configText, configType)

        viewModelScope.launch(exceptionHandler) {
            setLoading(true)
            val result = repository.connect(config)
            setLoading(false)

            if (result.isFailure) {
                showToast("Connection failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch(exceptionHandler) {
            repository.disconnect()
        }
    }

    fun ping() {
        viewModelScope.launch(exceptionHandler) {
            setLoading(true)
            val result = repository.ping()
            setLoading(false)
            _pingResult.value = result

            if (result.isSuccess) {
                showToast("Ping: ${result.latency}ms")
            } else {
                showToast("Ping failed: ${result.error}")
            }
        }
    }

    private fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}