/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppsState.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_apps_rpc

import androidx.compose.runtime.Immutable
import com.my.axe.data.utils.AppsInfo

@Immutable
data class AppsState(
    val apps: List<AppsInfo> = emptyList(),
    val enabledApps: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
)
