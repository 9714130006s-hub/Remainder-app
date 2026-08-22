package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.notification.BillAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
      CoroutineScope(Dispatchers.IO).launch {
        val db = AppDatabase.getDatabase(context)
        val bills = db.billDao().getAllActiveBillsSync()
        BillAlarmScheduler.scheduleAll(context, bills)
      }
    }
  }
}
