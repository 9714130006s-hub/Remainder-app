package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.BillUiModel
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusPaidText
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillCalendarView(
  yearMonth: YearMonth,
  allBills: List<BillUiModel>,
  onDismiss: () -> Unit,
  onSelectBill: (BillUiModel) -> Unit,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedDate by remember { mutableStateOf(LocalDate.now()) }

  val daysInMonth = yearMonth.lengthOfMonth()
  val firstDayOfMonth = yearMonth.atDay(1)
  val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0, Monday = 1 ...

  val billsByDay = remember(allBills, yearMonth) {
    allBills.groupBy { it.bill.dueDayOfMonth.coerceIn(1, daysInMonth) }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
        .padding(bottom = 28.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Due Dates Calendar",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Month Selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onPreviousMonth) {
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }

        Text(
          text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNextMonth) {
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Day of Week Header Row
      val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        daysOfWeek.forEach { d ->
          Text(
            text = d,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Calendar Grid (6 rows x 7 cols)
      val totalSlots = 42
      for (row in 0 until 6) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          for (col in 0 until 7) {
            val slotIndex = row * 7 + col
            val dayNumber = slotIndex - firstDayOfWeek + 1

            if (dayNumber in 1..daysInMonth) {
              val isSelected = selectedDate.dayOfMonth == dayNumber && selectedDate.month == yearMonth.month
              val billsForDay = billsByDay[dayNumber] ?: emptyList()
              val hasBills = billsForDay.isNotEmpty()
              val hasUnpaid = billsForDay.any { !it.isPaid }

              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      isSelected -> BrandPrimary
                      hasBills -> MaterialTheme.colorScheme.surfaceVariant
                      else -> Color.Transparent
                    }
                  )
                  .clickable {
                    selectedDate = yearMonth.atDay(dayNumber)
                  },
                contentAlignment = Alignment.Center
              ) {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Text(
                    text = "$dayNumber",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected || hasBills) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                      isSelected -> Color.White
                      hasBills -> MaterialTheme.colorScheme.onSurface
                      else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                  )

                  if (hasBills) {
                    Box(
                      modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                          when {
                            isSelected -> Color.White
                            hasUnpaid -> BrandPrimary
                            else -> StatusPaidText
                          }
                        )
                    )
                  }
                }
              }
            } else {
              Spacer(modifier = Modifier.size(40.dp))
            }
          }
        }
        Spacer(modifier = Modifier.height(4.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Selected Day Details
      val selectedDayBills = billsByDay[selectedDate.dayOfMonth] ?: emptyList()
      Text(
        text = "Bills Due on ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} ${selectedDate.dayOfMonth}:",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(8.dp))

      if (selectedDayBills.isEmpty()) {
        Text(
          text = "No bills scheduled for this day.",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(vertical = 8.dp)
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(selectedDayBills) { b ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .clickable { onSelectBill(b) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(b.category.backgroundColor),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = b.category.icon,
                    contentDescription = null,
                    tint = b.category.iconColor,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = b.bill.title,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = b.formattedAmount,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (b.isPaid) "✓ Paid" else "Upcoming",
                  fontSize = 11.sp,
                  color = if (b.isPaid) StatusPaidText else BrandPrimary
                )
              }
            }
          }
        }
      }
    }
  }
}
