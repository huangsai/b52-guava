package com.mobile.guava.android.io

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.WorkerThread
import com.mobile.guava.android.ensureWorkThread
import okhttp3.internal.io.FileSystem
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

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
        val timeStamp: String = System.currentTimeMillis().toString()
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        val file = File(storageDir, "${prefix + timeStamp}.jpg")
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    @WorkerThread
    fun saveImage(context: Context, uri: Uri): String {
        ensureWorkThread()
        var sPath = ""
        val filename = uri.lastPathSegment!!
        val extension = filename.split(".").last()

        var fos: OutputStream?
        val conValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/$extension")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            conValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri: Uri? = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                conValues
            )
            fos = imageUri?.let { context.contentResolver.openOutputStream(it, "w") }
            fos?.use { outputStream ->
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    FileUtils.transfer(inputStream, outputStream, 1024)
                }
                conValues.clear()
                conValues.put(MediaStore.Images.Media.IS_PENDING, 1)
                context.contentResolver.update(imageUri!!, conValues, null, null)

                sPath = Environment.DIRECTORY_PICTURES + File.separator + filename
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val imageFile = File(imagesDir, filename)
            fos = FileOutputStream(imageFile)
            fos?.use { outputStream ->
                context.contentResolver.openInputStream(uri).use { inputStream ->
                    FileUtils.transfer(inputStream, outputStream, 1024)
                }
                sPath = imageFile.absolutePath
            }

            conValues.put(MediaStore.MediaColumns.RELATIVE_PATH, imageFile.absolutePath)
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                conValues
            )
            context.sendBroadcast(
                Intent(
                    "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                    Uri.parse("file://" + imageFile.absolutePath)
                )
            )
        }
        return sPath
    }

    @WorkerThread
    fun saveImage(context: Context, bitmap: Bitmap): String {
        ensureWorkThread()
        var sPath = ""
        val timeStamp: String = System.currentTimeMillis().toString()
        val filename = "$timeStamp.$IMAGE_EXTENSION_JPG"

        var fos: OutputStream?
        val conValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/$IMAGE_EXTENSION_JPG")
            put(MediaStore.MediaColumns.SIZE, bitmap.byteCount)
            put(MediaStore.MediaColumns.WIDTH, bitmap.width)
            put(MediaStore.MediaColumns.HEIGHT, bitmap.height)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            conValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri: Uri? = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                conValues
            )
            fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it)
                sPath = Environment.DIRECTORY_PICTURES + File.separator + filename
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val imageFile = File(imagesDir, filename)
            fos = FileOutputStream(imageFile)
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it)
                sPath = imageFile.absolutePath
            }
            conValues.put(MediaStore.MediaColumns.RELATIVE_PATH, imageFile.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, conValues)
            context.sendBroadcast(
                Intent(
                    "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                    Uri.parse("file://" + imageFile.absolutePath)
                )
            )
        }

        return sPath
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