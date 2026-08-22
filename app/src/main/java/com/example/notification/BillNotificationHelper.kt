package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.BillAlarmReceiver
import java.util.Locale

object BillNotificationHelper {
  const val CHANNEL_ID = "bill_due_reminders_channel"
  const val CHANNEL_NAME = "Bill Due Reminders"
  const val CHANNEL_DESC = "Timely automated notifications before bill due dates"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        enableLights(true)
        setShowBadge(true)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun showBillReminderNotification(
    context: Context,
    billId: Long,
    title: String,
    amount: Double,
    dueText: String,
    category: String,
    year: Int,
    month: Int,
  ) {
    createNotificationChannel(context)

    val contentIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("SELECTED_BILL_ID", billId)
    }
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      billId.toInt(),
      contentIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Mark as paid action intent
    val markPaidIntent = Intent(context, BillAlarmReceiver::class.java).apply {
      action = BillAlarmReceiver.ACTION_MARK_PAID
      putExtra(BillAlarmReceiver.EXTRA_BILL_ID, billId)
      putExtra(BillAlarmReceiver.EXTRA_YEAR, year)
      putExtra(BillAlarmReceiver.EXTRA_MONTH, month)
      putExtra(BillAlarmReceiver.EXTRA_AMOUNT, amount)
    }
    val markPaidPendingIntent = PendingIntent.getBroadcast(
      context,
      (billId * 1000 + 1).toInt(),
      markPaidIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val formattedAmount = String.format(Locale.US, "$%,.2f", amount)
    val contentText = "$formattedAmount due $dueText ($category)"

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Bill Reminder: $title")
      .setContentText(contentText)
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("$title payment of $formattedAmount is due $dueText.\nTap to view details or mark as paid.")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setCategory(NotificationCompat.CATEGORY_REMINDER)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)
      .addAction(
        android.R.drawable.checkbox_on_background,
        "Mark as Paid",
        markPaidPendingIntent
      )

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(billId.toInt(), builder.build())
    } catch (e: SecurityException) {
      // Notification permission not granted yet
    }
  }

  fun sendTestNotification(context: Context) {
    createNotificationChannel(context)

    val contentIntent = Intent(context, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
      context,
      9999,
      contentIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Test Bill Alert: Electricity")
      .setContentText("$30.34 due in 3 days. Automated alerts are active!")
      .setStyle(
        NotificationCompat.BigTextStyle()
          .bigText("Automated reminders are working perfectly! You'll receive alerts before your monthly due dates.")
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(contentPendingIntent)

    try {
      val notificationManager = NotificationManagerCompat.from(context)
      notificationManager.notify(9999, builder.build())
    } catch (e: SecurityException) {
      // Permission not granted
    }
  }

  fun cancelNotification(context: Context, billId: Long) {
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.cancel(billId.toInt())
  }
}
