package com.chethans.timeflow.util

import java.util.Locale

// Central keyword bank used for default category auto-classification.
val DefaultCategoryKeywords: LinkedHashMap<String, List<String>> = linkedMapOf(
    "Birthday" to listOf("birthday", "bday", "born", "turning"),
    "Anniversary" to listOf("anniversary", "wedding", "engagement", "years together"),
    "Running Events" to listOf("marathon", "half marathon", "10k", "5k", "run", "race", "trail run", "ultra"),
    "Deadlines" to listOf("deadline", "due", "submission", "submit", "deliverable", "milestone", "cutoff"),
    "Exams" to listOf("exam", "test", "quiz", "midterm", "final", "viva", "assessment"),
    "Work" to listOf("meeting", "sprint", "release", "deploy", "deployment", "project", "client", "interview"),
    "Travel" to listOf("trip", "flight", "vacation", "holiday trip", "check-in", "checkout", "itinerary"),
    "Health" to listOf("doctor", "appointment", "checkup", "therapy", "medicine", "vaccination", "hospital"),
    "Fitness" to listOf("gym", "workout", "training", "yoga", "crossfit", "cardio", "cycling"),
    "Finance" to listOf("bill", "payment", "emi", "rent", "invoice", "tax", "salary", "loan"),
    "Holiday" to listOf("christmas", "new year", "diwali", "eid", "thanksgiving", "festival", "holiday"),
    "Personal" to listOf("family", "friend", "party", "event", "celebration", "reminder")
)

val DefaultCategoryOptions: List<String> = listOf("General") + DefaultCategoryKeywords.keys

fun classifyCategoryFromTitle(title: String): String {
    if (title.isBlank()) return "General"

    val normalizedTitle = title
        .lowercase(Locale.getDefault())
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    for ((category, keywords) in DefaultCategoryKeywords) {
        if (keywords.any { keyword ->
                val normalizedKeyword = keyword.lowercase(Locale.getDefault())
                normalizedTitle.contains(normalizedKeyword)
            }) {
            return category
        }
    }

    return "General"
}

