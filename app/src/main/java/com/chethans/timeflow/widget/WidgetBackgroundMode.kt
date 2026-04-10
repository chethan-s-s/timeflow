package com.chethans.timeflow.widget

enum class WidgetBackgroundMode {
    TRANSPARENT,
    COLOR,
    IMAGE_OR_COLOR
}

enum class WidgetLayoutMode {
    ONE_BY_FIVE,
    TWO_BY_TWO;

    companion object {
        fun fromStorage(value: String?): WidgetLayoutMode {
            return entries.firstOrNull { it.name == value } ?: ONE_BY_FIVE
        }
    }
}
