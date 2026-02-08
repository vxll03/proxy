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
        // Канал уведомлений
        val channel =
            NotificationChannel(CHANNEL_ID, "Proxy Service", NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        // Интент для кнопки "Остановить"
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

    private fun logLibContents() {
        val libDir = applicationInfo.nativeLibraryDir
        println("--- START LIB INSPECTION ---")
        println("Native Library Dir: $libDir")

        val root = File(libDir)
        if (root.exists() && root.isDirectory) {
            walkAndLog(root, 0)
        } else {
            println("Directory does not exist or is not a directory")
        }
        println("--- END LIB INSPECTION ---")
    }

    private fun walkAndLog(file: File, depth: Int) {
        val indent = "  ".repeat(depth)
        if (file.isDirectory) {
            println("$indent[D] ${file.name}")
            file.listFiles()?.forEach { child ->
                walkAndLog(child, depth + 1)
            }
        } else {
            val executable = if (file.canExecute()) "[X]" else "[ ]"
            println("$indent[F] $executable ${file.name} (${file.length()} bytes)")
        }
    }

    private fun startBinary() {
        if (proxyProcess != null) return

        // Получаем путь к папке, куда Android распаковал наши .so файлы
        logLibContents()
        val libraryDir = applicationInfo.nativeLibraryDir
        val binary = File(libraryDir, "libopera-proxy.so")

        println("DEBUG: Looking for binary at: ${binary.absolutePath}")
        println("DEBUG: File exists: ${binary.exists()}")

        if (!binary.exists()) {
            println("БИНАРНИК НЕ НАЙДЕН ПО ПУТИ: ${binary.absolutePath}")
            return
        }

        Thread {
            try {
                // Теперь запускаем напрямую, права уже должны быть выданы системой
                proxyProcess = ProcessBuilder(binary.absolutePath)
                    .directory(filesDir) // Рабочая директория может остаться прежней
                    .redirectErrorStream(true)
                    .start()

                proxyProcess?.inputStream?.bufferedReader()?.use { reader ->
                    reader.forEachLine { line -> println("PROXY_STDOUT: $line") }
                }
            }
            catch (e: java.io.IOException) {
                if (proxyProcess != null) {
                    println("PROXY: Процесс завершен пользователем")
                } else {
                    e.printStackTrace()
                }
            }
            catch (e: Exception) {
                println("ОШИБКА ЗАПУСКА: ${e.message}")
                e.printStackTrace()
            }
        }.start()
    }

    override fun onDestroy() {
        // Корректное завершение процесса
        proxyProcess?.destroy()
        proxyProcess = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}