package vn.ecard.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import vn.ecard.app.R
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.ecard.app.data.CalEvent
import vn.ecard.app.data.Category
import vn.ecard.app.data.Occasion

const val PRIVACY_URL = "https://nguyenvanhoa1912-lasotuvi.github.io/eCard/privacy-policy.html"

@Composable
fun HomeScreen(upcoming: List<CalEvent>, onOpen: (Occasion, String?) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF8E1B1B), Color(0xFF3A0A0A))),
                        RoundedCornerShape(18.dp)
                    )
                    .border(1.dp, EC.Gold.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painterResource(R.drawable.ic_launcher_foreground), "Logo eCard",
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFAF7F1)).scale(1.45f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("eCard – Tự Tạo Thiệp", color = EC.Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Đông · Tây · Kim · Cổ", color = EC.Text.copy(alpha = 0.85f), fontSize = 13.sp)
                        Text("Việt · English · 中文", color = EC.Text.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }
                if (upcoming.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Sắp tới", color = EC.Gold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    upcoming.take(3).forEach { e ->
                        val left = e.daysLeft()
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .clickable { onOpen(e.occasion, e.name) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(e.type.icon, fontSize = 18.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(e.name, color = EC.Text, modifier = Modifier.weight(1f), fontSize = 14.sp, maxLines = 1)
                            Text(
                                if (left == 0L) "Hôm nay" else "còn $left ngày",
                                color = if (left <= 3) EC.Gold else EC.Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
        Category.values().forEach { cat ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    cat.label.uppercase(), color = EC.Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(Occasion.values().filter { it.category == cat }) { o ->
                OccasionTile(o) { onOpen(o, null) }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            val ctx = LocalContext.current
            val version = remember {
                runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: ""
            }
            Column(
                Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Chính sách quyền riêng tư",
                    color = EC.Gold, fontSize = 13.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        runCatching {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL)))
                        }
                    }.padding(8.dp)
                )
                Text("eCard – Tự Tạo Thiệp · phiên bản $version", color = EC.Muted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun OccasionTile(o: Occasion, onClick: () -> Unit) {
    val bg = if (o.solemn) listOf(Color(0xFF2A2E33), Color(0xFF15171A)) else listOf(EC.Surface2, EC.Surface)
    Box(
        Modifier
            .fillMaxWidth()
            .height(104.dp)
            .background(Brush.verticalGradient(bg), RoundedCornerShape(14.dp))
            .border(1.dp, (if (o.solemn) Color(0xFFC9CCD1) else EC.Gold).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(o.icon, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(o.label, color = EC.Text, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 14.sp, maxLines = 2)
            Text(o.title.zh, color = EC.Muted, fontSize = 11.sp, maxLines = 1)
        }
    }
}
