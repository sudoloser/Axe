/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppsScreenViewModel.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_apps_rpc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.axe.data.utils.ConfigUtils
import com.my.axe.data.utils.getInstalledApps
import com.my.axe.domain.model.rpc.RpcConfig
import com.my.axe.preference.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val _state: MutableStateFlow<AppsState> = MutableStateFlow(AppsState())
    val state = _state.asStateFlow()

    private val _previewConfig: MutableStateFlow<RpcConfig?> = MutableStateFlow(null)
    val previewConfig = _previewConfig.asStateFlow()

    init {
        getInstalledApps()
    }

    fun getInstalledApps() {
        viewModelScope.launch(context = Dispatchers.Default) {
            val appList = getInstalledApps(
                context = context,
                isEnabled = Prefs::isAppEnabled
            ).sortedBy { !it.isChecked }
            val enabledApps = appList.associate { it.pkg to it.isChecked }
            val customConfigs = ConfigUtils.getAllConfigs(context)
            val appConfigs = Prefs.getAppCustomConfigs()

            _state.update {
                AppsState(
                    apps = appList,
                    isLoading = false,
                    enabledApps = enabledApps,
                    customConfigs = customConfigs,
                    appConfigs = appConfigs
                )
            }
        }
    }

    fun updateAppEnabled(pkg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Prefs.saveToPrefs(pkg)
            _state.update { currentState ->
                currentState.copy(
                    enabledApps = currentState.enabledApps.toMutableMap().apply {
                        this[pkg] = !this[pkg]!!
                    },
                )
            }
        }
    }

    fun updateAppConfig(pkg: String, configName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            Prefs.saveAppCustomConfig(pkg, configName)
            _state.update { currentState ->
                currentState.copy(
                    appConfigs = currentState.appConfigs.toMutableMap().apply {
                        if (configName == null) remove(pkg) else this[pkg] = configName
                    }
                )
            }
        }
    }

    fun showPreview(configName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = ConfigUtils.loadConfig(context, configName)
            _previewConfig.value = config
        }
    }

    fun dismissPreview() {
        _previewConfig.value = null
    }
}
