package com.my.axe.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CdnUploadResponse(
    val success: Boolean,
    val short: String? = null,
    val url: String? = null,
)
