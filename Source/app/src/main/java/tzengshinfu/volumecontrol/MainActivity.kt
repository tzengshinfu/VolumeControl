package tzengshinfu.volumecontrol

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.*
import kotlin.concurrent.fixedRateTimer


class MainActivity : AppCompatActivity() {
    private lateinit var volumeControlSetting: VolumeControlSetting
    private val doNotDisturbPermissionRequestCode = 1234
    private lateinit var timer_CheckSetting: Timer
    private val checkInterval: Long = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        volumeControlSetting = VolumeControlSetting(this@MainActivity)

        requestDoNotDisturbPermission(R.string.claimPermission)

        setTimer_CheckSetting()

        checkBox_Enable.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                volumeControlSetting.saveEnabled(checkBox_Enable.isChecked)

                if (volumeControlSetting.isEnabled()) {
                    volumeControlSetting.startVolumeControlService()
                }
            }
        })

        button_ToggleVolume.setOnClickListener {
            if (volumeControlSetting.isMuting()) {
                volumeControlSetting.setMute(false)

            } else {
                volumeControlSetting.setMute(true)
            }

            button_ToggleVolume.text = volumeControlSetting.getToggleVolumeText()

            volumeControlSetting.setNotificationBar(true)
        }

        button_UpVolume.setOnClickListener {
            volumeControlSetting.addVolume(1)
            textView_VolumePercent.text = volumeControlSetting.getCurrentVolumePercentText()

            volumeControlSetting.setNotificationBar(true)
        }

        button_DownVolume.setOnClickListener {
            volumeControlSetting.addVolume(-1)
            textView_VolumePercent.text = volumeControlSetting.getCurrentVolumePercentText()

            volumeControlSetting.setNotificationBar(true)
        }
    }

    private fun updateUI() {
        checkBox_Enable.isChecked = volumeControlSetting.isEnabled()
        progressBar_Volume.progress = volumeControlSetting.getCurrentVolume()
        progressBar_Volume.max = volumeControlSetting.getMaxVolume()
        textView_VolumePercent.text = volumeControlSetting.getCurrentVolumePercentText()
        button_ToggleVolume.text = volumeControlSetting.getToggleVolumeText()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == doNotDisturbPermissionRequestCode) {
            requestDoNotDisturbPermission(R.string.claimPermissionAgain)
        }
    }

    private fun requestDoNotDisturbPermission(claimPermissionTextId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!volumeControlSetting.getNotificationManager().isNotificationPolicyAccessGranted) {
                toast(claimPermissionTextId)

                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivityForResult(intent, doNotDisturbPermissionRequestCode)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        setTimer_CheckSetting()
    }

    override fun onPause() {
        super.onPause()

        timer_CheckSetting.cancel()
    }

    private fun setTimer_CheckSetting() {
        timer_CheckSetting = fixedRateTimer(initialDelay = 0, period = checkInterval) {
            runOnUiThread({
                updateUI()
            })
        }
    }
}