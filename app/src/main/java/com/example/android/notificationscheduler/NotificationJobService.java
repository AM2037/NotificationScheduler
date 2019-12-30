package com.example.android.notificationscheduler;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.core.app.NotificationCompat;

public class NotificationJobService extends JobService {

    // Member variable for NotificationManager
    NotificationManager mNotifyManager;

    // Notification channel ID
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    /**
     * Called to indicate that the job has begun executing.  Override this method with the
     * logic for your job.  Like all other component lifecycle callbacks, this method executes
     * on your application's main thread.
     *
     * @param jobParameters Parameters specifying info about this job, including the optional
     *               extras configured with {@link JobInfo.Builder#setExtras(PersistableBundle).
     *               This object serves to identify this specific running job instance when calling
     *               {@link #jobFinished(JobParameters, boolean)}.
     * @return {@code true} if your service will continue running, using a separate thread
     * when appropriate.  {@code false} means that this job has completed its work.
     */
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        // Create notification channel
        createNotificationChannel();

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        // Deliver notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Job Service")
                .setContentText("Your Job ran to completion!")
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_job_running)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
        mNotifyManager.notify(0, builder.build());

        // Returns false because for this app all the work's completed in
        // the onStartJob() callback.
        return false;
    }

    /**
     * Creates a Notification channel, for OREO and higher.
     */
    public void createNotificationChannel(){
        // Define notification manager object
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Check SDK version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create Notification channel with all parameters
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "Job Service Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notifications from Job Service");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }
    /**
     * This method is called if the system has determined that you must stop execution of your job
     * even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
     *
     * @param params The parameters identifying this job, as supplied to
     *               the job in the {@link #onStartJob(JobParameters)} callback.
     * @return {@code true} to indicate to the JobManager whether you'd like to reschedule
     * this job based on the retry criteria provided at job creation-time; or {@code false}
     * to end the job entirely.  Regardless of the value returned, your job must stop executing.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        // Returns true because if the job fails you want it rescheduled instead
        // of dropped.
        return true;
    }
}
