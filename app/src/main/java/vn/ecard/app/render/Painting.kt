package vn.ecard.app.render

import android.graphics.Bitmap

/** Bố cục tranh thư pháp. */
enum class PaintLayout(val label: String, val icon: String) {
    VN_FLOW("Thư pháp Việt", "🖌️"),
    VN_STEP("Thư pháp bậc thang", "🌸"),
    HAN_VERTICAL("Hán tự viết dọc", "🏯")
}

/** Họa tiết thủy mặc. */
enum class Motif(val label: String) {
    BAMBOO_BOAT("Tre · thuyền"),
    PLUM("Mai đỏ"),
    PEACH("Đào hồng"),
    LOTUS("Sen"),
    PINE_MOUNTAIN("Tùng · núi"),
    MOON("Trăng · núi"),
    NONE("Không họa tiết")
}

/** Nền tranh. */
enum class Paper(val label: String) {
    RICE("Giấy dó"),
    WHITE("Giấy trắng"),
    STONE("Bia đá"),
    DARK("Nền đậm")
}

/** Màu mực. */
enum class Ink(val label: String, val hex: String, val onDark: String) {
    BLACK("Mực đen", "#1E1A16", "#F3EEDF"),
    CINNABAR("Son đỏ", "#C0501E", "#FF8A50"),
    RED("Đỏ thắm", "#A31515", "#EF5350"),
    GOLD("Vàng kim", "#9C7424", "#E8C25A")
}

data class PaintingSpec(
    val layout: PaintLayout = PaintLayout.VN_FLOW,
    val motif: Motif = Motif.BAMBOO_BOAT,
    val paper: Paper = Paper.RICE,
    /** Chữ lớn. Bố cục bậc thang: nhiều cụm cách nhau bằng dấu "/". */
    val headline: String = "Tri Ân",
    /** Các dòng chữ nhỏ (xuống dòng = dòng mới). Bậc thang: nhóm cách nhau bằng dòng trống. */
    val body: String = "",
    /** Dòng ghi nguồn hoặc ký tên nhỏ. */
    val source: String = "",
    val seal: String = "福",
    val headInk: Ink = Ink.CINNABAR,
    val bodyInk: Ink = Ink.BLACK,
    val headFont: CardFont = CardFont.CALLI_BOLD,
    val bodyFont: CardFont = CardFont.CALLI_BRUSH,
    val textScale: Float = 1f,
    val aspect: Aspect = Aspect.PORTRAIT,
    val photo: Bitmap? = null,
    val photoDim: Float = 0.25f
)

/** Mẫu có sẵn: chữ + bố cục + họa tiết gợi ý. */
data class PaintPreset(
    val name: String,
    val layout: PaintLayout,
    val headline: String,
    val body: String,
    val source: String,
    val motif: Motif,
    val paper: Paper,
    val seal: String,
    val headInk: Ink = Ink.BLACK
) {
    fun toSpec(old: PaintingSpec? = null): PaintingSpec {
        val base = old ?: PaintingSpec()
        return base.copy(
            layout = layout, headline = headline, body = body, source = source,
            motif = motif, paper = paper, seal = seal, headInk = headInk,
            photo = null
        )
    }
}

/**
 * Kho câu thư pháp. Gồm ca dao, tục ngữ, thành ngữ, cổ văn đã hết bản quyền
 * và các câu do eCard tự soạn. Không dùng thơ có bản quyền của người khác.
 */
