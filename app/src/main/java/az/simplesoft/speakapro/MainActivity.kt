package az.simplesoft.speakapro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import az.simplesoft.speakapro.audio.AudioPlayer
import az.simplesoft.speakapro.audio.AudioRecorder
import az.simplesoft.speakapro.live.GeminiLiveClient
import az.simplesoft.speakapro.ui.TranslatorScreen

class MainActivity : ComponentActivity() {
    private val recorder = AudioRecorder()
    private val player = AudioPlayer()
    private var live: GeminiLiveClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var listening by remember { mutableStateOf(false) }
            var level by remember { mutableFloatStateOf(0f) }
            var frames by remember { mutableLongStateOf(0L) }
            var message by remember { mutableStateOf<String?>(null) }

            fun stopAll() {
                recorder.stop()
                live?.close()
                live = null
                player.stop()
                listening = false
                level = 0f
            }

            fun begin() {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    message = "Добавь GEMINI_API_KEY в local.properties"
                    return
                }

                message = null
                listening = true
                frames = 0

                val events = object : GeminiLiveClient.Events {
                    override fun ready() {
                        runOnUiThread {
                            try {
                                player.start()
                                recorder.start(
                                    onFrame = { frame ->
                                        live?.sendAudio(frame.pcm16le)
                                        runOnUiThread {
                                            level = frame.level
                                            frames += 1
                                        }
                                    },
                                    onError = { problem -> runOnUiThread {
                                        message = problem.message ?: "Ошибка микрофона"
                                        stopAll()
                                    } },
                                )
                            } catch (t: Throwable) {
                                message = t.message ?: "Не удалось запустить аудио"
                                stopAll()
                            }
                        }
                    }

                    override fun audio(bytes: ByteArray) {
                        player.write(bytes)
                    }

                    override fun inputText(text: String) = Unit
                    override fun outputText(text: String) = Unit

                    override fun error(messageText: String) {
                        runOnUiThread {
                            message = messageText
                            stopAll()
                        }
                    }

                    override fun closed() {
                        runOnUiThread {
                            if (listening) stopAll()
                        }
                    }
                }

                live = GeminiLiveClient(
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    targetLanguage = "ru",
                    events = events,
                ).also { it.connect() }
            }

            val askForMic = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { granted ->
                if (granted) begin() else message = "Нужен доступ к микрофону"
            }

            TranslatorScreen(
                isListening = listening,
                microphoneLevel = level,
                frameCount = frames,
                error = message,
                onToggleListening = {
                    if (listening) {
                        stopAll()
                    } else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        begin()
                    } else {
                        askForMic.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            )
        }
    }

    override fun onDestroy() {
        recorder.stop()
        live?.close()
        player.stop()
        super.onDestroy()
    }
}
