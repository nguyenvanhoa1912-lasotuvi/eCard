package vn.ecard.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class EventType(val label: String, val icon: String, val occasion: Occasion) {
    BIRTHDAY("Sinh nhật", "🎂", Occasion.BIRTHDAY),
    DEATH_ANNIVERSARY("Ngày giỗ", "🪷", Occasion.MEMORIAL),
    WEDDING_ANNIVERSARY("Kỷ niệm ngày cưới", "💍", Occasion.WEDDING),
    LONGEVITY("Mừng thọ", "🍑", Occasion.LONGEVITY),
    HOLIDAY("Lễ, Tết", "🎉", Occasion.TET),
    OTHER("Sự kiện khác", "📌", Occasion.THANKS)
}

/**
 * Sự kiện lặp lại hằng năm. Nếu lunar = true thì day/month là ngày âm lịch.
 * builtIn = ngày lễ có sẵn (không xoá được, chỉ bật/tắt nhắc).
 */
data class CalEvent(
    val id: String,
    val name: String,
    val type: EventType,
    val day: Int,
    val month: Int,
    val lunar: Boolean,
    val year: Int? = null,
    val remindDays: List<Int> = listOf(7, 3, 1, 0),
    val enabled: Boolean = true,
    val builtIn: Boolean = false,
    val occasion: Occasion = type.occasion,
    val note: String = ""
) {
    fun nextDate(today: LocalDate = LocalDate.now()): LocalDate =
        if (lunar) LunarCalendar.nextSolarOfLunar(day, month, today)
        else {
            fun safe(y: Int): LocalDate {
                val ym = java.time.YearMonth.of(y, month)
                return ym.atDay(minOf(day, ym.lengthOfMonth()))
            }
            val d = safe(today.year)
            if (d.isBefore(today)) safe(today.year + 1) else d
        }

    fun daysLeft(today: LocalDate = LocalDate.now()): Long = ChronoUnit.DAYS.between(today, nextDate(today))

    fun dateLabel(): String = if (lunar) "$day/$month âm lịch" else "$day/$month dương lịch"

    /** Số tuổi / số năm sắp tròn (nếu biết năm gốc). */
    fun yearsAt(next: LocalDate): Int? {
        val y = year ?: return null
        val n = if (lunar) LunarCalendar.solarToLunar(next).year - y else next.year - y
        return if (n > 0) n else null
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id); put("name", name); put("type", type.name)
        put("day", day); put("month", month); put("lunar", lunar)
        year?.let { put("year", it) }
        put("remind", JSONArray(remindDays))
        put("enabled", enabled); put("builtIn", builtIn)
        put("occasion", occasion.name); put("note", note)
    }

    companion object {
        fun fromJson(o: JSONObject): CalEvent {
            val r = o.optJSONArray("remind")
            val remind = if (r == null) listOf(7, 3, 1, 0) else List(r.length()) { r.getInt(it) }
            val type = runCatching { EventType.valueOf(o.getString("type")) }.getOrDefault(EventType.OTHER)
            return CalEvent(
                id = o.getString("id"),
                name = o.getString("name"),
                type = type,
                day = o.getInt("day"),
                month = o.getInt("month"),
                lunar = o.optBoolean("lunar"),
                year = if (o.has("year")) o.getInt("year") else null,
                remindDays = remind,
                enabled = o.optBoolean("enabled", true),
                builtIn = o.optBoolean("builtIn"),
                occasion = runCatching { Occasion.valueOf(o.getString("occasion")) }.getOrDefault(type.occasion),
                note = o.optString("note")
            )
        }
    }
}

object Holidays {
    private fun h(id: String, name: String, d: Int, m: Int, lunar: Boolean, occ: Occasion, remind: List<Int> = listOf(7, 1)) =
        CalEvent("hol_$id", name, EventType.HOLIDAY, d, m, lunar, null, remind, true, true, occ)

    val list: List<CalEvent> = listOf(
        h("newyear", "Tết Dương lịch", 1, 1, false, Occasion.NEW_YEAR),
        h("valentine", "Lễ Tình nhân", 14, 2, false, Occasion.VALENTINE),
        h("women83", "Quốc tế Phụ nữ 8/3", 8, 3, false, Occasion.WOMEN_DAY),
        h("304", "Giải phóng miền Nam 30/4", 30, 4, false, Occasion.THANKS, listOf(1)),
        h("labour", "Quốc tế Lao động 1/5", 1, 5, false, Occasion.THANKS, listOf(1)),
        h("children", "Quốc tế Thiếu nhi 1/6", 1, 6, false, Occasion.BIRTHDAY, listOf(3)),
        h("national", "Quốc khánh 2/9", 2, 9, false, Occasion.THANKS, listOf(1)),
        h("vnwomen", "Phụ nữ Việt Nam 20/10", 20, 10, false, Occasion.VN_WOMEN_DAY),
        h("teacher", "Nhà giáo Việt Nam 20/11", 20, 11, false, Occasion.TEACHER_DAY),
        h("xmas", "Giáng sinh", 25, 12, false, Occasion.CHRISTMAS),
        h("tet", "Tết Nguyên Đán", 1, 1, true, Occasion.TET, listOf(14, 7, 1)),
        h("ram1", "Rằm tháng Giêng", 15, 1, true, Occasion.TET, listOf(1)),
        h("hungking", "Giỗ Tổ Hùng Vương", 10, 3, true, Occasion.MEMORIAL, listOf(1)),
        h("vesak", "Lễ Phật Đản", 15, 4, true, Occasion.WISDOM, listOf(1)),
        h("doanngo", "Tết Đoan Ngọ", 5, 5, true, Occasion.TET, listOf(1)),
        h("vulan", "Lễ Vu Lan", 15, 7, true, Occasion.MEMORIAL, listOf(3, 1)),
        h("trungthu", "Tết Trung Thu", 15, 8, true, Occasion.MID_AUTUMN, listOf(7, 1)),
        h("ongtao", "Ông Công Ông Táo", 23, 12, true, Occasion.TET, listOf(1))
    )
}

class EventRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ecard_events", Context.MODE_PRIVATE)

    fun load(): List<CalEvent> {
        val user = runCatching {
            val arr = JSONArray(prefs.getString("events", "[]"))
            List(arr.length()) { CalEvent.fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
        val disabled = prefs.getStringSet("holiday_off", emptySet()) ?: emptySet()
        val hol = Holidays.list.map { it.copy(enabled = it.id !in disabled) }
        return user + hol
    }

    fun saveUser(events: List<CalEvent>) {
        val arr = JSONArray()
        events.filter { !it.builtIn }.forEach { arr.put(it.toJson()) }
        prefs.edit().putString("events", arr.toString()).apply()
    }

    fun upsert(e: CalEvent) {
        val list = load().filter { !it.builtIn }.toMutableList()
        val i = list.indexOfFirst { it.id == e.id }
        if (i >= 0) list[i] = e else list += e
        saveUser(list)
    }

    fun delete(id: String) = saveUser(load().filter { !it.builtIn && it.id != id })

    fun setHolidayEnabled(id: String, on: Boolean) {
        val s = (prefs.getStringSet("holiday_off", emptySet()) ?: emptySet()).toMutableSet()
        if (on) s.remove(id) else s.add(id)
        prefs.edit().putStringSet("holiday_off", s).apply()
    }

    /** Chống nhắc trùng trong cùng một ngày. */
    fun wasNotified(key: String): Boolean = prefs.getBoolean("n_$key", false)
    fun markNotified(key: String) = prefs.edit().putBoolean("n_$key", true).apply()
}
