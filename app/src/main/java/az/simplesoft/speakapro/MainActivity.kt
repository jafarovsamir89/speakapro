package az.simplesoft.speakapro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import az.simplesoft.speakapro.ui.TranslatorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TranslatorScreen(
                isListening = false,
                microphoneLevel = 0f,
                frameCount = 0,
                error = null,
                onToggleListening = {},
            )
        }
    }
}
