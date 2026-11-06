package vn.ecard.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun GalleryScreen() {
    val ctx = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var files by remember { mutableStateOf<List<File>>(emptyList()) }
    var open by remember { mutableStateOf<File?>(null) }
    LaunchedEffect(refresh) { files = withContext(Dispatchers.IO) { CardFiles.listSaved(ctx) } }

    if (files.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Chưa có thiệp nào.\nTạo thiệp rồi bấm Lưu hoặc Chia sẻ, thiệp sẽ xuất hiện ở đây.", color = EC.Muted, textAlign = TextAlign.Center)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files, key = { it.name }) { f ->
                Thumb(f, Modifier.clickable { open = f })
            }
        }
    }

    open?.let { f ->
        AlertDialog(
            onDismissRequest = { open = null },
            text = { Thumb(f, Modifier, full = true) },
            confirmButton = {
                TextButton(onClick = { CardFiles.shareFile(ctx, f) }) { Text("Chia sẻ") }
            },
            dismissButton = {
                TextButton(onClick = { f.delete(); open = null; refresh++ }) { Text("Xoá", color = Color(0xFFFF8A80)) }
            }
        )
    }
}

@Composable
private fun Thumb(f: File, modifier: Modifier, full: Boolean = false) {
    val img by produceState<ImageBitmap?>(null, f) {
        value = withContext(Dispatchers.IO) {
            val opts = BitmapFactory.Options().apply { inSampleSize = if (full) 2 else 4 }
            BitmapFactory.decodeFile(f.absolutePath, opts)?.asImageBitmap()
        }
    }
    val ratio = img?.let { it.width.toFloat() / it.height } ?: 0.8f
    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .clip(RoundedCornerShape(8.dp))
            .background(EC.Surface2)
    ) {
        img?.let { Image(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
    }
}
