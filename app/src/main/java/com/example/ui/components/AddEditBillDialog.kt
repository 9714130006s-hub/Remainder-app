package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BillCategory
import com.example.data.model.BillEntity
import com.example.data.model.Frequency
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusOverdueText
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillDialog(
  initialBill: BillEntity?,
  onDismiss: () -> Unit,
  onSave: (BillEntity) -> Unit,
  onDelete: ((Long) -> Unit)?,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val focusManager = LocalFocusManager.current

  var title by remember { mutableStateOf(initialBill?.title ?: "") }
  var amountStr by remember { mutableStateOf(initialBill?.let { String.format("%.2f", it.amount) } ?: "") }
  var selectedCategory by remember {
    mutableStateOf(BillCategory.fromString(initialBill?.category ?: BillCategory.STREAMING.name))
  }
  var selectedFrequency by remember {
    mutableStateOf(Frequency.fromString(initialBill?.frequency ?: Frequency.MONTHLY.name))
  }
  var dueDayOfMonth by remember { mutableIntStateOf(initialBill?.dueDayOfMonth ?: 15) }
  var reminderDaysBefore by remember { mutableIntStateOf(initialBill?.reminderDaysBefore ?: 1) }
  var reminderHour by remember { mutableIntStateOf(initialBill?.reminderTimeHour ?: 9) }
  var reminderMinute by remember { mutableIntStateOf(initialBill?.reminderTimeMinute ?: 0) }
  var isAutoPay by remember { mutableStateOf(initialBill?.isAutoPay ?: false) }
  var notes by remember { mutableStateOf(initialBill?.notes ?: "") }
  var showError by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 12.dp)
        .padding(bottom = 32.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (initialBill == null) "Add New Bill" else "Edit Bill",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (initialBill != null && onDelete != null) {
          IconButton(
            onClick = { onDelete(initialBill.id) },
            modifier = Modifier.testTag("delete_bill_button")
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete bill",
              tint = StatusOverdueText
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Bill Title Input
      OutlinedTextField(
        value = title,
        onValueChange = {
          title = it
          showError = false
        },
        label = { Text("Bill Name (e.g. Electricity, Netflix)") },
        singleLine = true,
        isError = showError && title.isBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bill_title_input"),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Amount Input
      OutlinedTextField(
        value = amountStr,
        onValueChange = {
          amountStr = it
          showError = false
        },
        label = { Text("Amount ($)") },
        leadingIcon = {
          Icon(Icons.Default.AttachMoney, contentDescription = "Dollar", tint = BrandPrimary)
        },
        singleLine = true,
        isError = showError && (amountStr.toDoubleOrNull() ?: 0.0) <= 0.0,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bill_amount_input"),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Category Selector Chips
      Text(
        text = "Category",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        BillCategory.entries.forEach { cat ->
          val isSelected = cat == selectedCategory
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) BrandPrimary else cat.backgroundColor)
              .clickable { selectedCategory = cat }
              .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = cat.icon,
                contentDescription = cat.displayName,
                tint = if (isSelected) Color.White else cat.iconColor,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = cat.displayName,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Due Day of Month Slider
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Due Day of Month",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "Day $dueDayOfMonth",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          color = BrandPrimary
        )
      }

      Slider(
        value = dueDayOfMonth.toFloat(),
        onValueChange = { dueDayOfMonth = it.toInt() },
        valueRange = 1f..31f,
        steps = 29,
        modifier = Modifier.fillMaxWidth().testTag("due_day_slider")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Frequency Selector
      Text(
        text = "Repeat Frequency",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Frequency.entries.forEach { freq ->
          val isSelected = freq == selectedFrequency
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surfaceVariant)
              .clickable { selectedFrequency = freq }
              .padding(horizontal = 12.dp, vertical = 7.dp)
          ) {
            Text(
              text = freq.displayName,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Automated Reminder Notification Settings Section
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(14.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = "Automated Alert",
            tint = BrandPrimary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Automated Due Date Reminder",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Send alert before due date:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        val reminderOptions = listOf(
          0 to "On due date",
          1 to "1 day before",
          2 to "2 days before",
          3 to "3 days before",
          7 to "1 week before"
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          reminderOptions.forEach { (days, label) ->
            val isSelected = reminderDaysBefore == days
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surface)
                .clickable { reminderDaysBefore = days }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Alert time:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          val timeOptions = listOf(
            Pair(9, 0) to "9:00 AM",
            Pair(12, 0) to "12:00 PM",
            Pair(18, 0) to "6:00 PM",
            Pair(20, 0) to "8:00 PM"
          )

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            timeOptions.forEach { (time, label) ->
              val isSelected = reminderHour == time.first && reminderMinute == time.second
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) BrandPrimary else MaterialTheme.colorScheme.surface)
                  .clickable {
                    reminderHour = time.first
                    reminderMinute = time.second
                  }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Auto Pay Switch
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Auto-Pay Enabled",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "Automatically debited from bank or card",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Switch(
          checked = isAutoPay,
          onCheckedChange = { isAutoPay = it },
          modifier = Modifier.testTag("autopay_switch")
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Notes
      OutlinedTextField(
        value = notes,
        onValueChange = { notes = it },
        label = { Text("Notes (optional)") },
        maxLines = 3,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bill_notes_input"),
        shape = RoundedCornerShape(12.dp),
      )

      if (showError) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Please enter a valid bill name and amount",
          fontSize = 12.sp,
          color = StatusOverdueText
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(48.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Cancel")
        }

        Button(
          onClick = {
            val amt = amountStr.toDoubleOrNull()
            if (title.isBlank() || amt == null || amt <= 0.0) {
              showError = true
            } else {
              val today = LocalDate.now()
              val targetDate = LocalDate.of(today.year, today.monthValue, dueDayOfMonth.coerceIn(1, 28))

              val billToSave = BillEntity(
                id = initialBill?.id ?: 0L,
                title = title.trim(),
                amount = amt,
                category = selectedCategory.name,
                dueDayOfMonth = dueDayOfMonth,
                dueDateEpochDay = targetDate.toEpochDay(),
                frequency = selectedFrequency.name,
                reminderDaysBefore = reminderDaysBefore,
                reminderTimeHour = reminderHour,
                reminderTimeMinute = reminderMinute,
                isAutoPay = isAutoPay,
                notes = notes.trim(),
              )
              onSave(billToSave)
            }
          },
          modifier = Modifier.weight(1f).height(48.dp).testTag("save_bill_button"),
          colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Save Bill", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
