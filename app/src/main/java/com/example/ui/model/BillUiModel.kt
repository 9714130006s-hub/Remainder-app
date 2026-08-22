package com.example.ui.model

import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.Frequency
import com.example.data.model.PaymentRecordEntity
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

enum class BillStatus {
  PAID,
  OVERDUE,
  DUE_TODAY,
  DUE_SOON, // within next 3 days
  UPCOMING
}

data class BillUiModel(
  val bill: BillEntity,
  val payment: PaymentRecordEntity?,
  val targetDueDate: LocalDate,
  val isPaid: Boolean,
  val status: BillStatus,
  val formattedAmount: String,
  val category: BillCategory,
  val frequency: Frequency,
  val countdownText: String,
  val formattedDueDate: String,
) {
  companion object {
    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    private val fullDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())

    fun from(
      bill: BillEntity,
      payment: PaymentRecordEntity?,
      selectedYear: Int,
      selectedMonth: Int,
      today: LocalDate = LocalDate.now(),
    ): BillUiModel {
      val isPaid = payment != null
      val category = BillCategory.fromString(bill.category)
      val freq = Frequency.fromString(bill.frequency)

      // Compute actual due date for the selected period
      val daysInTargetMonth = Month.of(selectedMonth).length(LocalDate.of(selectedYear, selectedMonth, 1).isLeapYear)
      val actualDay = bill.dueDayOfMonth.coerceIn(1, daysInTargetMonth)
      val calculatedDueDate = LocalDate.of(selectedYear, selectedMonth, actualDay)

      // Formatted amount (e.g. $151.79 or $1,762.00)
      val formattedAmount = String.format(Locale.US, "$%,.2f", bill.amount)
      val formattedDueDate = calculatedDueDate.format(dateFormatter)

      val daysBetween = ChronoUnit.DAYS.between(today, calculatedDueDate)

      val status = when {
        isPaid -> BillStatus.PAID
        daysBetween < 0 -> BillStatus.OVERDUE
        daysBetween == 0L -> BillStatus.DUE_TODAY
        daysBetween in 1..3 -> BillStatus.DUE_SOON
        else -> BillStatus.UPCOMING
      }

      val countdownText = when {
        isPaid -> {
          val paidDate = payment?.let { LocalDate.ofEpochDay(it.paidDateEpochDay) }
          if (paidDate != null) {
            "Paid on ${paidDate.format(dateFormatter)}"
          } else {
            "Paid (${Month.of(selectedMonth).name.lowercase().replaceFirstChar { it.uppercase() }})"
          }
        }
        daysBetween < 0 -> {
          val overdueDays = -daysBetween
          if (overdueDays == 1L) "Overdue by 1 day · $formattedDueDate"
          else "Overdue by $overdueDays days · $formattedDueDate"
        }
        daysBetween == 0L -> "Due today · $formattedDueDate"
        daysBetween == 1L -> "Due tomorrow · $formattedDueDate"
        else -> "Due in $daysBetween days · $formattedDueDate"
      }

      return BillUiModel(
        bill = bill,
        payment = payment,
        targetDueDate = calculatedDueDate,
        isPaid = isPaid,
        status = status,
        formattedAmount = formattedAmount,
        category = category,
        frequency = freq,
        countdownText = countdownText,
        formattedDueDate = formattedDueDate,
      )
    }
  }
}
