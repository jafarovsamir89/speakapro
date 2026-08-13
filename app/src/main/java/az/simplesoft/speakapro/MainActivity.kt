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
import az.simplesoft.speakapro.audio.AudioRouter
import az.simplesoft.speakapro.live.GeminiLiveClient
import az.simplesoft.speakapro.ui.SupportedLanguages
import az.simplesoft.speakapro.ui.TranslatorScreen

class MainActivity : ComponentActivity() {
    private val recorder = AudioRecorder()
    private val player = AudioPlayer()
    private var live: GeminiLiveClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val router = remember { AudioRouter(this) }
            val languages = remember { SupportedLanguages }
            var selected by remember { mutableStateOf(languages.first()) }
            var listening by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Готов к переводу") }
            var level by remember { mutableFloatStateOf(0f) }
            var frames by remember { mutableLongStateOf(0L) }
            var input by remember { mutableStateOf("") }
            var output by remember { mutableStateOf("") }
            var message by remember { mutableStateOf<String?>(null) }
            var route by remember { mutableStateOf(router.state()) }

            fun refreshRoute() {
                route = router.state()
                if (listening) {
                    recorder.routeTo(router.preferredInput())
                    player.routeTo(router.preferredOutput())
                }
            }
            DisposableEffect(Unit) {
                val callback = router.register { runOnUiThread { refreshRoute() } }
                onDispose { router.unregister(callback) }
            }
            fun appendText(current: String, next: String): String {
                val clean = next.trim()
                if (clean.isBlank() || current.endsWith(clean)) return current
                return (if (current.isBlank()) clean else "$current $clean").takeLast(700)
            }
            fun stopAll() {
                recorder.stop(); live?.close(); live = null; player.stop()
                listening = false; level = 0f; status = "Готов к переводу"
            }

            fun begin() {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) { message = "Добавь GEMINI_API_KEY в local.properties"; return }
                refreshRoute(); message = null; listening = true; status = "Подключаюсь…"; frames = 0; input = ""; output = ""
                val events = object : GeminiLiveClient.Events {
                    override fun ready() = runOnUiThread {
                        try {
                            player.start(router.preferredOutput())
                            recorder.start(
                                preferredDevice = router.preferredInput(),
                                onFrame = { frame -> live?.sendAudio(frame.pcm16le); runOnUiThread { level = frame.level; frames++ } },
                                onError = { problem -> runOnUiThread { message = problem.message ?: "Ошибка микрофона"; stopAll() } }
                            )
                            status = "Слушаю…"
                        } catch (t: Throwable) { message = t.message ?: "Не удалось запустить аудио"; stopAll() }
                    }
                    override fun audio(bytes: ByteArray) { player.write(bytes) }
                    override fun inputText(text: String) = runOnUiThread { input = appendText(input, text) }
                    override fun outputText(text: String) = runOnUiThread { output = appendText(output, text) }
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
                statusText = status,
                microphoneLevel = level,
                frameCount = frames,
                inputText = input,
                outputText = output,
                selectedLanguage = selected,
                languages = languages,
                outputDeviceLabel = route.label,
                headphonesConnected = route.headphonesConnected,
                error = message,
                onLanguageSelected = { if (!listening) { selected = it; input = ""; output = "" } },
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
