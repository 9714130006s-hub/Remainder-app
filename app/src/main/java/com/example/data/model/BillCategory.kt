package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatCreditCardBg
import com.example.ui.theme.CatCreditCardIcon
import com.example.ui.theme.CatElectricityBg
import com.example.ui.theme.CatElectricityIcon
import com.example.ui.theme.CatGymBg
import com.example.ui.theme.CatGymIcon
import com.example.ui.theme.CatInsuranceBg
import com.example.ui.theme.CatInsuranceIcon
import com.example.ui.theme.CatInternetBg
import com.example.ui.theme.CatInternetIcon
import com.example.ui.theme.CatMortgageBg
import com.example.ui.theme.CatMortgageIcon
import com.example.ui.theme.CatMusicBg
import com.example.ui.theme.CatMusicIcon
import com.example.ui.theme.CatOtherBg
import com.example.ui.theme.CatOtherIcon
import com.example.ui.theme.CatPhoneBg
import com.example.ui.theme.CatPhoneIcon
import com.example.ui.theme.CatRentBg
import com.example.ui.theme.CatRentIcon
import com.example.ui.theme.CatStreamingBg
import com.example.ui.theme.CatStreamingIcon
import com.example.ui.theme.CatWaterBg
import com.example.ui.theme.CatWaterIcon

enum class BillCategory(
  val displayName: String,
  val icon: ImageVector,
  val backgroundColor: Color,
  val iconColor: Color,
) {
  STREAMING("Streaming", Icons.Default.Tv, CatStreamingBg, CatStreamingIcon),
  WATER("Water", Icons.Default.WaterDrop, CatWaterBg, CatWaterIcon),
  ELECTRICITY("Electricity", Icons.Default.ElectricBolt, CatElectricityBg, CatElectricityIcon),
  MORTGAGE("Mortgage", Icons.Default.LocationCity, CatMortgageBg, CatMortgageIcon),
  MUSIC("Music", Icons.Default.MusicNote, CatMusicBg, CatMusicIcon),
  INTERNET("Internet", Icons.Default.Wifi, CatInternetBg, CatInternetIcon),
  GYM("Gym", Icons.Default.FitnessCenter, CatGymBg, CatGymIcon),
  RENT("Rent", Icons.Default.Home, CatRentBg, CatRentIcon),
  INSURANCE("Insurance", Icons.Default.Shield, CatInsuranceBg, CatInsuranceIcon),
  PHONE("Phone", Icons.Default.PhoneAndroid, CatPhoneBg, CatPhoneIcon),
  CREDIT_CARD("Credit Card", Icons.Default.Payment, CatCreditCardBg, CatCreditCardIcon),
  GAS("Gas / Fuel", Icons.Default.LocalFireDepartment, CatInsuranceBg, CatInsuranceIcon),
  OTHER("Other", Icons.Default.MoreHoriz, CatOtherBg, CatOtherIcon);

  companion object {
    fun fromString(name: String?): BillCategory {
      return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
        ?: OTHER
    }
  }
}
