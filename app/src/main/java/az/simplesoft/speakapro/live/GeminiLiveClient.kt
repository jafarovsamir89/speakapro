package az.simplesoft.speakapro.live

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class GeminiLiveClient(
    apiKey: String,
    private val targetLanguage: String,
    private val events: Events,
) : WebSocketListener() {
    interface Events {
        fun ready()
        fun audio(bytes: ByteArray)
        fun inputText(text: String)
        fun outputText(text: String)
        fun error(message: String)
        fun closed()
    }

    private val client = OkHttpClient()
    private val request = Request.Builder()
        .url("wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey")
        .build()
    private var socket: WebSocket? = null

    fun connect() {
        socket = client.newWebSocket(request, this)
    }

    fun sendAudio(bytes: ByteArray): Boolean = socket?.send(LiveProtocol.audio(bytes)) == true

    fun close() {
        socket?.close(1000, "stop")
        socket = null
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send(LiveProtocol.setup(targetLanguage))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        val root = JSONObject(text)
        if (root.has("setupComplete")) events.ready()
        val content = root.optJSONObject("serverContent") ?: return
        content.optJSONObject("inputTranscription")?.optString("text")
            ?.takeIf(String::isNotBlank)?.let(events::inputText)
        content.optJSONObject("outputTranscription")?.optString("text")
            ?.takeIf(String::isNotBlank)?.let(events::outputText)

        val parts = content.optJSONObject("modelTurn")?.optJSONArray("parts") ?: return
        for (i in 0 until parts.length()) {
            val data = parts.optJSONObject(i)?.optJSONObject("inlineData")?.optString("data") ?: continue
            if (data.isNotBlank()) events.audio(Base64.decode(data, Base64.DEFAULT))
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        events.error(t.message ?: "Gemini Live error")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        events.closed()
    }
}
