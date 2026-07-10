package com.my.axe.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class WebhookPayload(
    val embeds: List<Embed>,
)

@Serializable
data class Embed(
    val title: String,
    val description: String,
    val color: Int,
    val fields: List<Field>,
    val image: Image? = null,
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
