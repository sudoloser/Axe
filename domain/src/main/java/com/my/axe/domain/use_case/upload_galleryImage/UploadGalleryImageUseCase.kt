/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * UploadGalleryImageUseCase.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.domain.use_case.upload_galleryImage

import com.my.axe.domain.repository.axeRepository
import java.io.File
import javax.inject.Inject

class UploadGalleryImageUseCase @Inject constructor(
    private val axeRepository: axeRepository
) {
    suspend operator fun invoke(file: File): String? {
        return try {
            file.deleteOnExit()
            axeRepository.uploadImage(file)
        } catch (ex: Exception) {
            null
        }
    }
}