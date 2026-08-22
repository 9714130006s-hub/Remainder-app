package com.example.data.model

enum class Frequency(val displayName: String) {
  MONTHLY("Monthly"),
  YEARLY("Yearly"),
  BI_WEEKLY("Bi-weekly"),
  WEEKLY("Weekly"),
  ONE_TIME("One-time");

  companion object {
    fun fromString(name: String?): Frequency {
      return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
        ?: MONTHLY
    }
  }
}
