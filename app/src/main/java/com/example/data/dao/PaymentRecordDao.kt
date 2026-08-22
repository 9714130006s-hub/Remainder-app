package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {
  @Query("SELECT * FROM payment_records WHERE year = :year AND month = :month")
  fun getPaymentsForMonth(year: Int, month: Int): Flow<List<PaymentRecordEntity>>

  @Query("SELECT * FROM payment_records WHERE billId = :billId ORDER BY year DESC, month DESC")
  fun getPaymentsForBill(billId: Long): Flow<List<PaymentRecordEntity>>

  @Query("SELECT * FROM payment_records WHERE billId = :billId AND year = :year AND month = :month LIMIT 1")
  suspend fun getPayment(billId: Long, year: Int, month: Int): PaymentRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentRecordEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<PaymentRecordEntity>): List<Long>

  @Delete
  suspend fun deletePayment(payment: PaymentRecordEntity)

  @Query("DELETE FROM payment_records WHERE billId = :billId AND year = :year AND month = :month")
  suspend fun deletePaymentForMonth(billId: Long, year: Int, month: Int)

  @Query("DELETE FROM payment_records WHERE billId = :billId")
  suspend fun deleteAllPaymentsForBill(billId: Long)
}
