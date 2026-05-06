/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GamesState.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_console_rpc

import com.my.axe.domain.model.Game

sealed interface GamesState {
    object Loading: GamesState
    class Success(val games: List<Game>): GamesState
    class Error(val error: String): GamesState
}