package com.chethans.timeflow.ui.components

enum class CardLayoutMode {
    SINGLE_COLUMN,
    TWO_COLUMN;

    companion object {
        fun fromStorage(value: String?): CardLayoutMode {
            return entries.firstOrNull { it.name == value } ?: SINGLE_COLUMN
        }
    }
}

