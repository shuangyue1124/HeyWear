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
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageSaver {
    internal const val MAX_IMAGE_DIMENSION = 1280
    private const val JPEG_QUALITY = 92

    suspend fun saveImage(context: Context, url: String) {
        withContext(Dispatchers.IO) {
            try {
                // 1. 下载图片
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .size(MAX_IMAGE_DIMENSION)
                    .scale(Scale.FIT)
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

            } catch (_: OutOfMemoryError) {
                FileLogger.write(context, "ImageSave", "OutOfMemoryError")
                showToast(context, "图片过大，无法保存")
            } catch (e: Exception) {
                FileLogger.write(context, "ImageSave", e.javaClass.simpleName)
                showToast(context, "保存失败")
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
        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw IOException("MediaStore insert failed")

        try {
            val encoded = resolver.openOutputStream(imageUri)?.use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            } ?: false
            if (!encoded) {
                throw IOException("Image encoding failed")
            }

            //写入完成，标记为可见 (IS_PENDING = 0)
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            if (resolver.update(imageUri, contentValues, null, null) <= 0) {
                throw IOException("MediaStore publish failed")
            }

            showToast(context, "✅ 已保存至图库")
        } catch (e: Exception) {
            runCatching { resolver.delete(imageUri, null, null) }
            throw e
        }
    }

    // Android9及以下保存逻辑
    private suspend fun saveToLegacyStorage(context: Context, bitmap: Bitmap, filename: String) {
        // 存到 /sdcard/Pictures/HeyWear
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val heyWearDir = File(picturesDir, "HeyWear")

        if (!heyWearDir.exists() && !heyWearDir.mkdirs()) {
            throw IOException("Cannot create image directory")
        }

        val imageFile = File(heyWearDir, filename)
        try {
            FileOutputStream(imageFile).use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                    throw IOException("Image encoding failed")
                }
            }
        } catch (e: Exception) {
            imageFile.delete()
            throw e
        }

        MediaScannerConnection.scanFile(
            context,
            arrayOf(imageFile.absolutePath),
            arrayOf("image/jpeg")
        ) { _, _ -> }

        showToast(context, "✅ 已保存图片")
    }

    private suspend fun showToast(context: Context, msg: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
