package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.StatusPaidText
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BillsUiState
import java.util.Locale

@Composable
fun MonthHeaderSection(
  uiState: BillsUiState,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onOpenCalendar: () -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.surface)
      .padding(horizontal = 20.dp, vertical = 12.dp)
  ) {
    // Top Bar with App Title and Action Icons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Bill Reminder",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("app_title")
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
          onClick = onOpenCalendar,
          modifier = Modifier.testTag("calendar_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = "Calendar view",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
          )
        }

        IconButton(
          onClick = onOpenSettings,
          modifier = Modifier.testTag("settings_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.NotificationsActive,
            contentDescription = "Notification Settings",
            tint = BrandPrimary,
            modifier = Modifier.size(24.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Month Navigation & Summary Counts
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Month selector
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
      ) {
        IconButton(
          onClick = onPreviousMonth,
          modifier = Modifier.size(32.dp).testTag("prev_month_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = "Previous month",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }

        Text(
          text = uiState.formattedMonth,
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 4.dp).testTag("month_display")
        )

        IconButton(
          onClick = onNextMonth,
          modifier = Modifier.size(32.dp).testTag("next_month_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Next month",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      // Metric Summary Pills (e.g. 8 Total bills · 3 Paid · 5 Remaining)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatItem(
          count = uiState.totalCount,
          label = "Total bills",
          countColor = BrandPrimary
        )

        StatItem(
          count = uiState.paidCount,
          label = "Paid",
          countColor = StatusPaidText
        )

        StatItem(
          count = uiState.remainingCount,
          label = "Remaining",
          countColor = Color(0xFFF59E0B)
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Spending Summary Bar
    if (uiState.totalCount > 0) {
      val progress = (uiState.paidAmount / uiState.totalAmount.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Paid: ${String.format(Locale.US, "$%,.2f", uiState.paidAmount)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = StatusPaidText
          )
          Text(
            text = "Total: ${String.format(Locale.US, "$%,.2f", uiState.totalAmount)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BrandPrimary,
          trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
      }
    }
  }
}

@Composable
private fun StatItem(
  count: Int,
  label: String,
  countColor: Color,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = "$count",
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = countColor
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = label,
      fontSize = 12.sp,
      fontWeight = FontWeight.Normal,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
