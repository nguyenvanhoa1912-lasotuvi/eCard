package vn.ecard.app.render

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import vn.ecard.app.data.Lang
import vn.ecard.app.data.Occasion

enum class CardStyle(val label: String, val short: String) {
    EAST_CLASSIC("Đông phương – Cổ điển", "Đông · Cổ"),
    EAST_MODERN("Đông phương – Hiện đại", "Đông · Kim"),
    WEST_CLASSIC("Tây phương – Cổ điển", "Tây · Cổ"),
    WEST_MODERN("Tây phương – Hiện đại", "Tây · Kim")
}

enum class Aspect(val label: String, val w: Int, val h: Int) {
    PORTRAIT("4:5 (Facebook, Zalo)", 1080, 1350),
    SQUARE("1:1 (Vuông)", 1080, 1080),
    STORY("9:16 (Story)", 1080, 1920)
}

enum class CardFont(val label: String, val family: String, val calligraphy: Boolean = false) {
    SERIF("Có chân", "serif"),
    SANS("Không chân", "sans-serif"),
    SCRIPT("Viết tay", "cursive"),
    CONDENSED("Chữ hẹp", "sans-serif-condensed"),
    CALLI_BOLD("Thư pháp đậm", "splash", true),
    CALLI_LIGHT("Thư pháp thanh", "petemoss", true)
}

/** Phông thư pháp (OFL) nạp từ res/font. Gọi init() một lần khi mở app. */
object CardFonts {
    private var splash: Typeface? = null
    private var petemoss: Typeface? = null

    fun init(ctx: android.content.Context) {
        if (splash == null) splash = runCatching { androidx.core.content.res.ResourcesCompat.getFont(ctx, vn.ecard.app.R.font.splash) }.getOrNull()
        if (petemoss == null) petemoss = runCatching { androidx.core.content.res.ResourcesCompat.getFont(ctx, vn.ecard.app.R.font.petemoss) }.getOrNull()
    }

    fun typeface(f: CardFont, style: Int): Typeface = when (f) {
        CardFont.CALLI_BOLD -> splash ?: Typeface.create(Typeface.SERIF, style)
        CardFont.CALLI_LIGHT -> petemoss ?: Typeface.create(Typeface.SERIF, style)
        else -> Typeface.create(f.family, style)
    }

    /** Phông cho ô chữ thư pháp lớn. */
    fun bigWord(f: CardFont): Typeface = if (f == CardFont.CALLI_LIGHT) typeface(f, 0) else typeface(CardFont.CALLI_BOLD, 0)
}

/** Các chữ thư pháp lớn gợi ý. */
val BIG_WORDS = listOf("Phúc", "Lộc", "Thọ", "An", "Tâm", "Đức", "Nhẫn", "Hiếu", "Duyên", "Phát", "Xuân", "Hỷ")

class Palette(
    val deep1: Int, val deep2: Int,
    val light1: Int, val light2: Int,
    val accent: Int, val accent2: Int,
    val inkDark: Int, val inkLight: Int
)

enum class Tone(val label: String, val palette: Palette) {
    RED_GOLD("Đỏ – Vàng kim", Palette(c("#9B1C1C"), c("#5A0E0E"), c("#FFF6E5"), c("#F5DFB8"), c("#E8C25A"), c("#C62828"), c("#5A0E0E"), c("#FFF3D6"))),
    ROSE("Hồng – Đỏ rượu", Palette(c("#8C1D47"), c("#4A0E2A"), c("#FFF0F3"), c("#F9D5DD"), c("#F2C6A0"), c("#D6336C"), c("#6B1235"), c("#FFF0F5"))),
    NIGHT_GOLD("Đêm trăng", Palette(c("#1F2F6B"), c("#0B1530"), c("#F3F1FA"), c("#DCE3F5"), c("#F2C14E"), c("#E07A2F"), c("#1A2550"), c("#FDF6E3"))),
    GREEN_RED("Xanh thông – Đỏ", Palette(c("#14603A"), c("#082A19"), c("#F4FAF4"), c("#DCEEDC"), c("#E8C25A"), c("#C62828"), c("#0F3D24"), c("#FFF8E8"))),
    NAVY_GOLD("Xanh than – Vàng", Palette(c("#1B3556"), c("#0A1626"), c("#F8F5EC"), c("#E9E1CC"), c("#D4AF37"), c("#2E5A88"), c("#15263D"), c("#F5ECD7"))),
    PASTEL("Pastel vui tươi", Palette(c("#5B5BD6"), c("#B0437F"), c("#FDF7FF"), c("#E3F0FD"), c("#FFD166"), c("#EF6F9F"), c("#3B3355"), c("#FFFFFF"))),
    SOLEMN("Trắng – Đen (tang lễ)", Palette(c("#2A2E33"), c("#0B0C0E"), c("#FAFAF8"), c("#E4E4E1"), c("#C9CCD1"), c("#6B7280"), c("#2B2F33"), c("#F1F1EF")));

    companion object {
        fun auto(o: Occasion): Tone = when {
            o.solemn -> SOLEMN
            else -> when (o) {
                Occasion.TET, Occasion.OPENING, Occasion.LONGEVITY, Occasion.HOUSEWARMING -> RED_GOLD
                Occasion.WEDDING, Occasion.VALENTINE, Occasion.WOMEN_DAY, Occasion.VN_WOMEN_DAY, Occasion.INVITE_WEDDING -> ROSE
                Occasion.MID_AUTUMN, Occasion.NEW_YEAR -> NIGHT_GOLD
                Occasion.CHRISTMAS -> GREEN_RED
                Occasion.BIRTHDAY, Occasion.BABY -> PASTEL
                else -> NAVY_GOLD
            }
        }
    }
}

private fun c(hex: String) = Color.parseColor(hex)

data class CardSpec(
    val occasion: Occasion,
    val style: CardStyle = CardStyle.EAST_CLASSIC,
    val tone: Tone = Tone.auto(occasion),
    val photo: Bitmap? = null,
    val photoDim: Float = 0.35f,
    val langs: List<Lang> = listOf(Lang.VI),
    val title: Map<Lang, String> = Lang.values().associateWith { occasion.title.get(it) },
    val message: Map<Lang, String> = emptyMap(),
    val signature: String = "",
    val font: CardFont = CardFont.SERIF,
    val textScale: Float = 1f,
    val textOffset: Float = 0f,
    val aspect: Aspect = Aspect.PORTRAIT,
    val textColor: Int? = null,
    /** 0 = dùng phong cách chung; 1..3 = khung chủ đề riêng của dịp. */
    val frame: Int = 0,
    /** Chữ thư pháp lớn (rỗng = không dùng). */
    val bigWord: String = ""
)
