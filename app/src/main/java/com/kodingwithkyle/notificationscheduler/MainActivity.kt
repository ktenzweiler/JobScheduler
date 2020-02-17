package com.kodingwithkyle.notificationscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        const val JOB_ID = 0
    }

    private val mScheduler: JobScheduler by lazy {
        getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    }

    private lateinit var mIdleSwitch: Switch
    private lateinit var mChargingSwitch: Switch
    private lateinit var mSeekbar: SeekBar
    private lateinit var mSelectedTime : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mIdleSwitch = findViewById(R.id.idle)
        mChargingSwitch = findViewById(R.id.charging)
        mSeekbar = findViewById(R.id.seekbar)
        mSelectedTime = findViewById(R.id.selected_time)

        mSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mSelectedTime.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })
    }

    fun scheduleJobs(view: View) {
        val networkOptions = findViewById<RadioGroup>(R.id.network_options)
        val selectedNetworkOption = when (networkOptions.checkedRadioButtonId) {
            R.id.any -> JobInfo.NETWORK_TYPE_ANY
            R.id.no_network -> JobInfo.NETWORK_TYPE_NONE
            R.id.wifi -> JobInfo.NETWORK_TYPE_UNMETERED
            else -> JobInfo.NETWORK_TYPE_ANY
        }

        if (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE || mIdleSwitch.isChecked || mChargingSwitch.isChecked) {
            val serviceName = ComponentName(packageName, NotificationJobService::class.java.name)
            val builder = JobInfo.Builder(JOB_ID, serviceName)
            builder.setRequiresDeviceIdle(mIdleSwitch.isChecked)
            builder.setRequiresCharging(mChargingSwitch.isChecked)
            builder.setRequiredNetworkType(selectedNetworkOption)
            builder.setOverrideDeadline(mSelectedTime.text.toString().toLong() * 1000)
            val jobInfo = builder.build()
            mScheduler.schedule(jobInfo)
            Toast.makeText(
                this, "Job Scheduled, job will run when " +
                        "the constraints are met.", Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "select a constraint!", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancelJobs(view: View) {
        mScheduler.cancelAll()
        Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show();
    }
}
