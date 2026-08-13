package az.simplesoft.speakapro.live

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

object LiveProtocol {
    const val MODEL = "gemini-3.5-live-translate-preview"

    fun setup(targetLanguage: String): String {
        val config = JSONObject()
            .put("responseModalities", JSONArray().put("AUDIO"))
            .put("inputAudioTranscription", JSONObject())
            .put("outputAudioTranscription", JSONObject())
            .put("translationConfig", JSONObject()
                .put("targetLanguageCode", targetLanguage)
                .put("echoTargetLanguage", false))

        return JSONObject().put("setup", JSONObject()
            .put("model", "models/$MODEL")
            .put("generationConfig", config))
            .toString()
    }

    fun audio(pcm16le: ByteArray): String = JSONObject()
        .put("realtimeInput", JSONObject().put("audio", JSONObject()
            .put("data", Base64.encodeToString(pcm16le, Base64.NO_WRAP))
            .put("mimeType", "audio/pcm;rate=16000")))
        .toString()
}
