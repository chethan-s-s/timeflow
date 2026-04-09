package com.example.countdowntimer.util

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.countdowntimer.data.CountdownEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private const val BACKUP_VERSION = 1

data class BackupResult(
    val success: Boolean,
    val message: String,
    val count: Int = 0
)

fun exportCountdownsToJson(
    context: Context,
    uri: Uri,
    items: List<CountdownEntity>,
    includeImages: Boolean
): BackupResult {
    return try {
        val root = JSONObject().apply {
            put("version", BACKUP_VERSION)
            put("exportedAt", System.currentTimeMillis())
            put("includeImages", includeImages)
        }

        val countdownsJson = JSONArray()
        items.forEach { item ->
            countdownsJson.put(
                JSONObject().apply {
                    put("title", item.title)
                    put("targetTime", item.targetTime)
                    put("imageUri", item.imageUri ?: JSONObject.NULL)
                    put("colorIndex", item.colorIndex)
                    put("repeatYearly", item.repeatYearly)
                    put("category", item.category)
                    put("isArchived", item.isArchived)
                    put("createdAt", item.createdAt)

                    if (includeImages && !item.imageUri.isNullOrBlank()) {
                        val imageBytes = readUriBytes(context, Uri.parse(item.imageUri))
                        if (imageBytes != null) {
                            put("imageData", Base64.encodeToString(imageBytes, Base64.NO_WRAP))
                        }
                    }
                }
            )
        }
        root.put("countdowns", countdownsJson)

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(root.toString(2).toByteArray())
        } ?: return BackupResult(false, "Could not open selected file")

        BackupResult(true, "Backup exported", items.size)
    } catch (e: Exception) {
        BackupResult(false, e.message ?: "Backup export failed")
    }
}

fun importCountdownsFromJson(
    context: Context,
    uri: Uri
): Result<List<CountdownEntity>> {
    return runCatching {
        val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not read selected backup file")

        val root = JSONObject(content)
        val countdowns = root.optJSONArray("countdowns") ?: JSONArray()
        val restoredItems = buildList {
            for (i in 0 until countdowns.length()) {
                val obj = countdowns.getJSONObject(i)
                val restoredUri = restoreImageFromBackup(context, obj)
                add(
                    CountdownEntity(
                        title = obj.optString("title", "Countdown"),
                        targetTime = obj.optLong("targetTime", System.currentTimeMillis() + 60_000L),
                        imageUri = restoredUri ?: obj.optString("imageUri").takeIf { it.isNotBlank() && it != "null" },
                        colorIndex = obj.optInt("colorIndex", 0),
                        repeatYearly = obj.optBoolean("repeatYearly", false),
                        category = obj.optString("category", "General"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }
        restoredItems
    }
}

private fun restoreImageFromBackup(context: Context, obj: JSONObject): String? {
    if (!obj.has("imageData")) return null
    return try {
        val bytes = Base64.decode(obj.getString("imageData"), Base64.DEFAULT)
        val file = File(context.filesDir, "restored_${System.currentTimeMillis()}_${(0..9999).random()}.jpg")
        file.outputStream().use { it.write(bytes) }
        Uri.fromFile(file).toString()
    } catch (_: Exception) {
        null
    }
}

private fun readUriBytes(context: Context, uri: Uri): ByteArray? {
    return try {
        when (uri.scheme) {
            "file", null -> {
                val path = uri.path ?: return null
                File(path).takeIf { it.exists() }?.readBytes()
            }
            else -> context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        }
    } catch (_: Exception) {
        null
    }
}

