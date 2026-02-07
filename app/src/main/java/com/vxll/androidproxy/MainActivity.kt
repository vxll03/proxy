package com.vxll.androidproxy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vxll.androidproxy.ui.theme.AndroidProxyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidProxyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Заголовок",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ToggleButton()
    }
}

@Composable
fun ToggleButton(context: android.content.Context) {
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            downloadGitHubFile(context, "Snawoot", "opera-proxy")
        }
    }) {
        Text(text = "Нажми меня")
    }
}

suspend fun downloadGitHubFile(context: android.content.Context, owner: String, repo: String) {
    withContext(Dispatchers.IO) {
        try {
            val url = "https://github.com/$owner/$repo/archive/refs/heads/main.zip"

            val connection = URL(url).openConnection()
            val inputStream = connection.getInputStream()

            val file = File(context.filesDir, "latest_release.zip")

            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }

            println("Файл успешно сохранен в: ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}