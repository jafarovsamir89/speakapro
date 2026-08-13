package az.simplesoft.speakapro.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper

data class AudioRouteState(
    val label: String,
    val headphonesConnected: Boolean,
)

class AudioRouter(context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)

    fun preferredInput(): AudioDeviceInfo? =
        audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS)
            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_MIC }

    fun preferredOutput(): AudioDeviceInfo? {
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).toList()
        val priority = listOf(
            AudioDeviceInfo.TYPE_BLE_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
        )
        return priority.firstNotNullOfOrNull { type -> outputs.firstOrNull { it.type == type } }
    }

    fun state(): AudioRouteState {
        val output = preferredOutput()
        return if (output == null) {
            AudioRouteState("Динамик телефона", false)
        } else {
            AudioRouteState(labelFor(output.type), true)
        }
    }

    fun register(onChanged: () -> Unit): AudioDeviceCallback {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) = onChanged()
            override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) = onChanged()
        }
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        return callback
    }

    fun unregister(callback: AudioDeviceCallback) {
        audioManager.unregisterAudioDeviceCallback(callback)
    }

    private fun labelFor(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "LE Audio наушники"
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth наушники"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Проводные наушники"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Проводная гарнитура"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "USB-наушники"
        AudioDeviceInfo.TYPE_USB_DEVICE -> "USB-аудио"
        else -> "Наушники"
    }
}
