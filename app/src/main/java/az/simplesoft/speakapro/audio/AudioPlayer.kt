package az.simplesoft.speakapro.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

class AudioPlayer {
    companion object {
        const val SAMPLE_RATE = 24_000
    }

    private var track: AudioTrack? = null

    fun start() {
        if (track != null) return

        val minBuffer = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        require(minBuffer > 0) { "Invalid AudioTrack buffer size" }

        val newTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build(),
            maxOf(minBuffer, SAMPLE_RATE),
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE,
        )

        require(newTrack.state == AudioTrack.STATE_INITIALIZED) {
            newTrack.release()
            "AudioTrack initialization failed"
        }

        newTrack.play()
        track = newTrack
    }

    fun write(pcm16le: ByteArray): Int {
        val current = track ?: return 0
        if (pcm16le.isEmpty()) return 0
        return current.write(pcm16le, 0, pcm16le.size, AudioTrack.WRITE_BLOCKING)
    }

    fun stop() {
        val current = track ?: return
        track = null
        try {
            current.pause()
            current.flush()
            current.stop()
        } catch (_: IllegalStateException) {
        }
        current.release()
    }
}
