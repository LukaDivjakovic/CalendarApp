package com.example.rmaapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class EventNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EVENT_ID_EXTRA = "event_id_extra"
        const val EVENT_TITLE_EXTRA = "event_title_extra"
        const val NOTIFICATION_CHANNEL_ID = "event_channel"
        const val NOTIFICATION_CHANNEL_NAME = "Event Reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra(EVENT_ID_EXTRA, 0)
        val eventTitle = intent.getStringExtra(EVENT_TITLE_EXTRA) ?: "Event Starting"

        createNotificationChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("from_notification", true) // Add a marker
            putExtra(EVENT_ID_EXTRA, eventId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId, // Use a unique request code for each event
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_access_time_24)
            .setContentTitle("Event Starting Soon")
            .setContentText(eventTitle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(eventId, builder.build())
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming calendar events"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
