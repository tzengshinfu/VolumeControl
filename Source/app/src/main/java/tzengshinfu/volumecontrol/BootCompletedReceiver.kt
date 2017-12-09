package tzengshinfu.volumecontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BootCompletedReceiver : BroadcastReceiver() {
    private lateinit var volumeControlSetting: VolumeControlSetting

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                volumeControlSetting = VolumeControlSetting(context)

                volumeControlSetting.saveShowingNotificationBar(false)
                volumeControlSetting.startVolumeControlService()
            }
        }
    }
}