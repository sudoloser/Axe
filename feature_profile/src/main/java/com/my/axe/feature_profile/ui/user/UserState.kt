/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UserState.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_profile.ui.user

import com.my.axe.domain.model.user.User

sealed interface UserState {
    object Loading: UserState
    class Error(val error: String,val user: User?): UserState
    class LoadingCompleted(val user: User?): UserState
}