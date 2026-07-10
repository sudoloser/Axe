package com.my.axe.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CdnUploadResponse(
    val success: Boolean,
    val data: CdnUploadData? = null,
)

@Serializable
data class CdnUploadData(
    val link: String? = null,
    @SerialName("deletehash") val deleteHash: String? = null,
)
