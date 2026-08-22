package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.repository.BillRepository
import com.example.notification.BillNotificationHelper

class BillReminderApplication : Application() {
  val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
  val repository: BillRepository by lazy {
    BillRepository(this, database.billDao(), database.paymentRecordDao())
  }

  override fun onCreate() {
    super.onCreate()
    BillNotificationHelper.createNotificationChannel(this)
  }
}
