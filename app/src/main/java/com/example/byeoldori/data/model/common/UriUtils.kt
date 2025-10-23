package com.example.byeoldori.data.model.common

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

//갤러리에서 선택한 파일(URI)을 앱에 캐시 폴더에 복사해 File객체로 만듦(Uri -> file객체)
fun copyUriToCache(context: Context, uri: Uri, filename: String? = null): File {
    val name = filename ?: queryDisplayName(context, uri) ?: "upload_${System.currentTimeMillis()}"
    val dest = File(context.cacheDir, name)
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(dest).use { output -> input.copyTo(output) }
    } ?: error("failed to open input stream: $uri")
    return dest
}

fun queryDisplayName(context: Context, uri: Uri): String? {
    var name: String? = null
    val cursor: Cursor? = context.contentResolver.query(uri,null,null,null,null)
    cursor?.use {
        val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if(idx >= 0 && it.moveToFirst()) name = it.getString(idx)
    }
    return name
}