object PaintPresets {
    val all: List<PaintPreset> = listOf(
        // ── Thư pháp Việt ──
        PaintPreset("Tri Ân thầy cô", PaintLayout.VN_FLOW, "Tri Ân",
            "Ơn thầy như biển rộng,\ndìu con qua sóng gió,\ntừng nét chữ dịu dàng,\ntheo con suốt cuộc đời…", "", Motif.BAMBOO_BOAT, Paper.RICE, "師", Ink.CINNABAR),
        PaintPreset("Không thầy đố mày làm nên", PaintLayout.VN_FLOW, "Thầy",
            "Không thầy đố mày làm nên.\nMột chữ cũng là thầy,\nnửa chữ cũng là thầy.", "Tục ngữ", Motif.PINE_MOUNTAIN, Paper.RICE, "師", Ink.CINNABAR),
        PaintPreset("Chữ Hiếu", PaintLayout.VN_FLOW, "Hiếu",
            "Công cha như núi Thái Sơn,\nnghĩa mẹ như nước trong nguồn chảy ra.\nMột lòng thờ mẹ kính cha,\ncho tròn chữ hiếu mới là đạo con.", "Ca dao", Motif.PINE_MOUNTAIN, Paper.RICE, "孝"),
        PaintPreset("Chữ Mẹ", PaintLayout.VN_FLOW, "Mẹ",
            "Mẹ là dòng sông hiền,\nlặng lẽ chảy qua đời con,\nbồi đắp từng mùa thương,\nchẳng một lời kể công.", "", Motif.LOTUS, Paper.RICE, "母", Ink.RED),
        PaintPreset("Chữ Tâm", PaintLayout.VN_FLOW, "Tâm",
            "Tâm an thì vạn sự an,\nlòng thanh thì cảnh cũng nhàn.\nGiữ tâm như nước mùa thu,\ntrong veo soi bóng trăng vàng.", "", Motif.LOTUS, Paper.RICE, "心"),
        PaintPreset("Chữ Nhẫn", PaintLayout.VN_FLOW, "Nhẫn",
            "Một điều nhịn,\nchín điều lành.\nLặng một chút, biển trời yên,\nlùi một bước, lòng thênh thang.", "Tục ngữ", Motif.BAMBOO_BOAT, Paper.RICE, "忍"),
        PaintPreset("An Nhiên", PaintLayout.VN_FLOW, "An Nhiên",
            "Sống chậm lại một chút,\nthương mình nhiều hơn một chút,\nbuông bớt những muộn phiền,\nđể lòng nhẹ như mây.", "", Motif.MOON, Paper.RICE, "安"),
        PaintPreset("Gia Đình", PaintLayout.VN_FLOW, "Gia Đình",
            "Gia hòa vạn sự hưng.\nNhà là nơi để về,\ncó bữa cơm ấm áp,\ncó nụ cười yêu thương.", "", Motif.PEACH, Paper.RICE, "家", Ink.RED),
        PaintPreset("Uống nước nhớ nguồn", PaintLayout.VN_FLOW, "Nhớ Nguồn",
            "Uống nước nhớ nguồn,\năn quả nhớ kẻ trồng cây.\nCây có cội, nước có nguồn,\ncon người có tổ có tông.", "Tục ngữ – Ca dao", Motif.BAMBOO_BOAT, Paper.RICE, "源"),
        PaintPreset("Có công mài sắt", PaintLayout.VN_FLOW, "Kiên Trì",
            "Có công mài sắt,\ncó ngày nên kim.\nNước chảy đá mòn,\nchí bền ắt thành.", "Tục ngữ", Motif.PINE_MOUNTAIN, Paper.WHITE, "志"),

        // ── Thư pháp bậc thang ──
        PaintPreset("Xuân – Phúc", PaintLayout.VN_STEP, "Xuân / Phúc",
            "về an khang\nnhà sum họp\n\nđầy muôn lối\nlộc đến\ntrăm nhà", "", Motif.PLUM, Paper.WHITE, "春"),
        PaintPreset("Phúc – Lộc – Thọ", PaintLayout.VN_STEP, "Phúc / Lộc / Thọ",
            "như Đông Hải\n\ntheo xuân về\n\ntựa Nam Sơn", "", Motif.PLUM, Paper.WHITE, "福", Ink.RED),
        PaintPreset("Tết – An – Vui", PaintLayout.VN_STEP, "Tết / An",
            "đến muôn nhà\nhoa đào nở\n\nkhang thịnh vượng\nvạn sự như ý", "", Motif.PEACH, Paper.RICE, "吉"),
        PaintPreset("Hoa – Xuân", PaintLayout.VN_STEP, "Hoa / Xuân",
            "khai phú quý\nsắc thắm tươi\n\nvề đất nước\nlòng người vui", "", Motif.PLUM, Paper.WHITE, "春", Ink.RED),

        // ── Hán tự viết dọc (cổ văn đã hết bản quyền) ──
        PaintPreset("寧靜致遠", PaintLayout.HAN_VERTICAL, "寧靜致遠",
            "夫君子之行，靜以修身，儉以養德。非淡泊無以明志，非寧靜無以致遠。", "諸葛亮《誡子書》", Motif.PINE_MOUNTAIN, Paper.STONE, "靜"),
        PaintPreset("上善若水", PaintLayout.HAN_VERTICAL, "上善若水",
            "上善若水。水善利萬物而不爭，處眾人之所惡，故幾於道。", "老子《道德經》", Motif.MOON, Paper.STONE, "水"),
        PaintPreset("厚德載物", PaintLayout.HAN_VERTICAL, "厚德載物",
            "天行健，君子以自強不息；地勢坤，君子以厚德載物。", "《周易》", Motif.PINE_MOUNTAIN, Paper.RICE, "德"),
        PaintPreset("知足常樂", PaintLayout.HAN_VERTICAL, "知足常樂",
            "知人者智，自知者明。勝人者有力，自勝者強。知足者富。", "老子《道德經》", Motif.LOTUS, Paper.RICE, "樂"),
        PaintPreset("學而時習", PaintLayout.HAN_VERTICAL, "學而時習",
            "學而時習之，不亦說乎？有朋自遠方來，不亦樂乎？", "《論語》", Motif.BAMBOO_BOAT, Paper.RICE, "學"),
        PaintPreset("家和萬事興", PaintLayout.HAN_VERTICAL, "家和萬事興",
            "家和萬事興，福壽康寧。", "Thành ngữ", Motif.PEACH, Paper.DARK, "家", Ink.GOLD),
        PaintPreset("福壽康寧", PaintLayout.HAN_VERTICAL, "福壽康寧",
            "福如東海，壽比南山。", "Thành ngữ", Motif.PINE_MOUNTAIN, Paper.DARK, "壽", Ink.GOLD)
    )

    fun byLayout(l: PaintLayout) = all.filter { it.layout == l }
}
