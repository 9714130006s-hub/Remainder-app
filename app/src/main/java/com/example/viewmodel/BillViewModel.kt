package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BillReminderApplication
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.PaymentRecordEntity
import com.example.data.repository.BillRepository
import com.example.notification.BillNotificationHelper
import com.example.ui.model.BillStatus
import com.example.ui.model.BillUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class BillsUiState(
  val yearMonth: YearMonth = YearMonth.now(),
  val formattedMonth: String = "",
  val totalCount: Int = 0,
  val paidCount: Int = 0,
  val remainingCount: Int = 0,
  val totalAmount: Double = 0.0,
  val paidAmount: Double = 0.0,
  val remainingAmount: Double = 0.0,
  val upcomingBills: List<BillUiModel> = emptyList(),
  val paidBills: List<BillUiModel> = emptyList(),
  val overdueBills: List<BillUiModel> = emptyList(),
  val dueThisWeekMessage: String = "No bills due this week",
  val selectedCategoryFilter: BillCategory? = null,
  val isLoading: Boolean = false,
)

class BillViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BillRepository = (application as BillReminderApplication).repository

  private val _selectedYearMonth = MutableStateFlow(YearMonth.now())
  val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

  private val _selectedCategoryFilter = MutableStateFlow<BillCategory?>(null)
  val selectedCategoryFilter: StateFlow<BillCategory?> = _selectedCategoryFilter.asStateFlow()

  private val _editingBill = MutableStateFlow<BillEntity?>(null)
  val editingBill: StateFlow<BillEntity?> = _editingBill.asStateFlow()

  private val _isAddEditDialogOpen = MutableStateFlow(false)
  val isAddEditDialogOpen: StateFlow<Boolean> = _isAddEditDialogOpen.asStateFlow()

  private val _viewingBillDetail = MutableStateFlow<BillUiModel?>(null)
  val viewingBillDetail: StateFlow<BillUiModel?> = _viewingBillDetail.asStateFlow()

  private val _isCalendarViewOpen = MutableStateFlow(false)
  val isCalendarViewOpen: StateFlow<Boolean> = _isCalendarViewOpen.asStateFlow()

  private val _isSettingsOpen = MutableStateFlow(false)
  val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

  init {
    viewModelScope.launch {
      repository.seedDefaultBillsIfEmpty()
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val uiState: StateFlow<BillsUiState> = combine(
    _selectedYearMonth,
    _selectedCategoryFilter,
    _selectedYearMonth.flatMapLatest { ym ->
      combine(
        repository.activeBills,
        repository.getPaymentsForMonth(ym.year, ym.monthValue)
      ) { bills, payments -> Pair(bills, payments) }
    }
  ) { ym, categoryFilter, (bills, payments) ->
    val today = LocalDate.now()
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val formattedMonth = ym.format(monthFormatter)

    val paymentMap = payments.associateBy { it.billId }

    val allUiModels = bills.map { bill ->
      val payment = paymentMap[bill.id]
      BillUiModel.from(
        bill = bill,
        payment = payment,
        selectedYear = ym.year,
        selectedMonth = ym.monthValue,
        today = today
      )
    }

    val filtered = if (categoryFilter != null) {
      allUiModels.filter { it.category == categoryFilter }
    } else {
      allUiModels
    }

    val totalCount = filtered.size
    val paidBills = filtered.filter { it.isPaid }.sortedByDescending { it.payment?.paidDateEpochDay ?: 0L }
    val unpaidBills = filtered.filter { !it.isPaid }

    val overdueBills = unpaidBills.filter { it.status == BillStatus.OVERDUE }.sortedBy { it.targetDueDate }
    val upcomingBills = unpaidBills.filter { it.status != BillStatus.OVERDUE }.sortedBy { it.targetDueDate }

    val paidCount = paidBills.size
    val remainingCount = unpaidBills.size

    val totalAmount = filtered.sumOf { it.bill.amount }
    val paidAmount = paidBills.sumOf { it.bill.amount }
    val remainingAmount = unpaidBills.sumOf { it.bill.amount }

    // Count bills due within 7 days
    val dueIn7Days = unpaidBills.count {
      val days = java.time.temporal.ChronoUnit.DAYS.between(today, it.targetDueDate)
      days in 0..7
    }
    val dueThisWeekMsg = when {
      dueIn7Days == 0 -> "No bills due this week"
      dueIn7Days == 1 -> "1 bill due this week"
      else -> "$dueIn7Days bills due this week"
    }

    BillsUiState(
      yearMonth = ym,
      formattedMonth = formattedMonth,
      totalCount = totalCount,
      paidCount = paidCount,
      remainingCount = remainingCount,
      totalAmount = totalAmount,
      paidAmount = paidAmount,
      remainingAmount = remainingAmount,
      upcomingBills = upcomingBills,
      paidBills = paidBills,
      overdueBills = overdueBills,
      dueThisWeekMessage = dueThisWeekMsg,
      selectedCategoryFilter = categoryFilter,
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = BillsUiState(isLoading = true)
  )

  fun onSelectMonth(yearMonth: YearMonth) {
    _selectedYearMonth.value = yearMonth
  }

  fun onPreviousMonth() {
    _selectedYearMonth.value = _selectedYearMonth.value.minusMonths(1)
  }

  fun onNextMonth() {
    _selectedYearMonth.value = _selectedYearMonth.value.plusMonths(1)
  }

  fun onFilterCategory(category: BillCategory?) {
    _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
  }

  fun onTogglePaid(billUi: BillUiModel) {
    viewModelScope.launch {
      val ym = _selectedYearMonth.value
      if (billUi.isPaid) {
        repository.markAsUnpaid(billUi.bill.id, ym.year, ym.monthValue)
      } else {
        repository.markAsPaid(
          billId = billUi.bill.id,
          year = ym.year,
          month = ym.monthValue,
          amount = billUi.bill.amount,
          notes = "Marked as paid"
        )
      }
      // If currently viewing detail, update it
      if (_viewingBillDetail.value?.bill?.id == billUi.bill.id) {
        _viewingBillDetail.value = billUi.copy(isPaid = !billUi.isPaid)
      }
    }
  }

  fun onOpenAddBill() {
    _editingBill.value = null
    _isAddEditDialogOpen.value = true
  }

  fun onOpenEditBill(bill: BillEntity) {
    _editingBill.value = bill
    _isAddEditDialogOpen.value = true
    _viewingBillDetail.value = null
  }

  fun onCloseAddEditDialog() {
    _editingBill.value = null
    _isAddEditDialogOpen.value = false
  }

  fun onSaveBill(bill: BillEntity) {
    viewModelScope.launch {
      if (bill.id == 0L) {
        repository.insertBill(bill)
      } else {
        repository.updateBill(bill)
      }
      _isAddEditDialogOpen.value = false
      _editingBill.value = null
    }
  }

  fun onDeleteBill(billId: Long) {
    viewModelScope.launch {
      repository.deleteBill(billId)
      _viewingBillDetail.value = null
      _isAddEditDialogOpen.value = false
    }
  }

  fun onOpenBillDetail(billUi: BillUiModel) {
    _viewingBillDetail.value = billUi
  }

  fun onCloseBillDetail() {
    _viewingBillDetail.value = null
  }

  fun onOpenCalendarView() {
    _isCalendarViewOpen.value = true
  }

  fun onCloseCalendarView() {
    _isCalendarViewOpen.value = false
  }

  fun onOpenSettings() {
    _isSettingsOpen.value = true
  }

  fun onCloseSettings() {
    _isSettingsOpen.value = false
  }

  fun onSendTestNotification() {
    BillNotificationHelper.sendTestNotification(getApplication())
  }
}

class BillViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
      return BillViewModel(application) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
