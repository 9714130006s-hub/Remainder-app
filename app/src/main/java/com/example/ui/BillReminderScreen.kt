package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BillCategory
import com.example.ui.components.AddEditBillDialog
import com.example.ui.components.BillCalendarView
import com.example.ui.components.BillCardItem
import com.example.ui.components.BillDetailDialog
import com.example.ui.components.MonthHeaderSection
import com.example.ui.components.ReminderSettingsDialog
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CatWaterBg
import com.example.ui.theme.StatusOverdueText
import com.example.ui.theme.StatusPaidText
import com.example.viewmodel.BillViewModel

@Composable
fun BillReminderScreen(
  viewModel: BillViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val isAddEditOpen by viewModel.isAddEditDialogOpen.collectAsStateWithLifecycle()
  val editingBill by viewModel.editingBill.collectAsStateWithLifecycle()
  val viewingDetail by viewModel.viewingBillDetail.collectAsStateWithLifecycle()
  val isCalendarOpen by viewModel.isCalendarViewOpen.collectAsStateWithLifecycle()
  val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding(),
    bottomBar = {
      // Floating Bottom "+ Add bill" Bar
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          Button(
            onClick = viewModel::onOpenAddBill,
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("add_bill_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = BrandPrimary,
              contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "Add bill",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "+ Add bill",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Top Month Header and Metric Chips
      MonthHeaderSection(
        uiState = uiState,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onOpenCalendar = viewModel::onOpenCalendarView,
        onOpenSettings = viewModel::onOpenSettings
      )

      // Category Filter Chip Row
      CategoryFilterRow(
        selectedCategory = uiState.selectedCategoryFilter,
        onSelectCategory = viewModel::onFilterCategory
      )

      // Bills Scrollable List
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Overdue Section (if any)
        if (uiState.overdueBills.isNotEmpty()) {
          item(key = "header_overdue") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Overdue Bills",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = StatusOverdueText
              )
              Text(
                text = "${uiState.overdueBills.size} unpaid",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StatusOverdueText
              )
            }
          }

          items(
            items = uiState.overdueBills,
            key = { "overdue_${it.bill.id}" }
          ) { billUi ->
            BillCardItem(
              billUi = billUi,
              onClick = { viewModel.onOpenBillDetail(billUi) },
              onTogglePaid = { viewModel.onTogglePaid(billUi) }
            )
          }
        }

        // "Your bills" Section Header
        item(key = "header_your_bills") {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Your bills",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Text(
              text = uiState.dueThisWeekMessage,
              fontSize = 12.sp,
              fontWeight = FontWeight.Normal,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        // Empty Upcoming State
        if (uiState.upcomingBills.isEmpty() && uiState.overdueBills.isEmpty()) {
          item(key = "empty_upcoming") {
            EmptyBillsCard(
              message = if (uiState.paidBills.isNotEmpty()) "All bills paid for ${uiState.formattedMonth}!" else "No upcoming bills found."
            )
          }
        } else {
          items(
            items = uiState.upcomingBills,
            key = { "upcoming_${it.bill.id}" }
          ) { billUi ->
            BillCardItem(
              billUi = billUi,
              onClick = { viewModel.onOpenBillDetail(billUi) },
              onTogglePaid = { viewModel.onTogglePaid(billUi) }
            )
          }
        }

        // "Paid this period" Section Header
        if (uiState.paidBills.isNotEmpty()) {
          item(key = "header_paid_bills") {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 4.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Paid this period",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )

              Text(
                text = "${uiState.paidBills.size} completed",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = StatusPaidText
              )
            }
          }

          items(
            items = uiState.paidBills,
            key = { "paid_${it.bill.id}" }
          ) { billUi ->
            BillCardItem(
              billUi = billUi,
              onClick = { viewModel.onOpenBillDetail(billUi) },
              onTogglePaid = { viewModel.onTogglePaid(billUi) }
            )
          }
        }

        // Footer Banner: "Never miss a payment" (matching screenshot)
        item(key = "footer_banner") {
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surface)
              .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CatWaterBg),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = BrandPrimary,
                modifier = Modifier.size(26.dp)
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
              Text(
                text = "Never miss a payment",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Automated reminders alert you before every due date.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }

  // Modals & BottomSheets
  if (isAddEditOpen) {
    AddEditBillDialog(
      initialBill = editingBill,
      onDismiss = viewModel::onCloseAddEditDialog,
      onSave = viewModel::onSaveBill,
      onDelete = if (editingBill != null) { id -> viewModel.onDeleteBill(id) } else null
    )
  }

  if (viewingDetail != null) {
    BillDetailDialog(
      billUi = viewingDetail!!,
      onDismiss = viewModel::onCloseBillDetail,
      onTogglePaid = { viewModel.onTogglePaid(viewingDetail!!) },
      onEdit = viewModel::onOpenEditBill
    )
  }

  if (isCalendarOpen) {
    val allMonthBills = uiState.upcomingBills + uiState.paidBills + uiState.overdueBills
    BillCalendarView(
      yearMonth = uiState.yearMonth,
      allBills = allMonthBills,
      onDismiss = viewModel::onCloseCalendarView,
      onSelectBill = { bill ->
        viewModel.onCloseCalendarView()
        viewModel.onOpenBillDetail(bill)
      },
      onPreviousMonth = viewModel::onPreviousMonth,
      onNextMonth = viewModel::onNextMonth
    )
  }

  if (isSettingsOpen) {
    ReminderSettingsDialog(
      activeBillCount = uiState.totalCount,
      onDismiss = viewModel::onCloseSettings,
      onSendTestNotification = viewModel::onSendTestNotification
    )
  }
}

@Composable
private fun CategoryFilterRow(
  selectedCategory: BillCategory?,
  onSelectCategory: (BillCategory?) -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 20.dp, vertical = 6.dp)
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // "All" chip
    val isAllSelected = selectedCategory == null
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(if (isAllSelected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        .clickable { onSelectCategory(null) }
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      Text(
        text = "All",
        fontSize = 12.sp,
        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface
      )
    }

    // Category chips
    BillCategory.entries.forEach { cat ->
      val isSelected = cat == selectedCategory
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .clickable { onSelectCategory(cat) }
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = cat.icon,
            contentDescription = cat.displayName,
            tint = if (isSelected) Color.White else cat.iconColor,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = cat.displayName,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyBillsCard(message: String) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 12.dp),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = StatusPaidText,
        modifier = Modifier.size(36.dp)
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = message,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
