/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserViewModel.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_profile.ui.user

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.axe.domain.model.Resource
import com.my.axe.domain.model.user.User
import com.my.axe.domain.use_case.get_user.GetUserUseCase
import com.my.axe.preference.Prefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
): ViewModel() {

    private val _state: MutableState<UserState> = mutableStateOf(UserState.Loading)
    val state: State<UserState> = _state

    init {
        getUser()
    }

    private fun getUser() {
        viewModelScope.launch {
            try {
                withTimeout(15_000L) {
                    getUserUseCase(Prefs[Prefs.USER_ID, ""]).collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = UserState.LoadingCompleted(
                                    user = result.data?.copy(
                                        bio = Prefs[Prefs.USER_BIO],
                                        nitro = Prefs[Prefs.USER_NITRO]
                                    )
                                )
                                Prefs[Prefs.USER_DATA] = Json.encodeToString(result.data)
                            }

                            is Resource.Error -> {
                                val user = runCatching {
                                    Json.decodeFromString<User>(Prefs[Prefs.USER_DATA, "{}"])
                                }.getOrNull()
                                _state.value = UserState.Error(
                                    error = result.message ?: "An unexpected error occurred",
                                    user = user
                                )
                            }

                            is Resource.Loading -> {
                                _state.value = UserState.Loading
                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                val user = runCatching {
                    Json.decodeFromString<User>(Prefs[Prefs.USER_DATA, "{}"])
                }.getOrNull()
                _state.value = UserState.Error(
                    error = "Request timed out. Please check your connection.",
                    user = user
                )
            }
        }
    }
}