package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
  @Query("SELECT * FROM bills WHERE isArchived = 0 ORDER BY dueDayOfMonth ASC, dueDateEpochDay ASC")
  fun getAllActiveBills(): Flow<List<BillEntity>>

  @Query("SELECT * FROM bills ORDER BY dueDayOfMonth ASC")
  fun getAllBills(): Flow<List<BillEntity>>

  @Query("SELECT * FROM bills WHERE id = :id LIMIT 1")
  suspend fun getBillById(id: Long): BillEntity?

  @Query("SELECT * FROM bills WHERE isArchived = 0")
  suspend fun getAllActiveBillsSync(): List<BillEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBill(bill: BillEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBills(bills: List<BillEntity>): List<Long>

  @Update
  suspend fun updateBill(bill: BillEntity)

  @Delete
  suspend fun deleteBill(bill: BillEntity)

  @Query("DELETE FROM bills WHERE id = :id")
  suspend fun deleteBillById(id: Long)

  @Query("SELECT COUNT(*) FROM bills")
  suspend fun getCount(): Int
}
