package com.parita.chatapplication.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.parita.chatapplication.R
import java.util.*

class MessageListActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager

    companion object {
        private val NOTIFICATION_ID = 0
        private val PRIMARY_CHANNEL_ID = "primary_notification_channel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        navController = Navigation.findNavController(this, R.id.nav_fragment)
        initView()
    }

    private fun initView() {

    }

    fun createToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        val view = toast.view
        view?.setBackgroundResource(R.drawable.tags_rounded_corners)
        view?.setPadding(16, 16, 16, 16)
        val text = view?.findViewById<View>(android.R.id.message) as TextView
        text.setTextColor(ContextCompat.getColor(context, R.color.black))
        toast.show()
    }

    private fun scheduleNotification() {
        val notificationIntent = Intent(
            this, NotificationPublisher::class.java
        )
        pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_HOUR * 3,
            pendingIntent
        ) // 3 hours
        createNotificationChannel()
    }

    fun createNotificationChannel() {
        val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                "Lets Chat",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
    }

    class NotificationPublisher : BroadcastReceiver() {
        lateinit var notificationManager: NotificationManager

        override fun onReceive(context: Context, intent: Intent) {
            notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
                deliverNotification(context)
            } else {
                Log.d("TAG", "Running the service")
                deliverNotification(context)
            }
        }

        private fun deliverNotification(context: Context) {
            val contentIntent = Intent(context, MessageListActivity::class.java)
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_ID,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_alarm)
                    .setContentTitle(context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_text))
                    .setContentIntent(contentPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        }
    }

}