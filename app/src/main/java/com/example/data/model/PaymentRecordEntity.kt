package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "payment_records",
  foreignKeys = [
    ForeignKey(
      entity = BillEntity::class,
      parentColumns = ["id"],
      childColumns = ["billId"],
      onDelete = ForeignKey.CASCADE,
    )
  ],
  indices = [Index(value = ["billId", "year", "month"], unique = true)],
)
data class PaymentRecordEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val billId: Long,
  val year: Int,
  val month: Int, // 1-12
  val paidDateEpochDay: Long,
  val paidAmount: Double,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)
