package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String, // from BillCategory.name
  val dueDayOfMonth: Int, // 1 to 31
  val dueDateEpochDay: Long, // Target or next due date
  val frequency: String = "MONTHLY", // from Frequency.name
  val reminderDaysBefore: Int = 1, // 0 = on due date, 1, 2, 3, 7 days before
  val reminderTimeHour: Int = 9,
  val reminderTimeMinute: Int = 0,
  val isAutoPay: Boolean = false,
  val notes: String = "",
  val isArchived: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
)
