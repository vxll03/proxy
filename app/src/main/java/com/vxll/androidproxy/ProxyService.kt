package com.vxll.androidproxy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class ProxyService : Service() {
    private var proxyProcess: Process? = null

    companion object {
        const val ACTION_STOP = "com.vxll.androidproxy.STOP_PROXY"
        const val CHANNEL_ID = "proxy_channel"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(1, createNotification())
        startBinary()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val channel =
            NotificationChannel(CHANNEL_ID, "Proxy Service", NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val stopIntent = Intent(this, ProxyService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Opera Proxy запущен")
            .setContentText("Прокси-сервер работает в фоне")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Остановить",
                stopPendingIntent
            )
            .build()
    }

    private fun startBinary() {
        if (proxyProcess != null) return

        val libraryDir = applicationInfo.nativeLibraryDir
        val binary = File(libraryDir, "libopera-proxy.so")

        if (!binary.exists()) {
            println("BINARY FILE NOT FOUND IN: ${binary.absolutePath}")
            return
        }

        Thread {
            try {
                proxyProcess = ProcessBuilder(binary.absolutePath)
                    .directory(filesDir)
                    .redirectErrorStream(true)
                    .start()

                proxyProcess?.inputStream?.bufferedReader()?.use { reader ->
                    reader.forEachLine { line -> println("PROXY_STDOUT: $line") }
                }
            }
            catch (e: java.io.IOException) {
                if (proxyProcess != null) {
                    println("PROXY: Process finished by user")
                } else {
                    e.printStackTrace()
                }
            }
            catch (e: Exception) {
                println("ERROR: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        proxyProcess?.destroy()
        proxyProcess = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}