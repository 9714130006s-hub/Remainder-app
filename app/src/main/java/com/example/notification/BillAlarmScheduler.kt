package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.BillEntity
import com.example.receiver.BillAlarmReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object BillAlarmScheduler {

  fun scheduleBillReminder(context: Context, bill: BillEntity) {
    if (bill.isArchived) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

    val now = LocalDateTime.now()
    val today = LocalDate.now()

    // Determine the next upcoming target due date
    var targetYear = today.year
    var targetMonth = today.monthValue

    // Calculate due date in target month
    val daysInMonth = Month.of(targetMonth).length(LocalDate.of(targetYear, targetMonth, 1).isLeapYear)
    val dayOfMonth = bill.dueDayOfMonth.coerceIn(1, daysInMonth)
    var dueDate = LocalDate.of(targetYear, targetMonth, dayOfMonth)

    // Target reminder date & time
    var reminderDate = dueDate.minusDays(bill.reminderDaysBefore.toLong())
    var reminderDateTime = LocalDateTime.of(reminderDate, LocalTime.of(bill.reminderTimeHour, bill.reminderTimeMinute))

    // If reminder time has already passed for this month, advance to next month
    if (reminderDateTime.isBefore(now)) {
      val nextMonthDate = today.plusMonths(1)
      targetYear = nextMonthDate.year
      targetMonth = nextMonthDate.monthValue

      val nextDaysInMonth = Month.of(targetMonth).length(LocalDate.of(targetYear, targetMonth, 1).isLeapYear)
      val nextDayOfMonth = bill.dueDayOfMonth.coerceIn(1, nextDaysInMonth)
      dueDate = LocalDate.of(targetYear, targetMonth, nextDayOfMonth)

      reminderDate = dueDate.minusDays(bill.reminderDaysBefore.toLong())
      reminderDateTime = LocalDateTime.of(reminderDate, LocalTime.of(bill.reminderTimeHour, bill.reminderTimeMinute))
    }

    val triggerMillis = reminderDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val intent = Intent(context, BillAlarmReceiver::class.java).apply {
      action = BillAlarmReceiver.ACTION_BILL_REMINDER
      putExtra(BillAlarmReceiver.EXTRA_BILL_ID, bill.id)
      putExtra(BillAlarmReceiver.EXTRA_TITLE, bill.title)
      putExtra(BillAlarmReceiver.EXTRA_AMOUNT, bill.amount)
      putExtra(BillAlarmReceiver.EXTRA_CATEGORY, bill.category)
      putExtra(BillAlarmReceiver.EXTRA_YEAR, targetYear)
      putExtra(BillAlarmReceiver.EXTRA_MONTH, targetMonth)
      val daysUntilDue = ChronoUnit.DAYS.between(reminderDate, dueDate)
      val dueText = when (daysUntilDue) {
        0L -> "today"
        1L -> "tomorrow"
        else -> "in $daysUntilDue days"
      }
      putExtra(BillAlarmReceiver.EXTRA_DUE_TEXT, dueText)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      bill.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
      } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
      }
    } catch (e: SecurityException) {
      // If exact alarm permission is restricted on Android 12+, fallback to inexact
      alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
    }
  }

  fun cancelBillReminder(context: Context, billId: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, BillAlarmReceiver::class.java).apply {
      action = BillAlarmReceiver.ACTION_BILL_REMINDER
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      billId.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
  }

  fun scheduleAll(context: Context, bills: List<BillEntity>) {
    bills.filter { !it.isArchived }.forEach { scheduleBillReminder(context, it) }
  }
}
