package vn.ecard.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vn.ecard.app.data.CalEvent
import vn.ecard.app.data.EventType
import vn.ecard.app.data.LunarCalendar
import java.time.LocalDate
import java.util.UUID

@Composable
fun CalendarScreen(
    events: List<CalEvent>,
    onSave: (CalEvent) -> Unit,
    onDelete: (String) -> Unit,
    onToggleHoliday: (String, Boolean) -> Unit,
    onMakeCard: (CalEvent) -> Unit
) {
    val today = LocalDate.now()
    val lunar = remember(today) { LunarCalendar.solarToLunar(today) }
    var editing by remember { mutableStateOf<CalEvent?>(null) }
    var showHolidays by remember { mutableStateOf(true) }
    val sorted = remember(events, showHolidays) {
        events.filter { showHolidays || !it.builtIn }.sortedBy { it.daysLeft(today) }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(12.dp, 12.dp, 12.dp, 90.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF1B3556), Color(0xFF0A1626))), RoundedCornerShape(16.dp))
                        .border(1.dp, EC.Gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Hôm nay ${today.dayOfMonth}/${today.monthValue}/${today.year}", color = EC.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Âm lịch: ${lunar.day}/${lunar.month}${if (lunar.leap) " (nhuận)" else ""} năm ${lunar.canChiYear()} · ${LunarCalendar.canChiYearZh(lunar.year)}年",
                        color = EC.Gold, fontSize = 15.sp
                    )
                    Spacer(Modifier.padding(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hiện ngày Lễ, Tết có sẵn", color = EC.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = showHolidays, onCheckedChange = { showHolidays = it })
                    }
                }
            }
            items(sorted, key = { it.id }) { e -> EventRow(e, today, onToggleHoliday, onEdit = { editing = e }, onMakeCard = { onMakeCard(e) }) }
        }
        ExtendedFloatingActionButton(
            onClick = { editing = CalEvent(UUID.randomUUID().toString(), "", EventType.BIRTHDAY, today.dayOfMonth, today.monthValue, false) },
            icon = { Icon(Icons.Filled.Add, null) },
            text = { Text("Thêm sự kiện") },
            containerColor = EC.Gold,
            contentColor = Color(0xFF2A1E00),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        )
    }

    editing?.let { ev ->
        EventDialog(
            ev, isNew = events.none { it.id == ev.id },
            onDismiss = { editing = null },
            onSave = { onSave(it); editing = null },
            onDelete = { onDelete(ev.id); editing = null }
        )
    }
}

