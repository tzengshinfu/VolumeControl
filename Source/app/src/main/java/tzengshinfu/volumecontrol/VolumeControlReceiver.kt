package tzengshinfu.volumecontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class VolumeControlReceiver : BroadcastReceiver() {
    private lateinit var volumeControlSetting: VolumeControlSetting

    override fun onReceive(context: Context, intent: Intent) {
        volumeControlSetting = VolumeControlSetting(context)

        when (intent.action) {
            "upVolume" -> {
                volumeControlSetting.addVolume(1)
                volumeControlSetting.setNotificationBar(true)
            }

            "downVolume" -> {
                volumeControlSetting.addVolume(-1)
                volumeControlSetting.setNotificationBar(true)
            }

            "toggleVolume" -> {
                if (volumeControlSetting.isMuting()) {
                    volumeControlSetting.setMute(false)
                } else {
                    volumeControlSetting.setMute(true)
                }

                volumeControlSetting.setNotificationBar(true)
            }
        }
    }
}