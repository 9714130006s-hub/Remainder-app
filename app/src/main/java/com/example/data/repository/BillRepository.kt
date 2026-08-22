package com.example.data.repository

import android.content.Context
import com.example.data.dao.BillDao
import com.example.data.dao.PaymentRecordDao
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.Frequency
import com.example.data.model.PaymentRecordEntity
import com.example.notification.BillAlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BillRepository(
  private val context: Context,
  private val billDao: BillDao,
  private val paymentRecordDao: PaymentRecordDao,
) {

  val activeBills: Flow<List<BillEntity>> = billDao.getAllActiveBills()

  fun getPaymentsForMonth(year: Int, month: Int): Flow<List<PaymentRecordEntity>> {
    return paymentRecordDao.getPaymentsForMonth(year, month)
  }

  suspend fun insertBill(bill: BillEntity): Long = withContext(Dispatchers.IO) {
    val id = billDao.insertBill(bill)
    val inserted = bill.copy(id = id)
    BillAlarmScheduler.scheduleBillReminder(context, inserted)
    id
  }

  suspend fun updateBill(bill: BillEntity) = withContext(Dispatchers.IO) {
    billDao.updateBill(bill)
    BillAlarmScheduler.scheduleBillReminder(context, bill)
  }

  suspend fun deleteBill(billId: Long) = withContext(Dispatchers.IO) {
    BillAlarmScheduler.cancelBillReminder(context, billId)
    paymentRecordDao.deleteAllPaymentsForBill(billId)
    billDao.deleteBillById(billId)
  }

  suspend fun markAsPaid(billId: Long, year: Int, month: Int, amount: Double, notes: String = "") =
    withContext(Dispatchers.IO) {
      paymentRecordDao.insertPayment(
        PaymentRecordEntity(
          billId = billId,
          year = year,
          month = month,
          paidDateEpochDay = LocalDate.now().toEpochDay(),
          paidAmount = amount,
          notes = notes,
        )
      )
    }

  suspend fun markAsUnpaid(billId: Long, year: Int, month: Int) = withContext(Dispatchers.IO) {
    paymentRecordDao.deletePaymentForMonth(billId, year, month)
  }

  suspend fun seedDefaultBillsIfEmpty() = withContext(Dispatchers.IO) {
    val count = billDao.getCount()
    if (count == 0) {
      val today = LocalDate.now()
      val currentYear = today.year
      val currentMonth = today.monthValue

      val sampleBills = listOf(
        BillEntity(
          title = "Streaming",
          amount = 151.79,
          category = BillCategory.STREAMING.name,
          dueDayOfMonth = 12,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 12.coerceAtMost(28)).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 1,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = true,
          notes = "Netflix, HBO Max & Disney+ bundle"
        ),
        BillEntity(
          title = "Water",
          amount = 47.69,
          category = BillCategory.WATER.name,
          dueDayOfMonth = 21,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 21.coerceAtMost(28)).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 3,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = false,
          notes = "Municipal water utility"
        ),
        BillEntity(
          title = "Electricity",
          amount = 30.34,
          category = BillCategory.ELECTRICITY.name,
          dueDayOfMonth = 30,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 28).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 2,
          reminderTimeHour = 10,
          reminderTimeMinute = 0,
          isAutoPay = false,
          notes = "Clean energy grid power"
        ),
        BillEntity(
          title = "Mortgage",
          amount = 1762.00,
          category = BillCategory.MORTGAGE.name,
          dueDayOfMonth = 17,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 17.coerceAtMost(28)).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 3,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = true,
          notes = "First National Bank loan"
        ),
        BillEntity(
          title = "Music",
          amount = 307.94,
          category = BillCategory.MUSIC.name,
          dueDayOfMonth = 2,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 2).toEpochDay(),
          frequency = Frequency.YEARLY.name,
          reminderDaysBefore = 7,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = false,
          notes = "Annual family music subscription"
        ),
        BillEntity(
          title = "Internet",
          amount = 65.04,
          category = BillCategory.INTERNET.name,
          dueDayOfMonth = 1,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 1).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 1,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = true,
          notes = "Fiber Gigabit 1000Mbps"
        ),
        BillEntity(
          title = "Gym",
          amount = 134.44,
          category = BillCategory.GYM.name,
          dueDayOfMonth = 6,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 6).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 1,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = true,
          notes = "Fitness center & pool pass"
        ),
        BillEntity(
          title = "Rent",
          amount = 800.00,
          category = BillCategory.RENT.name,
          dueDayOfMonth = 1,
          dueDateEpochDay = LocalDate.of(currentYear, currentMonth, 1).toEpochDay(),
          frequency = Frequency.MONTHLY.name,
          reminderDaysBefore = 2,
          reminderTimeHour = 9,
          reminderTimeMinute = 0,
          isAutoPay = false,
          notes = "Apartment monthly lease"
        )
      )

      val ids = billDao.insertBills(sampleBills)

      // Pre-mark 3 bills as paid this period (matching the screenshot: Internet, Gym, Rent)
      if (ids.size >= 8) {
        val internetId = ids[5]
        val gymId = ids[6]
        val rentId = ids[7]

        paymentRecordDao.insertPayments(
          listOf(
            PaymentRecordEntity(
              billId = internetId,
              year = currentYear,
              month = currentMonth,
              paidDateEpochDay = today.minusDays(5).toEpochDay(),
              paidAmount = 65.04,
              notes = "Paid online"
            ),
            PaymentRecordEntity(
              billId = gymId,
              year = currentYear,
              month = currentMonth,
              paidDateEpochDay = today.minusDays(2).toEpochDay(),
              paidAmount = 134.44,
              notes = "Auto-debited"
            ),
            PaymentRecordEntity(
              billId = rentId,
              year = currentYear,
              month = currentMonth,
              paidDateEpochDay = today.minusDays(8).toEpochDay(),
              paidAmount = 800.00,
              notes = "Bank transfer"
            )
          )
        )
      }

      // Schedule alarms for the inserted bills
      val active = billDao.getAllActiveBillsSync()
      BillAlarmScheduler.scheduleAll(context, active)
    }
  }
}
