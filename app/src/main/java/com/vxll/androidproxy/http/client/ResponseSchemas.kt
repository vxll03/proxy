package com.vxll.androidproxy.http.client

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@OptIn(InternalSerializationApi::class)
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val created_at: String,
    val assets: List<Asset>
)

@Serializable
@OptIn(InternalSerializationApi::class)
data class Asset(
    @SerialName("browser_download_url")
    val downloadUrl: String,
    val name: String
)