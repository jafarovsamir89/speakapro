package az.simplesoft.speakapro.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LanguageOption(
    val code: String,
    val label: String,
    val flag: String,
)

private val Background = Color(0xFF080A0F)
private val SurfaceCard = Color(0xFF131720)
private val SurfaceSoft = Color(0xFF1A1F29)
private val TextPrimary = Color(0xFFF7F8FB)
private val TextSecondary = Color(0xFF8E96A6)
private val AccentPurple = Color(0xFF7657FF)
private val AccentBlue = Color(0xFF19A7FF)
private val Success = Color(0xFF42D68A)
private val Error = Color(0xFFFF7474)

@Composable
fun TranslatorScreen(
    isListening: Boolean,
    statusText: String,
    microphoneLevel: Float,
    frameCount: Long,
    inputText: String,
    outputText: String,
    selectedLanguage: LanguageOption,
    languages: List<LanguageOption>,
    outputDeviceLabel: String,
    headphonesConnected: Boolean,
    error: String?,
    onLanguageSelected: (LanguageOption) -> Unit,
    onToggleListening: () -> Unit,
) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 20.dp),
            ) {
                Header()
                Spacer(Modifier.height(20.dp))

                LanguageSelector(
                    selected = selectedLanguage,
                    languages = languages,
                    enabled = !isListening,
                    onSelected = onLanguageSelected,
                )

                Spacer(Modifier.height(22.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ListeningOrb(level = microphoneLevel, active = isListening)
                }
                Spacer(Modifier.height(12.dp))

                Text(
                    text = statusText,
                    color = if (isListening) TextPrimary else TextSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(7.dp))

                AudioRouteChip(
                    label = outputDeviceLabel,
                    connected = headphonesConnected,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )

                Spacer(Modifier.height(22.dp))
                TranscriptCard(
                    title = "ОРИГИНАЛ",
                    text = inputText.ifBlank { "Речь собеседника появится здесь…" },
                    highlighted = false,
                )
                Spacer(Modifier.height(12.dp))
                TranscriptCard(
                    title = "ПЕРЕВОД · ${selectedLanguage.label.uppercase()}",
                    text = outputText.ifBlank { "Перевод появится здесь…" },
                    highlighted = true,
                )

                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = error,
                        color = Error,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Error.copy(alpha = 0.09f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                    )
                }

                Spacer(Modifier.weight(1f))
                Text(
                    text = "16 kHz вход  •  Gemini Live  •  24 kHz перевод  •  $frameCount кадров",
                    color = TextSecondary.copy(alpha = 0.72f),
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(12.dp))
                GradientActionButton(
                    active = isListening,
                    onClick = onToggleListening,
                )
            }
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("SpeakAPro", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Синхронный перевод в наушники", color = TextSecondary, fontSize = 13.sp)
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(SurfaceSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("⚙", fontSize = 17.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun LanguageSelector(
    selected: LanguageOption,
    languages: List<LanguageOption>,
    enabled: Boolean,
    onSelected: (LanguageOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text("ПЕРЕВОДИТЬ НА", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(7.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(16.dp))
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selected.flag, fontSize = 20.sp)
                Spacer(Modifier.size(10.dp))
                Text(selected.label, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Text(if (enabled) "⌄" else "●", color = TextSecondary)
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = { Text("${language.flag}  ${language.label}") },
                        onClick = {
                            expanded = false
                            onSelected(language)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ListeningOrb(level: Float, active: Boolean) {
    val strength = level.coerceIn(0f, 1f)
    Canvas(modifier = Modifier.size(188.dp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.39f
        drawCircle(
            color = AccentPurple.copy(alpha = if (active) 0.16f else 0.07f),
            radius = radius * 1.25f,
            center = center,
        )
        drawCircle(
            brush = Brush.sweepGradient(listOf(AccentPurple, AccentBlue, AccentPurple)),
            radius = radius,
            center = center,
            style = Stroke(width = 8.dp.toPx()),
        )
        drawCircle(color = SurfaceCard, radius = radius - 12.dp.toPx(), center = center)

        val bars = 7
        val spacing = 7.dp.toPx()
        val barWidth = 4.dp.toPx()
        for (i in 0 until bars) {
            val distance = kotlin.math.abs(i - bars / 2).toFloat()
            val base = 16.dp.toPx() + (bars / 2f - distance) * 7.dp.toPx()
            val reactive = if (active) strength * 34.dp.toPx() else 0f
            val height = base + reactive * (1f - distance / bars)
            val x = center.x + (i - bars / 2) * spacing
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(AccentBlue, AccentPurple)),
                topLeft = Offset(x - barWidth / 2, center.y - height / 2),
                size = androidx.compose.ui.geometry.Size(barWidth, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2),
            )
        }
    }
}

@Composable
private fun AudioRouteChip(label: String, connected: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(
                if (connected) Success.copy(alpha = 0.10f) else SurfaceSoft,
                RoundedCornerShape(30.dp),
            )
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (connected) "🎧" else "🔊", fontSize = 14.sp)
        Spacer(Modifier.size(7.dp))
        Text(label, color = if (connected) Success else TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TranscriptCard(title: String, text: String, highlighted: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(18.dp))
            .then(
                if (highlighted) {
                    Modifier.background(
                        Brush.linearGradient(
                            listOf(AccentPurple.copy(alpha = 0.10f), AccentBlue.copy(alpha = 0.04f)),
                        ),
                        RoundedCornerShape(18.dp),
                    )
                } else Modifier
            )
            .padding(16.dp),
    ) {
        Text(
            title,
            color = if (highlighted) AccentPurple.copy(alpha = 0.95f) else TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = text,
            color = if (highlighted) TextPrimary else TextPrimary.copy(alpha = 0.82f),
            fontSize = if (highlighted) 19.sp else 16.sp,
            lineHeight = if (highlighted) 25.sp else 22.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun GradientActionButton(active: Boolean, onClick: () -> Unit) {
    val brush = if (active) {
        Brush.linearGradient(listOf(Color(0xFF292E39), Color(0xFF20242D)))
    } else {
        Brush.linearGradient(listOf(AccentPurple, AccentBlue))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(brush, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (active) "■   ОСТАНОВИТЬ" else "🎧   НАЧАТЬ ПЕРЕВОД",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
