package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BillDao
import com.example.data.dao.PaymentRecordDao
import com.example.data.model.BillEntity
import com.example.data.model.PaymentRecordEntity

@Database(
  entities = [BillEntity::class, PaymentRecordEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun billDao(): BillDao
  abstract fun paymentRecordDao(): PaymentRecordDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "bill_reminder_db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
