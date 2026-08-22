package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun BillCardItem(
  billUi: BillUiModel,
  onClick: () -> Unit,
  onTogglePaid: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val category = billUi.category

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("bill_item_${billUi.bill.id}"),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    shadowElevation = 0.5.dp,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Left: Category Icon Container
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(category.backgroundColor),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = category.icon,
            contentDescription = category.displayName,
            tint = category.iconColor,
            modifier = Modifier.size(24.dp)
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Info: Title, Countdown / Due date, AutoPay indicator
        Column(
          modifier = Modifier.weight(1f)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = billUi.bill.title,
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            if (billUi.bill.isAutoPay) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = "Auto Pay active",
                tint = BrandPrimary,
                modifier = Modifier.size(14.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(3.dp))

          Text(
            text = billUi.countdownText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            color = when (billUi.status) {
              BillStatus.OVERDUE -> StatusOverdueText
              BillStatus.DUE_TODAY, BillStatus.DUE_SOON -> StatusDueSoonText
              BillStatus.PAID -> StatusPaidText
              BillStatus.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Right Info: Amount, Status Pill, Chevron or Direct Paid Checkbox
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = billUi.formattedAmount,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(3.dp))

          StatusBadge(status = billUi.status)
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Quick Toggle Paid Checkbox Button
        IconButton(
          onClick = onTogglePaid,
          modifier = Modifier
            .size(36.dp)
            .testTag("toggle_paid_button_${billUi.bill.id}")
        ) {
          if (billUi.isPaid) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Paid - tap to mark unpaid",
              tint = StatusPaidText,
              modifier = Modifier.size(22.dp)
            )
          } else {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "View bill details",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatusBadge(status: BillStatus) {
  val (bgColor, textColor, label) = when (status) {
    BillStatus.PAID -> Triple(StatusPaidBg, StatusPaidText, "Paid")
    BillStatus.OVERDUE -> Triple(StatusOverdueBg, StatusOverdueText, "Overdue")
    BillStatus.DUE_TODAY -> Triple(StatusDueSoonBg, StatusDueSoonText, "Due today")
    BillStatus.DUE_SOON -> Triple(StatusDueSoonBg, StatusDueSoonText, "Due soon")
    BillStatus.UPCOMING -> Triple(StatusUpcomingBg, StatusUpcomingText, "Upcoming")
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bgColor)
      .padding(horizontal = 7.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      color = textColor
    )
  }
}
