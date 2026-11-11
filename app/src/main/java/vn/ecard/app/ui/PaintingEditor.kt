package vn.ecard.app.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vn.ecard.app.render.Aspect
import vn.ecard.app.render.CardFont
import vn.ecard.app.render.Ink
import vn.ecard.app.render.Motif
import vn.ecard.app.render.PaintLayout
import vn.ecard.app.render.PaintPresets
import vn.ecard.app.render.PaintingRenderer
import vn.ecard.app.render.PaintingSpec
import vn.ecard.app.render.Paper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintingEditor(layout: PaintLayout, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var spec by remember { mutableStateOf(PaintPresets.byLayout(layout).first().toSpec()) }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }

    BackHandler(onBack = onClose)

    LaunchedEffect(spec) {
        val bmp = withContext(Dispatchers.Default) { PaintingRenderer.render(spec, 0.5f) }
        preview = bmp.asImageBitmap()
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val bmp = withContext(Dispatchers.IO) { CardFiles.loadPhoto(ctx, uri) }
            if (bmp != null) spec = spec.copy(photo = bmp) else Toast.makeText(ctx, "Không đọc được ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    fun export(share: Boolean) {
        if (busy) return
        busy = true
        scope.launch {
            val full: Bitmap = withContext(Dispatchers.Default) { PaintingRenderer.render(spec, 1f) }
            withContext(Dispatchers.IO) { CardFiles.save(ctx, full) }
            if (share) CardFiles.shareBitmap(ctx, full)
            else Toast.makeText(ctx, "Đã lưu vào Thiệp đã lưu & Thư viện ảnh (Pictures/eCard)", Toast.LENGTH_LONG).show()
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(EC.Bg)) {
        TopAppBar(
            title = { Text("Tranh thư pháp · " + spec.layout.label, maxLines = 1, fontSize = 17.sp) },
            navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            actions = {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                IconButton(onClick = { export(false) }) { Icon(Icons.Filled.Download, "Lưu") }
                IconButton(onClick = { export(true) }) { Icon(Icons.Filled.Share, "Chia sẻ", tint = EC.Gold) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EC.Bg)
        )
        Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            val img = preview
            if (img != null) Image(
                img, "Xem trước tranh", contentScale = ContentScale.Fit,
                modifier = Modifier.aspectRatio(spec.aspect.w.toFloat() / spec.aspect.h).border(1.dp, EC.Gold.copy(alpha = 0.3f))
            ) else CircularProgressIndicator()
        }
        val tabs = listOf("Mẫu câu", "Bố cục · Họa tiết", "Chữ", "Ảnh nền")
        ScrollableTabRow(selectedTabIndex = tab, containerColor = EC.Surface, edgePadding = 8.dp) {
            tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
        }
        Column(
            Modifier.fillMaxWidth().heightIn(max = 320.dp).background(EC.Surface)
                .verticalScroll(rememberScrollState()).padding(12.dp)
        ) {
            when (tab) {
                0 -> PresetTab(spec) { spec = it }
                1 -> LayoutTab(spec) { spec = it }
                2 -> TextTabP(spec) { spec = it }
                3 -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("🖼️  Chọn ảnh phong cảnh") }
                        if (spec.photo != null) OutlinedButton(onClick = { spec = spec.copy(photo = null) }) { Text("Bỏ ảnh") }
                    }
                    if (spec.photo != null) {
                        Label("Độ tối của ảnh")
                        Slider(value = spec.photoDim, onValueChange = { spec = spec.copy(photoDim = it) }, valueRange = 0f..0.8f)
                    }
                    Text("Ảnh phong cảnh (núi, sông, đá, hoa…) kết hợp chữ thư pháp màu sáng. Ảnh chỉ dùng trong máy.",
                        color = EC.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun PresetTab(spec: PaintingSpec, onChange: (PaintingSpec) -> Unit) {
    PaintLayout.values().forEach { l ->
        Label(l.icon + " " + l.label)
        PaintPresets.byLayout(l).forEach { p ->
            val sel = spec.headline == p.headline && spec.layout == p.layout
            Column(
                Modifier.fillMaxWidth().padding(bottom = 6.dp)
                    .background(if (sel) EC.Gold.copy(alpha = 0.18f) else EC.Surface2, RoundedCornerShape(10.dp))
                    .border(1.dp, if (sel) EC.Gold else EC.Surface2, RoundedCornerShape(10.dp))
                    .clickable { onChange(p.toSpec(spec)) }
                    .padding(10.dp)
            ) {
                Text(p.name, color = EC.Gold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(p.body.replace("\n", " · ").take(90), color = EC.Text, fontSize = 12.sp, maxLines = 2)
                if (p.source.isNotBlank()) Text("Nguồn: " + p.source, color = EC.Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun LayoutTab(spec: PaintingSpec, onChange: (PaintingSpec) -> Unit) {
    Label("Bố cục")
    ChipRow { PaintLayout.values().forEach { l -> Chip(spec.layout == l, l.icon + " " + l.label) { onChange(spec.copy(layout = l)) } } }
    Label("Họa tiết thủy mặc")
    ChipRow { Motif.values().forEach { m -> Chip(spec.motif == m, m.label) { onChange(spec.copy(motif = m)) } } }
    Label("Nền")
    ChipRow { Paper.values().forEach { p -> Chip(spec.paper == p, p.label) { onChange(spec.copy(paper = p)) } } }
    Label("Khổ tranh")
    ChipRow { Aspect.values().forEach { a -> Chip(spec.aspect == a, a.label) { onChange(spec.copy(aspect = a)) } } }
}

@Composable
private fun TextTabP(spec: PaintingSpec, onChange: (PaintingSpec) -> Unit) {
    val step = spec.layout == PaintLayout.VN_STEP
    val han = spec.layout == PaintLayout.HAN_VERTICAL
    Label(if (step) "Chữ lớn (nhiều cụm cách nhau bằng dấu /)" else if (han) "Chữ lớn viết dọc (chữ Hán)" else "Chữ lớn (1–2 từ)")
    OutlinedTextField(spec.headline, { onChange(spec.copy(headline = it.take(24))) }, Modifier.fillMaxWidth(), singleLine = true)
    Label(if (step) "Các dòng nhỏ (mỗi nhóm cách nhau 1 dòng trống)" else if (han) "Đoạn chữ nhỏ (tự chia cột theo dấu câu)" else "Các dòng thơ")
    OutlinedTextField(spec.body, { onChange(spec.copy(body = it.take(400))) }, Modifier.fillMaxWidth(), minLines = 3, maxLines = 8)
    Label("Nguồn / ký tên (không bắt buộc)")
    OutlinedTextField(spec.source, { onChange(spec.copy(source = it.take(40))) }, Modifier.fillMaxWidth(), singleLine = true)
    Label("Chữ trên ấn son (1 chữ)")
    OutlinedTextField(spec.seal, { onChange(spec.copy(seal = it.take(1))) }, Modifier.fillMaxWidth(), singleLine = true)
    Label("Màu mực chữ lớn")
    ChipRow { Ink.values().forEach { i -> Chip(spec.headInk == i, i.label) { onChange(spec.copy(headInk = i)) } } }
    Label("Màu mực chữ nhỏ")
    ChipRow { Ink.values().forEach { i -> Chip(spec.bodyInk == i, i.label) { onChange(spec.copy(bodyInk = i)) } } }
    if (!han) {
        val callis = CardFont.values().filter { it.calligraphy }
        Label("Kiểu chữ lớn")
        ChipRow { callis.forEach { f -> Chip(spec.headFont == f, f.label) { onChange(spec.copy(headFont = f)) } } }
        Label("Kiểu chữ nhỏ")
        ChipRow { callis.forEach { f -> Chip(spec.bodyFont == f, f.label) { onChange(spec.copy(bodyFont = f)) } } }
    }
    Label("Cỡ chữ")
    Slider(value = spec.textScale, onValueChange = { onChange(spec.copy(textScale = it)) }, valueRange = 0.6f..1.4f)
}
