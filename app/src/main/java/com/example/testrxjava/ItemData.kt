package com.example.testrxjava

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemData(
    @SerialName("id") val id: Long,
    @SerialName("title") val title: String,
    val marked: Boolean = false,
    val price: Double = 0.0,
    @SerialName("description") val description: String,
    val category: String? = null,
    @SerialName("image") val image: String
)
