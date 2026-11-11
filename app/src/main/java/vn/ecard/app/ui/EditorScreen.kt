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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
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
import vn.ecard.app.data.Lang
import vn.ecard.app.data.Occasion
import vn.ecard.app.data.Phrase
import vn.ecard.app.data.PhraseLibrary
import vn.ecard.app.render.Aspect
import vn.ecard.app.render.CardFont
import vn.ecard.app.render.CardRenderer
import vn.ecard.app.render.CardSpec
import vn.ecard.app.render.CardStyle
import vn.ecard.app.render.Tone
import vn.ecard.app.render.ThemeFrames
import vn.ecard.app.render.BIG_WORDS

private fun specFor(o: Occasion, phrase: Phrase?, old: CardSpec? = null): CardSpec {
    val ph = phrase ?: PhraseLibrary.byOccasion(o).firstOrNull()
    val base = old ?: CardSpec(occasion = o, langs = listOf(Lang.VI, Lang.ZH))
    return base.copy(
        occasion = o,
        tone = if (old == null || old.tone == Tone.auto(old.occasion)) Tone.auto(o) else old.tone,
        frame = if (ThemeFrames.has(o)) (if (old != null && old.frame > 0 && ThemeFrames.has(old.occasion)) old.frame else 1) else 0,
        title = Lang.values().associateWith { o.title.get(it) },
        message = if (ph != null) Lang.values().associateWith { ph.text.get(it) } else emptyMap()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(occasion: Occasion, phrase: Phrase?, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var spec by remember { mutableStateOf(specFor(occasion, phrase)) }
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    var showPicker by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    BackHandler(onBack = onClose)

    LaunchedEffect(spec) {
        val bmp = withContext(Dispatchers.Default) { CardRenderer.render(spec, 0.5f) }
        preview = bmp.asImageBitmap()
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            val bmp = withContext(Dispatchers.IO) { CardFiles.loadPhoto(ctx, uri) }
            if (bmp != null) spec = spec.copy(photo = bmp) else Toast.makeText(ctx, "Không đọc được ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    fun export(action: (Bitmap) -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            val full = withContext(Dispatchers.Default) { CardRenderer.render(spec, 1f) }
            withContext(Dispatchers.IO) { action(full) }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(EC.Bg)) {
        TopAppBar(
            title = { Text(spec.occasion.title.vi, maxLines = 1, fontSize = 18.sp) },
            navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            actions = {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                IconButton(onClick = {
                    export {
                        CardFiles.save(ctx, it)
                        scope.launch(Dispatchers.Main) { Toast.makeText(ctx, "Đã lưu vào Thiệp đã lưu & Thư viện ảnh (Pictures/eCard)", Toast.LENGTH_LONG).show() }
                    }
                }) { Icon(Icons.Filled.Download, "Lưu") }
                IconButton(onClick = {
                    export {
                        CardFiles.save(ctx, it)
                        scope.launch(Dispatchers.Main) { CardFiles.shareBitmap(ctx, it) }
                    }
                }) { Icon(Icons.Filled.Share, "Chia sẻ", tint = EC.Gold) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = EC.Bg)
        )

        // Xem trước
        Box(
            Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val img = preview
            if (img != null) {
                Image(
                    img, "Xem trước thiệp",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .aspectRatio(spec.aspect.w.toFloat() / spec.aspect.h)
                        .border(1.dp, EC.Gold.copy(alpha = 0.3f))
                )
            } else CircularProgressIndicator()
        }

        val tabs = listOf("Mẫu thiệp", "Lời chúc", "Ngôn ngữ", "Chữ", "Ảnh nền")
        ScrollableTabRow(selectedTabIndex = tab, containerColor = EC.Surface, edgePadding = 8.dp) {
            tabs.forEachIndexed { i, t -> Tab(selected = tab == i, onClick = { tab = i }, text = { Text(t) }) }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
                .background(EC.Surface)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            when (tab) {
                0 -> TemplateTab(spec) { spec = it }
                1 -> ContentTab(spec, onChange = { spec = it }, onPick = { showPicker = true })
                2 -> LangTab(spec) { spec = it }
                3 -> TextTab(spec) { spec = it }
                4 -> PhotoTab(
                    spec,
                    onPick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onChange = { spec = it }
                )
            }
        }
    }

    if (showPicker) {
        PhrasePicker(spec.occasion, onDismiss = { showPicker = false }) { ph ->
            spec = spec.copy(message = Lang.values().associateWith { ph.text.get(it) })
            showPicker = false
        }
    }
}

@Composable
internal fun Label(t: String) =
    Text(t, color = EC.Gold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp, bottom = 4.dp))

@Composable
internal fun ChipRow(content: @Composable () -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { content() }
}

@Composable
internal fun Chip(selected: Boolean, text: String, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(text, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EC.Gold, selectedLabelColor = Color(0xFF2A1E00))
    )
}

@Composable
private fun TemplateTab(spec: CardSpec, onChange: (CardSpec) -> Unit) {
    if (ThemeFrames.has(spec.occasion)) {
        Label("Khung theo chủ đề")
        val labels = ThemeFrames.labels(spec.occasion)
        val frameThumbs = remember(spec.occasion, spec.aspect) {
            (1..labels.size).associateWith {
                CardRenderer.render(spec.copy(frame = it, photo = null, message = emptyMap(), signature = "", bigWord = "", langs = listOf(spec.langs.firstOrNull() ?: Lang.VI)), 0.12f).asImageBitmap()
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            labels.forEachIndexed { idx, name ->
                val f = idx + 1
                val sel = spec.frame == f && spec.photo == null
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(86.dp).clickable { onChange(spec.copy(frame = f, photo = null)) }) {
                    Image(
                        frameThumbs.getValue(f), name,
                        modifier = Modifier
                            .width(78.dp)
                            .aspectRatio(spec.aspect.w.toFloat() / spec.aspect.h)
                            .border(if (sel) 3.dp else 1.dp, if (sel) EC.Gold else EC.Muted.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    )
                    Text(name, fontSize = 11.sp, color = if (sel) EC.Gold else EC.Text, maxLines = 2, lineHeight = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
    Label(if (ThemeFrames.has(spec.occasion)) "Hoặc phong cách chung" else "Phong cách")
    // Ảnh thu nhỏ của 4 phong cách
    val thumbs = remember(spec.occasion, spec.tone, spec.aspect, spec.photo) {
        CardStyle.values().associateWith {
            CardRenderer.render(spec.copy(style = it, frame = 0, message = emptyMap(), signature = "", bigWord = "", langs = listOf(spec.langs.firstOrNull() ?: Lang.VI)), 0.12f).asImageBitmap()
        }
    }
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CardStyle.values().forEach { st ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onChange(spec.copy(style = st, frame = 0)) }) {
                Image(
                    thumbs.getValue(st), st.label,
                    modifier = Modifier
                        .width(78.dp)
                        .aspectRatio(spec.aspect.w.toFloat() / spec.aspect.h)
                        .border(if (spec.style == st && spec.frame == 0) 3.dp else 1.dp, if (spec.style == st && spec.frame == 0) EC.Gold else EC.Muted.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                )
                Text(st.short, fontSize = 12.sp, color = if (spec.style == st && spec.frame == 0) EC.Gold else EC.Text)
            }
        }
    }
    Label("Dịp")
    ChipRow {
        Occasion.values().forEach { o ->
            Chip(spec.occasion == o, o.icon + " " + o.label) { onChange(specFor(o, null, spec)) }
        }
    }
    Label("Bảng màu")
    ChipRow {
        Tone.values().forEach { t ->
            Chip(spec.tone == t, t.label + if (t == Tone.auto(spec.occasion)) " ★" else "") { onChange(spec.copy(tone = t)) }
        }
    }
    if (spec.occasion.solemn && spec.tone != Tone.SOLEMN) {
        Text("Lưu ý: thiệp chia buồn nên dùng tông Trắng – Đen.", color = EC.Muted, fontSize = 12.sp)
    }
    Label("Khổ thiệp")
    ChipRow { Aspect.values().forEach { a -> Chip(spec.aspect == a, a.label) { onChange(spec.copy(aspect = a)) } } }
}

@Composable
private fun ContentTab(spec: CardSpec, onChange: (CardSpec) -> Unit, onPick: () -> Unit) {
    Button(onClick = onPick, modifier = Modifier.fillMaxWidth()) { Text("📖  Chọn lời từ thư viện (Việt · Anh · Trung)") }
    spec.langs.forEach { l ->
        Label("Tiêu đề – ${l.label}")
        OutlinedTextField(
            value = spec.title[l].orEmpty(),
            onValueChange = { v -> onChange(spec.copy(title = spec.title + (l to v))) },
            modifier = Modifier.fillMaxWidth(), singleLine = false, maxLines = 2
        )
        Label("Lời nhắn – ${l.label}")
        OutlinedTextField(
            value = spec.message[l].orEmpty(),
            onValueChange = { v -> onChange(spec.copy(message = spec.message + (l to v))) },
            modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 6
        )
    }
    Label("Người gửi (ký tên)")
    OutlinedTextField(
        value = spec.signature, onValueChange = { onChange(spec.copy(signature = it)) },
        modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Ví dụ: Gia đình Hòa") }
    )
}

@Composable
private fun LangTab(spec: CardSpec, onChange: (CardSpec) -> Unit) {
    Label("Ngôn ngữ hiển thị trên thiệp (chọn 1–3)")
    ChipRow {
        Lang.values().forEach { l ->
            val on = l in spec.langs
            Chip(on, l.label) {
                val next = if (on) spec.langs - l else spec.langs + l
                if (next.isNotEmpty()) onChange(spec.copy(langs = next))
            }
        }
    }
    Label("Ngôn ngữ chính (chữ to nhất, đứng đầu)")
    ChipRow {
        spec.langs.forEach { l ->
            Chip(spec.langs.first() == l, l.label) { onChange(spec.copy(langs = listOf(l) + (spec.langs - l))) }
        }
    }
    Text(
        "Tiếng Trung dùng chữ Phồn thể (繁體). Mỗi câu trong thư viện đều có đủ 3 ngôn ngữ.",
        color = EC.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)
    )
}

private val swatches = listOf(
    null, 0xFFFFFFFF, 0xFF000000, 0xFFE8C25A, 0xFFC62828, 0xFF1B3556, 0xFF6B1235, 0xFF14603A, 0xFF5A0E0E
)

@Composable
private fun TextTab(spec: CardSpec, onChange: (CardSpec) -> Unit) {
    Label("Kiểu chữ")
    ChipRow { CardFont.values().filter { !it.calligraphy }.forEach { f -> Chip(spec.font == f, f.label) { onChange(spec.copy(font = f)) } } }
    Label("Thư pháp tiếng Việt")
    ChipRow { CardFont.values().filter { it.calligraphy }.forEach { f -> Chip(spec.font == f, "✒️ " + f.label) { onChange(spec.copy(font = f)) } } }
    Label("Chữ thư pháp lớn (ô giấy dó + ấn đỏ)")
    ChipRow {
        Chip(spec.bigWord.isBlank(), "Không") { onChange(spec.copy(bigWord = "")) }
        BIG_WORDS.forEach { wd -> Chip(spec.bigWord == wd, wd) { onChange(spec.copy(bigWord = wd)) } }
    }
    OutlinedTextField(
        value = spec.bigWord, onValueChange = { onChange(spec.copy(bigWord = it.take(8))) },
        modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Hoặc tự gõ chữ (tối đa 8 ký tự)") }
    )
    Label("Cỡ chữ")
    Slider(value = spec.textScale, onValueChange = { onChange(spec.copy(textScale = it)) }, valueRange = 0.6f..1.5f)
    Label("Vị trí chữ (lên – xuống)")
    Slider(value = spec.textOffset, onValueChange = { onChange(spec.copy(textOffset = it)) }, valueRange = -1f..1f)
    Label("Màu chữ")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
        swatches.forEach { sw ->
            val selected = spec.textColor == sw?.toInt()
            Box(
                Modifier
                    .size(36.dp)
                    .background(if (sw == null) EC.Surface2 else Color(sw), CircleShape)
                    .border(if (selected) 3.dp else 1.dp, if (selected) EC.Gold else EC.Muted, CircleShape)
                    .clickable { onChange(spec.copy(textColor = sw?.toInt())) },
                contentAlignment = Alignment.Center
            ) { if (sw == null) Text("Tự", fontSize = 11.sp, color = EC.Text) }
        }
    }
}

@Composable
private fun PhotoTab(spec: CardSpec, onPick: () -> Unit, onChange: (CardSpec) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onPick) { Text("🖼️  Chọn ảnh của bạn") }
        if (spec.photo != null) OutlinedButton(onClick = { onChange(spec.copy(photo = null)) }) { Text("Dùng mẫu có sẵn") }
    }
    if (spec.photo != null) {
        Label("Độ tối của ảnh (giúp chữ dễ đọc)")
        Slider(value = spec.photoDim, onValueChange = { onChange(spec.copy(photoDim = it)) }, valueRange = 0f..0.8f)
        Text("Khung viền theo phong cách đang chọn vẫn được giữ trên ảnh.", color = EC.Muted, fontSize = 12.sp)
    } else {
        Text("Ảnh sẽ tự cắt vừa khổ thiệp. Ảnh chỉ dùng trong máy, không gửi đi đâu.", color = EC.Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
fun PhrasePicker(occasion: Occasion, onDismiss: () -> Unit, onPick: (Phrase) -> Unit) {
    var showAll by remember { mutableStateOf(false) }
    val list = remember(showAll, occasion) {
        if (showAll) PhraseLibrary.byCategory(occasion.category) + PhraseLibrary.byOccasion(Occasion.WISDOM)
        else PhraseLibrary.byOccasion(occasion)
    }.distinct()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
        dismissButton = {
            TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "Chỉ dịp này" else "Xem thêm cùng nhóm") }
        },
        title = { Text("${occasion.icon} ${occasion.title.vi}") },
        text = {
            LazyColumn(Modifier.heightIn(max = 460.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list) { ph -> PhraseCard(ph, onClick = { onPick(ph) }) }
            }
        }
    )
}

@Composable
fun PhraseCard(ph: Phrase, onClick: (() -> Unit)? = null, footer: (@Composable () -> Unit)? = null) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(EC.Surface2, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp)
    ) {
        Text(ph.text.vi, color = EC.Text, fontSize = 15.sp)
        Spacer(Modifier.height(4.dp))
        Text(ph.text.en, color = EC.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        Text(ph.text.zh, color = EC.Gold, fontSize = 15.sp)
        if (ph.source != null) Text("Nguồn: ${ph.source}", color = EC.Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
        footer?.invoke()
    }
}
