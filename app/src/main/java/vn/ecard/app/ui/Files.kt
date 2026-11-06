package vn.ecard.app.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object CardFiles {

    fun cardsDir(ctx: Context): File = File(ctx.filesDir, "cards").apply { mkdirs() }

    fun listSaved(ctx: Context): List<File> =
        cardsDir(ctx).listFiles { f -> f.extension == "png" }?.sortedByDescending { it.lastModified() } ?: emptyList()

    /** Lưu vào bộ sưu tập trong app + thư viện ảnh của máy (Pictures/eCard). */
    fun save(ctx: Context, bmp: Bitmap): File {
        val name = "eCard_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".png"
        val f = File(cardsDir(ctx), name)
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/eCard")
            }
            val uri = ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) ctx.contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        return f
    }

    fun shareBitmap(ctx: Context, bmp: Bitmap, text: String = "") {
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        val f = File(dir, "eCard.png")
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        shareFile(ctx, f, text)
    }

    fun shareFile(ctx: Context, f: File, text: String = "") {
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            if (text.isNotBlank()) putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Chia sẻ thiệp qua…").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(chooser)
    }

    /** Đọc ảnh người dùng chọn, thu nhỏ tối đa 1800px, bitmap phần mềm để vẽ được lên Canvas. */
    fun loadPhoto(ctx: Context, uri: Uri): Bitmap? = runCatching {
        val src = ImageDecoder.createSource(ctx.contentResolver, uri)
        ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val big = max(info.size.width, info.size.height)
            if (big > 1800) {
                val r = 1800f / big
                decoder.setTargetSize((info.size.width * r).toInt(), (info.size.height * r).toInt())
            }
        }
    }.getOrNull()
}
