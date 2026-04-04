package com.m16a4666.heywear.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {

    suspend fun saveImage(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            try {
                // 1. 下载图片
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()
                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = result?.toBitmap()

                if (bitmap == null) {
                    showToast(context, "下载失败")
                    return@withContext
                }

                val filename = "HeyWear_${System.currentTimeMillis()}.jpg"

                //分版本保存逻辑
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android10+(Wear OS 3/4/5)
                    //用MediaStore API
                    saveToMediaStore(context, bitmap, filename)
                } else {
                    //Android9及以下
                    //使用传统文件读写+广播刷新
                    saveToLegacyStorage(context, bitmap, filename)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                showToast(context, "保存出错: ${e.message}")
            }
        }
    }

    //Android10+保存逻辑
    private suspend fun saveToMediaStore(context: Context, bitmap: Bitmap, filename: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/HeyWear")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        if (imageUri != null) {
            resolver.openOutputStream(imageUri).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos!!)
            }

            //写入完成，标记为可见 (IS_PENDING = 0)
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

            showToast(context, "✅ 已保存至图库")
        } else {
            throw Exception("MediaStore Uri is null")
        }
    }

    // Android9及以下保存逻辑
    private suspend fun saveToLegacyStorage(context: Context, bitmap: Bitmap, filename: String) {
        // 存到 /sdcard/Pictures/HeyWear
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val heyWearDir = File(picturesDir, "HeyWear")

        if (!heyWearDir.exists()) {
            heyWearDir.mkdirs()
        }

        val imageFile = File(heyWearDir, filename)
        val fos = FileOutputStream(imageFile)

        fos.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            arrayOf("image/jpeg")
        ) { path, uri ->
            // 扫描完成后回调
            // Log.d("ImageSaver", "Scanned $path -> $uri")
        }

        showToast(context, "✅ 已保存图片")
    }

    private suspend fun showToast(context: Context, msg: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}