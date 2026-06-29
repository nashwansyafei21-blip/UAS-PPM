package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val isExpense: Boolean, // true for Expense, false for Income
    val category: String,   // Makanan, Transportasi, Hiburan, Belanja, Gaji, dll.
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)
