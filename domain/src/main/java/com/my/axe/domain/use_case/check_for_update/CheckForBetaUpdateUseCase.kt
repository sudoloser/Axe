package com.my.axe.domain.use_case.check_for_update

import com.my.axe.domain.model.Resource
import com.my.axe.domain.model.release.Release
import com.my.axe.domain.repository.AxeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CheckForBetaUpdateUseCase @Inject constructor(
    private val repository: AxeRepository
) {
    operator fun invoke(): Flow<Resource<Release>> = flow {
        try {
            emit(Resource.Loading())
            val release = repository.checkForBetaUpdate()
            emit(Resource.Success(release))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }
    }
}
