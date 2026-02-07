package com.vxll.androidproxy.http.client

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HttpClient(
    private val owner: String,
    private val repo: String,
    private val client: OkHttpClient = OkHttpClient()
) {
    fun getLatestReleaseAndDownload() {
        val request = Request.Builder()
            .url("https://api.github.com/repos/${owner}/${repo}/releases/latest")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val body = response.body?.string()
                    // Здесь нужно распарсить JSON (например, через Gson или Kotlin Serialization)
                    // И найти нужный URL в массиве "assets"
                    val downloadUrl = "https://example.com/file_from_github.zip"
                    println("response ${response.code}")
                    // downloadFile(downloadUrl, "my_file.zip")
                }
            }
        })
    }
}