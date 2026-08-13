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
import az.simplesoft.speakapro.audio.AudioRecorder
import az.simplesoft.speakapro.ui.TranslatorScreen

class MainActivity : ComponentActivity() {
    private val recorder = AudioRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var listening by remember { mutableStateOf(false) }
            var level by remember { mutableFloatStateOf(0f) }
            var frames by remember { mutableLongStateOf(0L) }
            var message by remember { mutableStateOf<String?>(null) }

            fun begin() {
                message = null
                recorder.start(
                    onFrame = { frame -> runOnUiThread {
                        level = frame.level
                        frames += 1
                    } },
                    onError = { problem -> runOnUiThread {
                        listening = false
                        message = problem.message
                    } },
                )
                listening = true
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
                        recorder.stop()
                        listening = false
                        level = 0f
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
        super.onDestroy()
    }
}
