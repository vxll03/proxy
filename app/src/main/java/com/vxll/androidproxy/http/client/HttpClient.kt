package com.vxll.androidproxy.http.client

import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

// TODO: SHA256 check

class HttpClient(
    private val owner: String,
    private val repo: String,
) {
    private val client: OkHttpClient = OkHttpClient()
    private val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private fun getAssetName(): String {
        val archSuffix = if (Build.SUPPORTED_ABIS.contains("arm64-v8a")) "64" else ""
        return "opera-proxy.android-arm$archSuffix"
    }

    suspend fun getLatestReleaseUrl(): String? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://api.github.com/repos/$owner/$repo/releases/latest")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val release = json.decodeFromString<GitHubRelease>(body)
                val targetName = getAssetName()

                return@withContext release.assets.find { it.name == targetName }?.downloadUrl
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadFile(fileUrl: String, destinationFile: File): File? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(fileUrl).build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null

                    val body = response.body ?: return@withContext null

                    body.byteStream().use { inputStream ->
                        destinationFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    destinationFile
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}