/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * GetGamesUseCase.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.domain.use_case.get_games

import com.my.axe.domain.model.Resource
import com.my.axe.domain.model.Game
import com.my.axe.domain.repository.AxeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class GetGamesUseCase @Inject constructor(
    private val AxeRepository: AxeRepository
) {
    operator fun invoke(): Flow<Resource<List<Game>>> = flow {
        try {
            emit(Resource.Loading())
            val games = AxeRepository.getGames()
            emit(Resource.Success(games))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}