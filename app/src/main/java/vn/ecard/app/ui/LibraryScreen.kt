package vn.ecard.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.ecard.app.data.Category
import vn.ecard.app.data.Lang
import vn.ecard.app.data.Occasion
import vn.ecard.app.data.Phrase
import vn.ecard.app.data.PhraseLibrary

@Composable
fun LibraryScreen(onUse: (Phrase) -> Unit) {
    val ctx = LocalContext.current
    var cat by remember { mutableIntStateOf(0) }
    var occ by remember { mutableStateOf<Occasion?>(null) }
    var query by remember { mutableStateOf("") }
    val category = Category.values()[cat]
    val list = remember(cat, occ, query) {
        val base = if (occ != null) PhraseLibrary.byOccasion(occ!!) else PhraseLibrary.byCategory(category)
        val q = query.trim().lowercase()
        if (q.isEmpty()) base else base.filter {
            it.text.vi.lowercase().contains(q) || it.text.en.lowercase().contains(q) || it.text.zh.contains(q)
        }
    }

    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = cat, containerColor = EC.Surface, edgePadding = 8.dp) {
            Category.values().forEachIndexed { i, c ->
                Tab(selected = cat == i, onClick = { cat = i; occ = null }, text = {
                    Text("${c.label} (${PhraseLibrary.byCategory(c).size})")
                })
            }
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            placeholder = { Text("Tìm lời chúc (Việt / Anh / 中文)…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val occs = Occasion.values().filter { it.category == category }
            if (occs.size > 1) {
                FilterChip(selected = occ == null, onClick = { occ = null }, label = { Text("Tất cả") }, colors = chipColors())
                occs.forEach { o ->
                    FilterChip(selected = occ == o, onClick = { occ = o }, label = { Text(o.icon + " " + o.label, fontSize = 13.sp) }, colors = chipColors())
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(list, key = { it.id }) { ph ->
                PhraseCard(ph, footer = {
                    Row {
                        TextButton(onClick = { onUse(ph) }) { Text("✍️ Tạo thiệp") }
                        Lang.values().forEach { l ->
                            TextButton(onClick = { copy(ctx, ph.text.get(l)) }) { Text("Chép ${l.name}", fontSize = 12.sp) }
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun chipColors() =
    FilterChipDefaults.filterChipColors(selectedContainerColor = EC.Gold, selectedLabelColor = Color(0xFF2A1E00))

private fun copy(ctx: Context, text: String) {
    val cm = ctx.getSystemService(ClipboardManager::class.java)
    cm.setPrimaryClip(ClipData.newPlainText("eCard", text))
    Toast.makeText(ctx, "Đã chép", Toast.LENGTH_SHORT).show()
}
