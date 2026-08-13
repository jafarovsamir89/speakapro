package az.simplesoft.speakapro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TranslatorScreen(
    isListening: Boolean,
    microphoneLevel: Float,
    frameCount: Long,
    error: String?,
    onToggleListening: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B0D12)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("SpeakAPro", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Синхронный перевод в наушники", color = Color(0xFF949AA8))
                Spacer(Modifier.height(32.dp))

                Text(
                    if (isListening) "● СЛУШАЮ" else "МИКРОФОН ГОТОВ",
                    color = if (isListening) Color(0xFF71E6A3) else Color(0xFFA8AFBD),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                Text("Уровень окружающей речи", color = Color.White)
                Spacer(Modifier.height(8.dp))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth(microphoneLevel.coerceIn(0.01f, 1f))
                        .height(12.dp)
                        .background(Color(0xFF71E6A3), RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(16.dp))
                Text("16 kHz · PCM mono · 100 ms · кадров: $frameCount", color = Color(0xFF949AA8))

                if (error != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(error, color = Color(0xFFFF8D8D))
                }

                Spacer(Modifier.weight(1f))
                Text(
                    if (isListening) "Говори рядом с телефоном — индикатор должен реагировать."
                    else "Сначала проверяем чистый аудиопоток, затем подключим Gemini Live.",
                    color = Color(0xFF949AA8),
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onToggleListening,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(if (isListening) "ОСТАНОВИТЬ" else "НАЧАТЬ ТЕСТ МИКРОФОНА")
                }
            }
        }
    }
}
