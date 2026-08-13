package az.simplesoft.speakapro.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
        Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0D12)) {
            Column(Modifier.padding(24.dp)) {
                Text("SpeakAPro", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Синхронный перевод → Русский", color = Color.Gray)
                Spacer(Modifier.height(30.dp))
                Text(
                    if (isListening) "● ПЕРЕВОД ВКЛЮЧЕН" else "ГОТОВ К ПЕРЕВОДУ",
                    color = if (isListening) Color(0xFF71E6A3) else Color.LightGray,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(14.dp))
                Text("Микрофон ${(microphoneLevel * 100).toInt()}%", color = Color.White)
                Text("16 kHz · 100 ms · кадров: $frameCount", color = Color.Gray, fontSize = 12.sp)
                if (error != null) {
                    Spacer(Modifier.height(18.dp))
                    Text(error, color = Color(0xFFFF8D8D))
                }
                Spacer(Modifier.weight(1f))
                Text("Телефон слушает окружающую речь, перевод звучит через аудиовыход Android.", color = Color.Gray)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onToggleListening, modifier = Modifier.fillMaxWidth().height(58.dp)) {
                    Text(if (isListening) "ОСТАНОВИТЬ" else "НАЧАТЬ ПЕРЕВОД")
                }
            }
        }
    }
}
