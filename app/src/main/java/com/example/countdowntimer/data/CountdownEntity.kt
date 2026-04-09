package com.example.countdowntimer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countdowns")
data class CountdownEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val targetTime: Long,
    val imageUri: String? = null,
    val colorIndex: Int = 0,
    val repeatYearly: Boolean = false,
    val category: String = "General",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)