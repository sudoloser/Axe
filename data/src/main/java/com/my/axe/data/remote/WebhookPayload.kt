package com.my.axe.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebhookPayload(
    val embeds: List<Embed>,
)

@Serializable
data class Embed(
    val title: String? = null,
    val description: String? = null,
    val color: Int? = null,
    val url: String? = null,
    val fields: List<Field>? = null,
    val image: Image? = null,
    val thumbnail: Thumbnail? = null,
    val author: Author? = null,
    val footer: Footer? = null,
    val timestamp: String? = null,
)

@Serializable
data class Field(
    val name: String,
    val value: String,
    val `inline`: Boolean = false,
)

@Serializable
data class Image(
    val url: String,
)

@Serializable
data class Thumbnail(
    val url: String,
)

@Serializable
data class Author(
    val name: String,
    @SerialName("icon_url") val iconUrl: String? = null,
)

@Serializable
data class Footer(
    val text: String,
    @SerialName("icon_url") val iconUrl: String? = null,
)
