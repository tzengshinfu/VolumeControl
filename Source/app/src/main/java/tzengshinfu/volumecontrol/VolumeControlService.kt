package tzengshinfu.volumecontrol

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.util.*
import kotlin.concurrent.fixedRateTimer


class VolumeControlService : Service() {
    //region 屬性
    private lateinit var volumeControlSetting: VolumeControlSetting
    private lateinit var timer_CheckSetting: Timer
    private val checkInterval: Long = 1000
    //endregion

    override fun onCreate() {
        super.onCreate()

        volumeControlSetting = VolumeControlSetting(this@VolumeControlService)

        setTimer_CheckSetting()
    }

    private fun setTimer_CheckSetting() {
        timer_CheckSetting = fixedRateTimer(initialDelay = 0, period = checkInterval) {
            if (volumeControlSetting.isEnabled()) {
                if (!volumeControlSetting.isShowingNotificationBar()) {
                    volumeControlSetting.setNotificationBar(true)
                }
            } else {
                volumeControlSetting.setNotificationBar(false)

                timer_CheckSetting.cancel()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        timer_CheckSetting.cancel()
        setTimer_CheckSetting()

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? = null
}