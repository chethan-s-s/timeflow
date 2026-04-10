package com.chethans.timeflow.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Copies a picked content URI image into the app's private internal storage
 * so it remains accessible even after the app is cleared from recents / cache.
 *
 * @return A "file://…" URI string pointing to the persisted copy,
 *         or the original string if anything fails (best-effort fallback).
 */
fun copyImageToInternalStorage(context: Context, uriString: String): String {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString

        val filename = "countdown_img_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)

        FileOutputStream(file).use { out ->
            inputStream.use { it.copyTo(out) }
        }

        Uri.fromFile(file).toString()   // e.g. file:///data/user/0/.../files/countdown_img_xxx.jpg
    } catch (e: Exception) {
        uriString   // fallback – at least works for the current session
    }
}

/**
 * Deletes a previously persisted image from internal storage.
 * Safe to call with any URI string – silently ignores non-file URIs.
 */
fun deleteImageFromInternalStorage(uriString: String?) {
    if (uriString.isNullOrEmpty()) return
    try {
        val uri = Uri.parse(uriString)
        if (uri.scheme == "file") {
            File(uri.path ?: return).delete()
        }
    } catch (_: Exception) { }
}

