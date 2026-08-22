package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillEntity
import com.example.ui.model.BillStatus
import com.example.ui.model.BillUiModel
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusDueSoonBg
import com.example.ui.theme.StatusDueSoonText
import com.example.ui.theme.StatusOverdueBg
import com.example.ui.theme.StatusOverdueText
import com.example.ui.theme.StatusPaidBg
import com.example.ui.theme.StatusPaidText
import com.example.ui.theme.StatusUpcomingBg
import com.example.ui.theme.StatusUpcomingText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailDialog(
  billUi: BillUiModel,
  onDismiss: () -> Unit,
  onTogglePaid: () -> Unit,
  onEdit: (BillEntity) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val category = billUi.category

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .padding(bottom = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Category Icon Header
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(category.backgroundColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = category.icon,
          contentDescription = category.displayName,
          tint = category.iconColor,
          modifier = Modifier.size(32.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = billUi.bill.title,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = billUi.formattedAmount,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BrandPrimary
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Status Pill
      val (statusBg, statusText, statusLabel) = when (billUi.status) {
        BillStatus.PAID -> Triple(StatusPaidBg, StatusPaidText, "✓ Paid this period")
        BillStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueText, "⚠️ Overdue")
        BillStatus.DUE_TODAY -> Triple(StatusDueSoonBg, StatusDueSoonText, "⏰ Due Today")
        BillStatus.DUE_SOON -> Triple(StatusDueSoonBg, StatusDueSoonText, "⏰ Due Soon")
        BillStatus.UPCOMING -> Triple(StatusUpcomingBg, StatusUpcomingText, "Upcoming")
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .background(statusBg)
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Text(
          text = statusLabel,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold,
          color = statusText
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Detail Rows
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        DetailRow(
          icon = Icons.Default.CalendarToday,
          label = "Due Date",
          value = "${billUi.formattedDueDate} (Day ${billUi.bill.dueDayOfMonth})"
        )

        DetailRow(
          icon = Icons.Default.Repeat,
          label = "Frequency",
          value = billUi.frequency.displayName
        )

        val reminderText = if (billUi.bill.reminderDaysBefore == 0) {
          "On due date at ${String.format("%02d:%02d", billUi.bill.reminderTimeHour, billUi.bill.reminderTimeMinute)}"
        } else {
          "${billUi.bill.reminderDaysBefore} day(s) before at ${String.format("%02d:%02d", billUi.bill.reminderTimeHour, billUi.bill.reminderTimeMinute)}"
        }

        DetailRow(
          icon = Icons.Default.Notifications,
          label = "Automated Alert",
          value = reminderText
        )

        if (billUi.bill.isAutoPay) {
          DetailRow(
            icon = Icons.Default.Autorenew,
            label = "Auto-Pay",
            value = "Enabled (Auto-debited)"
          )
        }

        if (billUi.bill.notes.isNotBlank()) {
          DetailRow(
            icon = Icons.Default.Notes,
            label = "Notes",
            value = billUi.bill.notes
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Toggle Paid Button
      Button(
        onClick = onTogglePaid,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("dialog_mark_paid_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (billUi.isPaid) MaterialTheme.colorScheme.surfaceVariant else StatusPaidText
        )
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = if (billUi.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (billUi.isPaid) "Mark as Unpaid" else "Mark as Paid",
          fontWeight = FontWeight.Bold,
          color = if (billUi.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Edit Button
      OutlinedButton(
        onClick = { onEdit(billUi.bill) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("dialog_edit_bill_button"),
        shape = RoundedCornerShape(14.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Edit,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Edit Bill Details", fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
private fun DetailRow(
  icon: ImageVector,
  label: String,
  value: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = BrandPrimary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Text(
      text = value,
      fontSize = 13.sp,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
