package az.simplesoft.speakapro.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.concurrent.thread
import kotlin.math.sqrt

data class AudioFrame(
    val pcm16le: ByteArray,
    val capturedAtNanos: Long,
    val level: Float,
)

class AudioRecorder {
    companion object {
        const val SAMPLE_RATE = 16_000
        const val CHUNK_SAMPLES = 1_600
        const val CHUNK_BYTES = 3_200
    }

    @Volatile
    private var running = false

    private var recorder: AudioRecord? = null
    private var worker: Thread? = null

    @SuppressLint("MissingPermission")
    fun start(onFrame: (AudioFrame) -> Unit, onError: (Throwable) -> Unit) {
        if (running) return

        val minBuffer = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        require(minBuffer > 0) { "Invalid audio buffer size" }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuffer, CHUNK_BYTES * 4),
        )
        require(audioRecord.state == AudioRecord.STATE_INITIALIZED) {
            audioRecord.release()
            "AudioRecord initialization failed"
        }

        recorder = audioRecord
        running = true
        audioRecord.startRecording()

        worker = thread(name = "SpeakAPro-AudioCapture", isDaemon = true) {
            val samples = ShortArray(CHUNK_SAMPLES)
            try {
                while (running) {
                    val count = audioRecord.read(
                        samples,
                        0,
                        samples.size,
                        AudioRecord.READ_BLOCKING,
                    )
                    if (count > 0) onFrame(makeFrame(samples, count))
                    if (count < 0) throw IllegalStateException("Audio read failed: $count")
                }
            } catch (t: Throwable) {
                if (running) onError(t)
            } finally {
                running = false
                release(audioRecord)
            }
        }
    }

    fun stop() {
        running = false
        val current = recorder
        try {
            current?.stop()
        } catch (_: IllegalStateException) {
        }
        if (worker !== Thread.currentThread()) worker?.join(500)
        release(current)
    }

    private fun release(audioRecord: AudioRecord?) {
        if (audioRecord == null || recorder !== audioRecord) return
        audioRecord.release()
        recorder = null
        worker = null
    }

    private fun makeFrame(samples: ShortArray, count: Int): AudioFrame {
        val pcm = ByteArray(count * 2)
        var energy = 0.0

        for (i in 0 until count) {
            val sample = samples[i].toInt()
            pcm[i * 2] = (sample and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
            energy += sample.toDouble() * sample.toDouble()
        }

        val rms = sqrt(energy / count)
        val level = ((rms / Short.MAX_VALUE) * 5.0).toFloat().coerceIn(0f, 1f)

        return AudioFrame(
            pcm16le = pcm,
            capturedAtNanos = System.nanoTime(),
            level = level,
        )
    }
}
