package vn.ecard.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import vn.ecard.app.data.CalEvent
import vn.ecard.app.data.EventRepository
import vn.ecard.app.data.Occasion
import vn.ecard.app.data.Phrase
import vn.ecard.app.reminder.ReminderWorker
import vn.ecard.app.ui.CalendarScreen
import vn.ecard.app.ui.EC
import vn.ecard.app.ui.ECardTheme
import vn.ecard.app.ui.EditorScreen
import vn.ecard.app.ui.GalleryScreen
import vn.ecard.app.ui.HomeScreen
import vn.ecard.app.ui.LibraryScreen

/** Yêu cầu mở trình soạn thiệp (từ thông báo nhắc hoặc từ màn hình khác). */
data class EditRequest(val occasion: Occasion, val phrase: Phrase? = null, val stamp: Long = System.nanoTime())

class MainActivity : ComponentActivity() {

    private var pendingRequest by mutableStateOf<EditRequest?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderWorker.ensureChannel(this)
        ReminderWorker.schedule(this)
        handleIntent(intent)
        setContent { ECardTheme { App(pendingRequest) { pendingRequest = null } } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        val name = i?.getStringExtra(ReminderWorker.EXTRA_OCCASION) ?: return
        val o = runCatching { Occasion.valueOf(name) }.getOrNull() ?: return
        pendingRequest = EditRequest(o)
        i.removeExtra(ReminderWorker.EXTRA_OCCASION)
    }
}

@Composable
private fun App(external: EditRequest?, consumeExternal: () -> Unit) {
    val ctx = LocalContext.current
    val repo = remember { EventRepository(ctx) }
    var events by remember { mutableStateOf<List<CalEvent>>(repo.load()) }
    var tab by remember { mutableIntStateOf(0) }
    var editor by remember { mutableStateOf<EditRequest?>(null) }

    val notifPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) ReminderWorker.runNow(ctx)
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        else ReminderWorker.runNow(ctx)
    }
    LaunchedEffect(external) {
        if (external != null) {
            editor = external
            consumeExternal()
        }
    }

    fun reload() {
        events = repo.load()
    }

    Box(Modifier.fillMaxSize().background(EC.Bg).safeDrawingPadding()) {
        val req = editor
        if (req != null) {
            key(req.stamp) { EditorScreen(req.occasion, req.phrase, onClose = { editor = null }) }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        0 -> HomeScreen(
                            upcoming = events.filter { it.enabled }.sortedBy { it.daysLeft() }.filter { it.daysLeft() <= 30 },
                            onOpen = { o, _ -> editor = EditRequest(o) }
                        )
                        1 -> LibraryScreen(onUse = { ph -> editor = EditRequest(ph.occasion, ph) })
                        2 -> CalendarScreen(
                            events = events,
                            onSave = { repo.upsert(it); reload() },
                            onDelete = { repo.delete(it); reload() },
                            onToggleHoliday = { id, on -> repo.setHolidayEnabled(id, on); reload() },
                            onMakeCard = { e -> editor = EditRequest(e.occasion) }
                        )
                        3 -> GalleryScreen()
                    }
                }
                NavigationBar(containerColor = EC.Surface) {
                    val items = listOf("🎨" to "Tạo thiệp", "📖" to "Thư viện lời", "📅" to "Lịch nhắc", "🖼️" to "Đã lưu")
                    items.forEachIndexed { i, (icon, label) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Text(icon, fontSize = 20.sp) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = EC.Gold,
                                indicatorColor = EC.Gold.copy(alpha = 0.25f),
                                unselectedTextColor = EC.Muted
                            )
                        )
                    }
                }
            }
        }
    }
}
