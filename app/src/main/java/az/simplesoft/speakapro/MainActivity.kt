package az.simplesoft.speakapro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import az.simplesoft.speakapro.audio.AudioPlayer
import az.simplesoft.speakapro.audio.AudioRecorder
import az.simplesoft.speakapro.live.GeminiLiveClient
import az.simplesoft.speakapro.ui.LanguageOption
import az.simplesoft.speakapro.ui.TranslatorScreen

class MainActivity : ComponentActivity() {
    private val recorder = AudioRecorder()
    private val player = AudioPlayer()
    private var live: GeminiLiveClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val languages = remember { listOf(
                LanguageOption("ru", "Русский", "🇷🇺"),
                LanguageOption("en", "English", "🇬🇧"),
                LanguageOption("az", "Azərbaycan", "🇦🇿"),
                LanguageOption("tr", "Türkçe", "🇹🇷")
            ) }
            var selected by remember { mutableStateOf(languages.first()) }
            var listening by remember { mutableStateOf(false) }
            var level by remember { mutableFloatStateOf(0f) }
            var frames by remember { mutableLongStateOf(0L) }
            var input by remember { mutableStateOf("") }
            var output by remember { mutableStateOf("") }
            var message by remember { mutableStateOf<String?>(null) }

            fun stopAll() {
                recorder.stop(); live?.close(); live = null; player.stop()
                listening = false; level = 0f
            }

            fun begin() {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) { message = "Добавь GEMINI_API_KEY в local.properties"; return }
                message = null; listening = true; frames = 0; input = ""; output = ""
                val events = object : GeminiLiveClient.Events {
                    override fun ready() = runOnUiThread {
                        try {
                            player.start()
                            recorder.start(
                                onFrame = { frame -> live?.sendAudio(frame.pcm16le); runOnUiThread { level = frame.level; frames++ } },
                                onError = { problem -> runOnUiThread { message = problem.message ?: "Ошибка микрофона"; stopAll() } }
                            )
                        } catch (t: Throwable) { message = t.message ?: "Не удалось запустить аудио"; stopAll() }
                    }
                    override fun audio(bytes: ByteArray) { player.write(bytes) }
                    override fun inputText(text: String) = runOnUiThread { input = text }
                    override fun outputText(text: String) = runOnUiThread { output = text }
                    override fun error(messageText: String) = runOnUiThread { message = messageText; stopAll() }
                    override fun closed() = runOnUiThread { if (listening) stopAll() }
                }
                live = GeminiLiveClient(BuildConfig.GEMINI_API_KEY, selected.code, events).also { it.connect() }
            }

            val askForMic = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) begin() else message = "Нужен доступ к микрофону"
            }

            TranslatorScreen(
                isListening = listening,
                statusText = if (listening) "Слушаю…" else "Готов к переводу",
                microphoneLevel = level,
                frameCount = frames,
                inputText = input,
                outputText = output,
                selectedLanguage = selected,
                languages = languages,
                outputDeviceLabel = "Аудиовыход Android",
                headphonesConnected = false,
                error = message,
                onLanguageSelected = { if (!listening) selected = it },
                onToggleListening = {
                    if (listening) stopAll()
                    else if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) begin()
                    else askForMic.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }
    }

    override fun onDestroy() { recorder.stop(); live?.close(); player.stop(); super.onDestroy() }
}
