package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.model.PaymentRecordEntity
import com.example.notification.BillAlarmScheduler
import com.example.notification.BillNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class BillAlarmReceiver : BroadcastReceiver() {

  companion object {
    const val ACTION_BILL_REMINDER = "com.aistudio.billreminder.ACTION_BILL_REMINDER"
    const val ACTION_MARK_PAID = "com.aistudio.billreminder.ACTION_MARK_PAID"

    const val EXTRA_BILL_ID = "extra_bill_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_AMOUNT = "extra_amount"
    const val EXTRA_CATEGORY = "extra_category"
    const val EXTRA_DUE_TEXT = "extra_due_text"
    const val EXTRA_YEAR = "extra_year"
    const val EXTRA_MONTH = "extra_month"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return

    when (intent.action) {
      ACTION_BILL_REMINDER -> {
        val billId = intent.getLongExtra(EXTRA_BILL_ID, -1L)
        if (billId == -1L) return

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Upcoming Bill"
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Bill"
        val dueText = intent.getStringExtra(EXTRA_DUE_TEXT) ?: "soon"
        val year = intent.getIntExtra(EXTRA_YEAR, LocalDate.now().year)
        val month = intent.getIntExtra(EXTRA_MONTH, LocalDate.now().monthValue)

        BillNotificationHelper.showBillReminderNotification(
          context = context,
          billId = billId,
          title = title,
          amount = amount,
          dueText = dueText,
          category = category,
          year = year,
          month = month,
        )

        // Reschedule next cycle
        CoroutineScope(Dispatchers.IO).launch {
          val db = AppDatabase.getDatabase(context)
          val bill = db.billDao().getBillById(billId)
          if (bill != null && !bill.isArchived) {
            BillAlarmScheduler.scheduleBillReminder(context, bill)
          }
        }
      }

      ACTION_MARK_PAID -> {
        val billId = intent.getLongExtra(EXTRA_BILL_ID, -1L)
        val year = intent.getIntExtra(EXTRA_YEAR, LocalDate.now().year)
        val month = intent.getIntExtra(EXTRA_MONTH, LocalDate.now().monthValue)
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)

        // Dismiss notification
        BillNotificationHelper.cancelNotification(context, billId)

        if (billId != -1L) {
          CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            db.paymentRecordDao().insertPayment(
              PaymentRecordEntity(
                billId = billId,
                year = year,
                month = month,
                paidDateEpochDay = LocalDate.now().toEpochDay(),
                paidAmount = amount,
                notes = "Marked as paid from notification",
              )
            )
          }
        }
      }
    }
  }
}
