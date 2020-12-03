package com.mobile.guava.android.io

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import com.mobile.guava.android.context.AppUtils
import com.mobile.guava.android.ensureWorkThread
import okhttp3.internal.io.FileSystem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object DiskOperations {

    private const val QUALITY = 100
    private const val DATA_PATTERN = "yyyyMMdd_HHmmss"
    private const val IMAGE_EXTENSION_JPG = "jpg"
    private const val IMAGE_EXTENSION_PNG = "png"

    const val FLAG_NO_CACHE = "No_Cache_"
    const val FLAG_CACHE = "Cache_"

    @WorkerThread
    fun clearExternalFiles(context: Context, type: String, prefix: String) {
        ensureWorkThread()
        val storageDir: File = context.getExternalFilesDir(type)!!
        if (storageDir.isDirectory) {
            storageDir.listFiles().forEach {
                if (it.name.startsWith(prefix)) {
                    FileSystem.SYSTEM.delete(it)
                }
            }
        }
    }

    @WorkerThread
    fun createImageFile(context: Context, prefix: String): File {
        ensureWorkThread()
        val timeStamp: String = SimpleDateFormat(DATA_PATTERN, Locale.US).format(Date())
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "${prefix + timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    @WorkerThread
    fun saveImage(context: Context, uri: Uri) {
        ensureWorkThread()
        context.contentResolver.openInputStream(uri).use { inputStream ->
            val collection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            val dirDest = File(Environment.DIRECTORY_PICTURES, AppUtils.getName(context))
            val timeStamp: String = SimpleDateFormat(DATA_PATTERN, Locale.US).format(Date())
            val extension = IMAGE_EXTENSION_JPG

            val newImage = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$timeStamp.$extension")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
                put(MediaStore.MediaColumns.DATE_ADDED, timeStamp)
                put(MediaStore.MediaColumns.DATE_MODIFIED, timeStamp)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest${File.separator}")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            context.contentResolver.insert(collection, newImage)?.let { newImageUri ->
                context.contentResolver.openOutputStream(newImageUri, "w").use { outputStream ->
                    FileUtils.transfer(inputStream, outputStream, 1024)
                }
                newImage.clear()
                newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(newImageUri, newImage, null, null)
            }
        }
    }

    @WorkerThread
    fun saveImage(
        context: Context,
        bitmap: Bitmap,
        format: Bitmap.CompressFormat
    ) {
        ensureWorkThread()
        val collection = MediaStore.Images.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL_PRIMARY
        )
        val dirDest = File(Environment.DIRECTORY_PICTURES, AppUtils.getName(context))
        val timeStamp: String = SimpleDateFormat(DATA_PATTERN, Locale.US).format(Date())
        val extension = getImageExtension(format)

        val newImage = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$timeStamp.$extension")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
            put(MediaStore.MediaColumns.DATE_ADDED, timeStamp)
            put(MediaStore.MediaColumns.DATE_MODIFIED, timeStamp)
            put(MediaStore.MediaColumns.SIZE, bitmap.byteCount)
            put(MediaStore.MediaColumns.WIDTH, bitmap.width)
            put(MediaStore.MediaColumns.HEIGHT, bitmap.height)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirDest${File.separator}")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        context.contentResolver.insert(collection, newImage)?.let { newImageUri ->
            context.contentResolver.openOutputStream(newImageUri, "w").use {
                bitmap.compress(format, QUALITY, it)
            }
            newImage.clear()
            newImage.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(newImageUri, newImage, null, null)
        }
    }

    private fun getImageFormat(type: String): Bitmap.CompressFormat {
        return when (type) {
            Bitmap.CompressFormat.PNG.name -> Bitmap.CompressFormat.PNG
            Bitmap.CompressFormat.JPEG.name -> Bitmap.CompressFormat.JPEG
            Bitmap.CompressFormat.WEBP.name -> Bitmap.CompressFormat.WEBP
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    when (type) {
                        Bitmap.CompressFormat.WEBP_LOSSY.name -> Bitmap.CompressFormat.WEBP_LOSSY
                        Bitmap.CompressFormat.WEBP_LOSSLESS.name -> Bitmap.CompressFormat.WEBP_LOSSLESS
                        else -> Bitmap.CompressFormat.JPEG
                    }
                } else {
                    Bitmap.CompressFormat.JPEG
                }
            }
        }
    }

    private fun getImageExtension(format: Bitmap.CompressFormat): String {
        return when (format) {
            Bitmap.CompressFormat.PNG -> IMAGE_EXTENSION_PNG
            Bitmap.CompressFormat.JPEG -> IMAGE_EXTENSION_JPG
            else -> IMAGE_EXTENSION_JPG
        }
    }
}