package tzengshinfu.volumecontrol

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.support.v7.app.NotificationCompat
import android.widget.RemoteViews
import kotlin.math.roundToInt


class VolumeControlSetting(context: Context) {
    //region 屬性
    private val notifyId = 0

    private var appSettings: SharedPreferences
    private var application: Context = context
    private var audioManager: AudioManager
    private var ringAddVolumeWeight: Double
    private var musicAddVolumeWeight: Double
    private var ringMaxVolume: Int
    private var musicMaxVolume: Int
    private var volumeControlReceiver: VolumeControlReceiver
    private var notificationManager: NotificationManager
    private var viewVolume: RemoteViews
    //endregion

    init {
        appSettings = application.getSharedPreferences(context.packageName, 0)
        audioManager = application.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        ringAddVolumeWeight = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).toDouble() / audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION).toDouble()
        musicAddVolumeWeight = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toDouble() / audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION).toDouble()
        ringMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
        musicMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeControlReceiver = VolumeControlReceiver()
        notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        viewVolume = RemoteViews(application.packageName, R.layout.view_volume)
    }

    fun startVolumeControlService() {
        val volumeControlService = Intent()
        volumeControlService.setClass(application, VolumeControlService::class.java)
        application.startService(volumeControlService)
    }

    fun setNotificationBar(isVolumeControlActived: Boolean) {
        if (isVolumeControlActived) {
            val notification = NotificationCompat.Builder(application)
                    .setSmallIcon(R.drawable.ic_stat_notify)
                    .setContent(viewVolume)
                    .setPriority(Notification.PRIORITY_MAX)
                    .build()
            notification.flags = Notification.FLAG_ONGOING_EVENT

            viewVolume.setProgressBar(R.id.proressBar_Volume, getMaxVolume(), getCurrentVolume(), false)
            viewVolume.setOnClickPendingIntent(R.id.button_UpVolume, PendingIntent.getBroadcast(application, 0, Intent("upVolume"), FLAG_UPDATE_CURRENT))
            viewVolume.setOnClickPendingIntent(R.id.button_DownVolume, PendingIntent.getBroadcast(application, 0, Intent("downVolume"), FLAG_UPDATE_CURRENT))
            viewVolume.setOnClickPendingIntent(R.id.button_ToggleVolume, PendingIntent.getBroadcast(application, 0, Intent("toggleVolume"), FLAG_UPDATE_CURRENT))
            viewVolume.setTextViewText(R.id.button_ToggleVolume, getToggleVolumeText())

            notificationManager.notify(notifyId, notification)

            saveShowingNotificationBar(true)
        } else {
            notificationManager.cancel(notifyId)

            saveShowingNotificationBar(false)
        }
    }

    fun isMuting(): Boolean = (getCurrentVolume() == 0)

    fun getToggleVolumeText(): String = if (isMuting()) application.getString(R.string.mute) else application.getString(R.string.unmute)

    fun isEnabled(): Boolean = appSettings.getBoolean("IsEnabled", false)

    fun saveEnabled(isEnabled: Boolean) {
        val appSettingsEditor = appSettings.edit()
        appSettingsEditor.putBoolean("IsEnabled", isEnabled)
        appSettingsEditor.commit()
    }

    fun getCurrentVolume(): Int = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

    fun getMaxVolume(): Int = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)

    fun getCurrentVolumePercentText(): String = ((getCurrentVolume().toDouble() / getMaxVolume().toDouble()) * 100).roundToInt().toString()

    fun addVolume(volumeDirection: Int) {
        when (volumeDirection) {
            1 -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_RAISE, 0)
                keepAbreastVolume()
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)

                if (getCurrentVolume() == getMaxVolume()) {
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, ringMaxVolume, 0)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, musicMaxVolume, 0)
                }
            }

            -1 -> {
                if (getCurrentVolume() != 0) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_LOWER, 0)
                    keepAbreastVolume()

                    if (getCurrentVolume() == 0) {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0)
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    }
                }
            }

            else -> throw Exception(application.getString(R.string.please_input_one_or_minus_one))
        }
    }

    /**
     * 將鈴聲及音樂音量與通知音量對齊
     */
    private fun keepAbreastVolume() {
        audioManager.setStreamVolume(AudioManager.STREAM_RING, (getCurrentVolume() * ringAddVolumeWeight).roundToInt(), 0)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (getCurrentVolume() * musicAddVolumeWeight).roundToInt(), 0)
    }

    fun isShowingNotificationBar(): Boolean = appSettings.getBoolean("IsShowingNotificationBar", false)

    fun saveShowingNotificationBar(isShowingNotificationBar: Boolean) {
        val appSettingsEditor = appSettings.edit()
        appSettingsEditor.putBoolean("IsShowingNotificationBar", isShowingNotificationBar)
        appSettingsEditor.commit()
    }

    fun setMute(isMute: Boolean) {
        if (isMute) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
        } else {
            audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL)
        }
    }

    fun getNotificationManager(): NotificationManager {
        return notificationManager
    }
}