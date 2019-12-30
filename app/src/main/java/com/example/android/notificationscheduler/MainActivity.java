package com.example.android.notificationscheduler;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final int JOB_ID = 0;
    private JobScheduler mScheduler;

    // Switches for setting job options
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;

    // Member variable for override deadline Seek Bar
    private SeekBar mSeekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize job option switches
        mDeviceIdleSwitch = findViewById(R.id.idleSwitch);
        mDeviceChargingSwitch = findViewById(R.id.chargingSwitch);

        // Initialize seek bar and seek bar progress TextView
        mSeekBar = findViewById(R.id.seekBar);
        final TextView seekBarProgress = findViewById(R.id.seekBarProgress);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i > 0) {
                    seekBarProgress.setText(i + " s");
                } else {
                    seekBarProgress.setText("Not Set");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void scheduleJob(View view) {
        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        // For seek bar since it should only be set if int value is greater than 0
        int seekBarInteger = mSeekBar.getProgress();
        boolean seekBarSet = seekBarInteger > 0;

        RadioGroup networkOptions = findViewById(R.id.networkOptions);
        int selectedNetworkID = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
        switch(selectedNetworkID) {
            case R.id.noNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;
                break;
            case R.id.anyNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
                break;
            case R.id.wifiNetwork:
                selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
                break;
        }

        // Create JobInfo.Builder object
        ComponentName serviceName = new ComponentName(getPackageName(),
                NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());

        // For seek bar
        if (seekBarSet) {
            // use 1000 since parameter is in ms and we want s
            builder.setOverrideDeadline(seekBarInteger * 1000);
        }

        // Check constraints -- if at least one is set it will be true, and false if otherwise
        boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE) || mDeviceChargingSwitch.isChecked()
                || mDeviceIdleSwitch.isChecked() || seekBarSet;
        if(constraintSet) {
          // Schedule job and notify user
            // Call schedule() on JobScheduler object using build() to pass in JobInfo object
            JobInfo myJobInfo = builder.build();
            mScheduler.schedule(myJobInfo);
            // Toast letting user know job was scheduled
            Toast.makeText(this, "Job scheduled, job will run when " + "the constraints are met.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please set at least one constraint", Toast.LENGTH_SHORT).show();
        }


    }

    public void cancelJob(View view) {
        // Check whether JobScheduler object's null. If not, call cancelAll() to remove all pending
        // jobs. Reset to null and show toast telling user job was cancelled.
        if (mScheduler != null) {
            mScheduler.cancelAll();
            mScheduler = null;
            Toast.makeText(this, "Job(s) cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