@Composable
private fun EventRow(e: CalEvent, today: LocalDate, onToggleHoliday: (String, Boolean) -> Unit, onEdit: () -> Unit, onMakeCard: () -> Unit) {
    val next = e.nextDate(today)
    val left = e.daysLeft(today)
    val years = e.yearsAt(next)
    Column(
        Modifier
            .fillMaxWidth()
            .background(EC.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, if (left <= 3) EC.Gold.copy(alpha = 0.7f) else Color.Transparent, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.type.icon, fontSize = 26.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(e.name, color = EC.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    e.dateLabel() + " → ${next.dayOfMonth}/${next.monthValue}/${next.year}" + (years?.let { " · tròn $it năm" } ?: ""),
                    color = EC.Muted, fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(if (left == 0L) "HÔM NAY" else "$left", color = if (left <= 7) EC.Gold else EC.Text, fontSize = if (left == 0L) 14.sp else 22.sp, fontWeight = FontWeight.Bold)
                if (left > 0) Text("ngày", color = EC.Muted, fontSize = 11.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onMakeCard) { Text("✍️ Tạo thiệp") }
            if (e.builtIn) {
                Spacer(Modifier.weight(1f))
                Text("Nhắc", color = EC.Muted, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Switch(checked = e.enabled, onCheckedChange = { onToggleHoliday(e.id, it) })
            } else {
                TextButton(onClick = onEdit) { Text("Sửa") }
                Spacer(Modifier.weight(1f))
                Text(if (e.enabled) "🔔 " + e.remindDays.sortedDescending().joinToString(", ") { if (it == 0) "đúng ngày" else "$it" } else "🔕 tắt nhắc", color = EC.Muted, fontSize = 12.sp)
            }
        }
    }
}

private val remindOptions = listOf(30, 14, 7, 3, 1, 0)

@Composable
private fun EventDialog(ev: CalEvent, isNew: Boolean, onDismiss: () -> Unit, onSave: (CalEvent) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(ev.name) }
    var type by remember { mutableStateOf(ev.type) }
    var lunar by remember { mutableStateOf(ev.lunar) }
    var day by remember { mutableStateOf(ev.day.toString()) }
    var month by remember { mutableStateOf(ev.month.toString()) }
    var year by remember { mutableStateOf(ev.year?.toString() ?: "") }
    var remind by remember { mutableStateOf(ev.remindDays.toSet()) }
    var enabled by remember { mutableStateOf(ev.enabled) }
    var error by remember { mutableStateOf<String?>(null) }

    val chipColors = FilterChipDefaults.filterChipColors(selectedContainerColor = EC.Gold, selectedLabelColor = Color(0xFF2A1E00))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Thêm sự kiện" else "Sửa sự kiện") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(name, { name = it }, label = { Text("Tên (vd: Sinh nhật mẹ, Giỗ ông nội)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("Loại", color = EC.Gold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    EventType.values().filter { it != EventType.HOLIDAY }.forEach { t ->
                        FilterChip(selected = type == t, onClick = {
                            type = t
                            if (t == EventType.DEATH_ANNIVERSARY) lunar = true
                        }, label = { Text(t.icon + " " + t.label) }, colors = chipColors)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Tính theo Âm lịch", modifier = Modifier.weight(1f), color = EC.Text)
                    Switch(checked = lunar, onCheckedChange = { lunar = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(day, { day = it.filter(Char::isDigit).take(2) }, label = { Text("Ngày") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(month, { month = it.filter(Char::isDigit).take(2) }, label = { Text("Tháng") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(year, { year = it.filter(Char::isDigit).take(4) }, label = { Text("Năm gốc") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1.3f))
                }
                Text(
                    if (lunar) "Ngày âm lịch – tự đổi sang dương lịch mỗi năm. Năm gốc (không bắt buộc) dùng để tính số năm."
                    else "Năm gốc (không bắt buộc) dùng để tính tuổi / số năm kỷ niệm.",
                    color = EC.Muted, fontSize = 11.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Text("Bật nhắc nhở", modifier = Modifier.weight(1f), color = EC.Text)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Text("Nhắc trước (ngày)", color = EC.Gold, fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    remindOptions.forEach { d ->
                        FilterChip(selected = d in remind, onClick = { remind = if (d in remind) remind - d else remind + d },
                            label = { Text(if (d == 0) "Đúng ngày" else "$d") }, colors = chipColors)
                    }
                }
                error?.let { Text(it, color = Color(0xFFFF8A80), fontSize = 13.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val d = day.toIntOrNull()
                val m = month.toIntOrNull()
                val y = year.toIntOrNull()
                error = when {
                    name.isBlank() -> "Nhập tên sự kiện"
                    m == null || m !in 1..12 -> "Tháng phải từ 1 đến 12"
                    d == null || d !in 1..(if (lunar) 30 else 31) -> "Ngày không hợp lệ"
                    y != null && (y < 1900 || y > 2100) -> "Năm gốc không hợp lệ"
                    else -> null
                }
                if (error == null) onSave(
                    ev.copy(
                        name = name.trim(), type = type, lunar = lunar, day = d!!, month = m!!, year = y,
                        remindDays = remind.sorted(), enabled = enabled, occasion = type.occasion
                    )
                )
            }) { Text("Lưu") }
        },
        dismissButton = {
            Row {
                if (!isNew) TextButton(onClick = onDelete) { Text("Xoá", color = Color(0xFFFF8A80)) }
                TextButton(onClick = onDismiss) { Text("Huỷ") }
            }
        }
    )
}
