package vn.ecard.app.render

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import vn.ecard.app.data.Occasion
import java.util.Locale
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Kết quả vẽ khung: vùng đặt chữ + bảng màu chữ. */
class FrameResult(
    val area: RectF,
    val title: Int,
    val body: Int,
    val ornament: Int,
    val divider: CardStyle,
    val dark: Boolean
)

/**
 * Khung thiết kế riêng theo chủ đề (mỗi dịp 3 khung), vẽ hoàn toàn bằng vector.
 * Đợt 1: Tết, Sinh nhật, Cưới, Mừng thọ, Chia buồn/Tưởng nhớ, Trung thu.
 */
object ThemeFrames {

    private enum class Group { TET, BIRTHDAY, WEDDING, LONGEVITY, CONDOLENCE, MID_AUTUMN }

    private fun group(o: Occasion): Group? = when (o) {
        Occasion.TET -> Group.TET
        Occasion.BIRTHDAY -> Group.BIRTHDAY
        Occasion.WEDDING, Occasion.INVITE_WEDDING -> Group.WEDDING
        Occasion.LONGEVITY -> Group.LONGEVITY
        Occasion.CONDOLENCE, Occasion.MEMORIAL, Occasion.INVITE_MEMORIAL -> Group.CONDOLENCE
        Occasion.MID_AUTUMN -> Group.MID_AUTUMN
        else -> null
    }

    fun has(o: Occasion): Boolean = group(o) != null

    fun labels(o: Occasion): List<String> = when (group(o)) {
        Group.TET -> listOf("Câu đối đỏ", "Mai đào · đèn lồng", "Pháo · lì xì")
        Group.BIRTHDAY -> listOf("Bánh kem", "Bóng bay", "Quà · ruy băng")
        Group.WEDDING -> listOf("Song hỷ 囍", "Vòng hoa hồng", "Nhẫn đôi")
        Group.LONGEVITY -> listOf("Chữ Thọ 壽", "Đào tiên · tùng hạc", "Mây lành")
        Group.CONDOLENCE -> listOf("Hoa cúc trắng", "Nến · khói hương", "Hoa sen")
        Group.MID_AUTUMN -> listOf("Trăng · thỏ ngọc", "Đèn ông sao", "Đèn lồng · bánh")
        null -> emptyList()
    }

    fun draw(cv: Canvas, w: Float, h: Float, o: Occasion, index: Int): FrameResult {
        val u = w / 1000f
        val i = index.coerceIn(1, 3)
        return when (group(o)) {
            Group.TET -> when (i) { 1 -> tetCouplet(cv, w, h, u); 2 -> tetBlossom(cv, w, h, u); else -> tetFirecracker(cv, w, h, u) }
            Group.BIRTHDAY -> when (i) { 1 -> bdayCake(cv, w, h, u); 2 -> bdayBalloons(cv, w, h, u); else -> bdayGift(cv, w, h, u) }
            Group.WEDDING -> when (i) { 1 -> wedDoubleHappy(cv, w, h, u); 2 -> wedWreath(cv, w, h, u); else -> wedRings(cv, w, h, u) }
            Group.LONGEVITY -> when (i) { 1 -> lonCharacter(cv, w, h, u); 2 -> lonPeachCrane(cv, w, h, u); else -> lonClouds(cv, w, h, u) }
            Group.CONDOLENCE -> when (i) { 1 -> conChrysanthemum(cv, w, h, u); 2 -> conCandle(cv, w, h, u); else -> conLotus(cv, w, h, u) }
            Group.MID_AUTUMN -> when (i) { 1 -> midMoon(cv, w, h, u); 2 -> midStarLantern(cv, w, h, u); else -> midLanternCake(cv, w, h, u) }
            null -> {
                grad(cv, w, h, Color.DKGRAY, Color.BLACK)
                FrameResult(RectF(120 * u, 150 * u, w - 120 * u, h - 150 * u), Color.WHITE, Color.WHITE, Color.WHITE, CardStyle.WEST_MODERN, true)
            }
        }
    }

    // ═══════════════════════ tiện ích ═══════════════════════

    private fun c(hex: String) = Color.parseColor(hex)
    private fun a(color: Int, al: Float) =
        Color.argb((al * 255).toInt().coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    private fun stroke(color: Int, width: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.STROKE; strokeWidth = width; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    private fun grad(cv: Canvas, w: Float, h: Float, c1: Int, c2: Int) {
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, 0f, h, c1, c2, Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, p)
    }

    private fun glow(cv: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(cx, cy, r, color, Color.TRANSPARENT, Shader.TileMode.CLAMP)
        cv.drawCircle(cx, cy, r, p)
    }

    private fun border(cv: Canvas, w: Float, h: Float, u: Float, color: Int, inset: Float = 32f) {
        cv.drawRect(inset * u, inset * u, w - inset * u, h - inset * u, stroke(color, 4 * u))
        cv.drawRect((inset + 12) * u, (inset + 12) * u, w - (inset + 12) * u, h - (inset + 12) * u, stroke(a(color, 0.6f), 1.6f * u))
    }

    /** Chữ Hán canh giữa theo cả hai chiều. */
    private fun hanzi(cv: Canvas, text: String, cx: Float, cy: Float, size: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textLocale = Locale.TRADITIONAL_CHINESE
        }
        val fm = p.fontMetrics
        cv.drawText(text, cx, cy - (fm.ascent + fm.descent) / 2, p)
    }

    private fun flower5(cv: Canvas, cx: Float, cy: Float, r: Float, petal: Int, center: Int, rot: Float) {
        val p = fill(petal)
        for (k in 0 until 5) {
            val ang = Math.toRadians((rot + k * 72).toDouble())
            cv.drawCircle(cx + (r * 0.55f * cos(ang)).toFloat(), cy + (r * 0.55f * sin(ang)).toFloat(), r * 0.5f, p)
        }
        cv.drawCircle(cx, cy, r * 0.22f, fill(center))
    }

    private fun branch(cv: Canvas, u: Float, x0: Float, y0: Float, x1: Float, y1: Float, bend: Float, wood: Int, petal: Int, center: Int, rnd: Random) {
        val mx = (x0 + x1) / 2 + bend * u
        val my = (y0 + y1) / 2 - bend * u
        val path = Path().apply { moveTo(x0, y0); quadTo(mx, my, x1, y1) }
        cv.drawPath(path, stroke(wood, 10 * u))
        // nhánh phụ + hoa dọc theo cành
        for (k in 1..6) {
            val t = k / 7f
            val bx = (1 - t) * (1 - t) * x0 + 2 * (1 - t) * t * mx + t * t * x1
            val by = (1 - t) * (1 - t) * y0 + 2 * (1 - t) * t * my + t * t * y1
            val ex = bx + (rnd.nextFloat() - 0.3f) * 70 * u
            val ey = by - (30 + rnd.nextFloat() * 50) * u
            cv.drawLine(bx, by, ex, ey, stroke(wood, 5 * u))
            flower5(cv, ex, ey, (17 + rnd.nextFloat() * 9) * u, petal, center, rnd.nextFloat() * 72)
            if (k % 2 == 0) flower5(cv, bx, by, 15 * u, petal, center, rnd.nextFloat() * 72)
        }
        flower5(cv, x1, y1, 20 * u, petal, center, 10f)
    }

    private fun lantern(cv: Canvas, u: Float, cx: Float, top: Float, r: Float, body: Int, gold: Int) {
        cv.drawLine(cx, 0f, cx, top, stroke(gold, 2.5f * u))
        val oval = RectF(cx - r, top + 0.25f * r, cx + r, top + 1.85f * r)
        val bp = Paint(Paint.ANTI_ALIAS_FLAG)
        bp.shader = RadialGradient(cx - 0.3f * r, top + 0.8f * r, 1.3f * r, Color.rgb(255, 120, 90), body, Shader.TileMode.CLAMP)
        cv.drawOval(oval, bp)
        for (k in 1..3) {
            val ww = r * (1f - k * 0.27f)
            cv.drawOval(RectF(cx - ww, oval.top, cx + ww, oval.bottom), stroke(a(gold, 0.85f), 2f * u))
        }
        cv.drawRoundRect(RectF(cx - 0.42f * r, top, cx + 0.42f * r, top + 0.32f * r), 4 * u, 4 * u, fill(gold))
        cv.drawRoundRect(RectF(cx - 0.42f * r, oval.bottom - 0.08f * r, cx + 0.42f * r, oval.bottom + 0.22f * r), 4 * u, 4 * u, fill(gold))
        val tp = stroke(gold, 2.5f * u)
        for (k in -3..3) cv.drawLine(cx + k * 0.07f * r, oval.bottom + 0.22f * r, cx + k * 0.1f * r, oval.bottom + 0.9f * r, tp)
        hanzi(cv, "福", cx, (oval.top + oval.bottom) / 2, r * 0.75f, gold)
    }

    private fun stars(cv: Canvas, w: Float, h: Float, u: Float, rnd: Random, n: Int, color: Int, maxY: Float = h) {
        val p = fill(color)
        repeat(n) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * maxY
            p.alpha = (80 + rnd.nextInt(170))
            cv.drawCircle(x, y, (1.2f + rnd.nextFloat() * 2.6f) * u, p)
        }
    }

    private fun sparkle(cv: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val path = Path().apply {
            moveTo(cx, cy - r); quadTo(cx, cy, cx + r, cy); quadTo(cx, cy, cx, cy + r); quadTo(cx, cy, cx - r, cy); quadTo(cx, cy, cx, cy - r); close()
        }
        cv.drawPath(path, fill(color))
    }

    private fun burst(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float, color: Int) {
        val p = stroke(color, 3 * u)
        for (k in 0 until 18) {
            val ang = Math.toRadians(k * 20.0)
            val x1 = cx + (r * 0.35f * cos(ang)).toFloat()
            val y1 = cy + (r * 0.35f * sin(ang)).toFloat()
            val x2 = cx + (r * cos(ang)).toFloat()
            val y2 = cy + (r * sin(ang)).toFloat()
            cv.drawLine(x1, y1, x2, y2, p)
            cv.drawCircle(x2, y2, 4 * u, fill(color))
        }
    }

    // ═══════════════════════ TẾT ═══════════════════════

    private fun tetCouplet(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#FBF1DC"), c("#EED9AE"))
        val red = c("#C62828"); val gold = c("#D4A73A")
        // Hoành phi
        val pl = RectF(260 * u, 46 * u, w - 260 * u, 140 * u)
        cv.drawRoundRect(pl, 10 * u, 10 * u, fill(red))
        cv.drawRoundRect(RectF(pl.left + 8 * u, pl.top + 8 * u, pl.right - 8 * u, pl.bottom - 8 * u), 6 * u, 6 * u, stroke(gold, 2.5f * u))
        val head = "迎春接福"
        val step = (pl.width() - 60 * u) / head.length
        head.forEachIndexed { k, ch -> hanzi(cv, ch.toString(), pl.left + 30 * u + step * (k + 0.5f), pl.centerY(), 54 * u, c("#F6D27A")) }
        // Câu đối 2 bên (đọc phải sang trái: vế trên bên phải)
        val bw = 112 * u
        val top = 175 * u; val bot = h - 95 * u
        val lines = listOf("春滿乾坤福滿門" to 50 * u, "天增歲月人增壽" to w - 50 * u - bw)
        for ((text, x) in lines) {
            val r = RectF(x, top, x + bw, bot)
            cv.drawRoundRect(r, 8 * u, 8 * u, fill(red))
            cv.drawRoundRect(RectF(r.left + 8 * u, r.top + 8 * u, r.right - 8 * u, r.bottom - 8 * u), 5 * u, 5 * u, stroke(gold, 2.5f * u))
            val cell = (r.height() - 30 * u) / text.length
            val size = min(bw * 0.68f, cell * 0.8f)
            text.forEachIndexed { k, ch -> hanzi(cv, ch.toString(), r.centerX(), r.top + 15 * u + cell * (k + 0.5f), size, c("#F6D27A")) }
        }
        return FrameResult(RectF(195 * u, 175 * u, w - 195 * u, h - 110 * u), c("#B71C1C"), c("#4E2A1A"), gold, CardStyle.EAST_CLASSIC, false)
    }

    private fun tetBlossom(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#A31515"), c("#5E0A0A"))
        glow(cv, w / 2, h * 0.45f, w * 0.7f, a(c("#FF8A65"), 0.18f))
        val gold = c("#E8C25A")
        border(cv, w, h, u, gold, 26f)
        val rnd = Random(11)
        lantern(cv, u, w * 0.17f, 70 * u, 68 * u, c("#E53935"), gold)
        lantern(cv, u, w * 0.83f, 120 * u, 58 * u, c("#E53935"), gold)
        // Mai vàng góc trái dưới, đào hồng góc phải dưới
        branch(cv, u, -10 * u, h - 40 * u, 360 * u, h - 250 * u, 60f, c("#3E2723"), c("#FFD54F"), c("#E65100"), rnd)
        branch(cv, u, w + 10 * u, h - 60 * u, w - 360 * u, h - 270 * u, -60f, c("#3E2723"), c("#F48FB1"), c("#AD1457"), rnd)
        return FrameResult(RectF(130 * u, 300 * u, w - 130 * u, h - 300 * u), c("#F6D27A"), c("#FFF3D6"), gold, CardStyle.EAST_CLASSIC, true)
    }

    private fun tetFirecracker(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#D84315"), c("#8E1B0F"))
        val gold = c("#FFD54F")
        burst(cv, u, w * 0.2f, 150 * u, 95 * u, a(gold, 0.9f))
        burst(cv, u, w * 0.55f, 90 * u, 60 * u, a(c("#FFF59D"), 0.8f))
        // Dây pháo bên phải
        val sx = w - 105 * u
        val end = h * 0.62f
        cv.drawLine(sx, 0f, sx, end, stroke(c("#5D4037"), 3 * u))
        var y = 40 * u
        var k = 0
        while (y < end) {
            cv.save()
            cv.rotate(if (k % 2 == 0) 28f else -28f, sx, y)
            val r = RectF(sx - 40 * u, y - 13 * u, sx + 40 * u, y + 13 * u)
            cv.drawRoundRect(r, 6 * u, 6 * u, fill(c("#C62828")))
            cv.drawRect(r.left + 8 * u, r.top, r.left + 15 * u, r.bottom, fill(gold))
            cv.drawRect(r.right - 15 * u, r.top, r.right - 8 * u, r.bottom, fill(gold))
            cv.restore()
            y += 44 * u; k++
        }
        sparkle(cv, sx, end + 30 * u, 26 * u, gold)
        // Bao lì xì góc trái dưới
        val px = 175 * u; val py = h - 150 * u
        for ((ang, dx) in listOf(-22f to -60f, 0f to 0f, 22f to 60f)) {
            cv.save()
            cv.rotate(ang, px + dx * u, py)
            val r = RectF(px + dx * u - 55 * u, py - 150 * u, px + dx * u + 55 * u, py + 20 * u)
            cv.drawRoundRect(r, 10 * u, 10 * u, fill(c("#E53935")))
            cv.drawRoundRect(RectF(r.left + 7 * u, r.top + 7 * u, r.right - 7 * u, r.bottom - 7 * u), 6 * u, 6 * u, stroke(gold, 2.2f * u))
            cv.drawCircle(r.centerX(), r.top + 62 * u, 30 * u, fill(gold))
            hanzi(cv, "福", r.centerX(), r.top + 62 * u, 40 * u, c("#B71C1C"))
            cv.restore()
        }
        return FrameResult(RectF(110 * u, 250 * u, w - 190 * u, h - 300 * u), gold, c("#FFF8E1"), gold, CardStyle.EAST_CLASSIC, true)
    }

    // ═══════════════════════ SINH NHẬT ═══════════════════════

    private fun bdayCake(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#FFE4EC"), c("#E9D5FF"))
        val rnd = Random(21)
        val cols = intArrayOf(c("#EC407A"), c("#AB47BC"), c("#FFCA28"), c("#29B6F6"))
        repeat(60) {
            val p = fill(cols[rnd.nextInt(cols.size)])
            val x = rnd.nextFloat() * w; val y = rnd.nextFloat() * h * 0.35f
            cv.save(); cv.rotate(rnd.nextFloat() * 180, x, y)
            if (rnd.nextBoolean()) cv.drawCircle(x, y, 5 * u, p) else cv.drawRect(x - 9 * u, y - 3.5f * u, x + 9 * u, y + 3.5f * u, p)
            cv.restore()
        }
        // Bánh 3 tầng
        val cx = w / 2
        val base = h - 95 * u
        cv.drawOval(RectF(cx - 320 * u, base - 22 * u, cx + 320 * u, base + 22 * u), fill(c("#D1C4E9")))
        val tiers = listOf(Triple(520f, 120f, c("#F8BBD0")), Triple(400f, 100f, c("#F48FB1")), Triple(280f, 85f, c("#F8BBD0")))
        var top = base
        for ((tw, th, col) in tiers) {
            val r = RectF(cx - tw / 2 * u, top - th * u, cx + tw / 2 * u, top)
            cv.drawRoundRect(r, 14 * u, 14 * u, fill(col))
            // kem chảy
            val cream = fill(Color.WHITE)
            cv.drawRect(r.left, r.top, r.right, r.top + 16 * u, cream)
            var x = r.left + 12 * u
            while (x < r.right - 8 * u) {
                cv.drawCircle(x, r.top + 16 * u, 13 * u, cream)
                x += 30 * u
            }
            top = r.top
        }
        // Nến
        val n = 5
        for (k in 0 until n) {
            val x = cx - 100 * u + k * 50 * u
            cv.drawRoundRect(RectF(x - 7 * u, top - 70 * u, x + 7 * u, top), 4 * u, 4 * u, fill(cols[k % cols.size]))
            glow(cv, x, top - 92 * u, 30 * u, a(c("#FFEB3B"), 0.6f))
            val flame = Path().apply {
                moveTo(x, top - 112 * u); quadTo(x + 13 * u, top - 88 * u, x, top - 74 * u); quadTo(x - 13 * u, top - 88 * u, x, top - 112 * u); close()
            }
            cv.drawPath(flame, fill(c("#FF9800")))
        }
        val textBottom = top - 140 * u
        return FrameResult(RectF(110 * u, 110 * u, w - 110 * u, textBottom), c("#8E24AA"), c("#4A2C5A"), c("#EC407A"), CardStyle.WEST_MODERN, false)
    }

    private fun balloon(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float, col: Int) {
        val path = Path().apply { moveTo(cx, cy + r * 1.15f); quadTo(cx + 40 * u, cy + r * 1.8f, cx - 10 * u, cy + r * 2.4f) }
        cv.drawPath(path, stroke(a(Color.DKGRAY, 0.6f), 2 * u))
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = RadialGradient(cx - r * 0.35f, cy - r * 0.4f, r * 1.4f, Color.WHITE, col, Shader.TileMode.CLAMP)
        cv.drawOval(RectF(cx - r * 0.86f, cy - r, cx + r * 0.86f, cy + r * 1.1f), p)
        val knot = Path().apply { moveTo(cx, cy + r * 1.08f); lineTo(cx - 9 * u, cy + r * 1.08f + 14 * u); lineTo(cx + 9 * u, cy + r * 1.08f + 14 * u); close() }
        cv.drawPath(knot, fill(col))
    }

    private fun bdayBalloons(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#E3F2FD"), c("#FFF8E1"))
        balloon(cv, u, 120 * u, 200 * u, 80 * u, c("#EC407A"))
        balloon(cv, u, 250 * u, 140 * u, 70 * u, c("#FFCA28"))
        balloon(cv, u, 215 * u, 290 * u, 62 * u, c("#26A69A"))
        balloon(cv, u, w - 120 * u, 190 * u, 80 * u, c("#7E57C2"))
        balloon(cv, u, w - 250 * u, 130 * u, 68 * u, c("#FF7043"))
        balloon(cv, u, w - 215 * u, 285 * u, 60 * u, c("#42A5F5"))
        val rnd = Random(5)
        repeat(14) { sparkle(cv, rnd.nextFloat() * w, h * 0.55f + rnd.nextFloat() * h * 0.4f, (6 + rnd.nextFloat() * 8) * u, a(c("#FFB300"), 0.7f)) }
        return FrameResult(RectF(110 * u, 450 * u, w - 110 * u, h - 110 * u), c("#1565C0"), c("#263238"), c("#FF7043"), CardStyle.WEST_MODERN, false)
    }

    private fun bow(cv: Canvas, u: Float, cx: Float, cy: Float, s: Float, col: Int) {
        val p = fill(col)
        cv.save(); cv.rotate(-25f, cx, cy); cv.drawOval(RectF(cx - 70 * s * u, cy - 26 * s * u, cx, cy + 26 * s * u), p); cv.restore()
        cv.save(); cv.rotate(25f, cx, cy); cv.drawOval(RectF(cx, cy - 26 * s * u, cx + 70 * s * u, cy + 26 * s * u), p); cv.restore()
        cv.drawCircle(cx, cy, 16 * s * u, fill(a(Color.BLACK, 0.15f)))
        cv.drawCircle(cx, cy, 14 * s * u, p)
    }

    private fun bdayGift(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#2A1B5E"), c("#120B30"))
        val gold = c("#FFD54F")
        stars(cv, w, h, u, Random(3), 90, Color.WHITE)
        // dải ruy băng chéo 2 góc
        val band = Path().apply { moveTo(0f, 240 * u); lineTo(240 * u, 0f); lineTo(330 * u, 0f); lineTo(0f, 330 * u); close() }
        cv.drawPath(band, fill(gold))
        bow(cv, u, 150 * u, 140 * u, 1.1f, c("#FFB300"))
        val band2 = Path().apply { moveTo(w, h - 240 * u); lineTo(w - 240 * u, h); lineTo(w - 330 * u, h); lineTo(w, h - 330 * u); close() }
        cv.drawPath(band2, fill(gold))
        bow(cv, u, w - 150 * u, h - 140 * u, 1.1f, c("#FFB300"))
        // hộp quà góc trái dưới
        fun box(x: Float, y: Float, s: Float, col: Int) {
            val r = RectF(x, y - s * u, x + s * u, y)
            cv.drawRect(r, fill(col))
            cv.drawRect(r.left - 8 * u, r.top - 26 * u, r.right + 8 * u, r.top, fill(col))
            cv.drawRect(r.centerX() - 10 * u, r.top - 26 * u, r.centerX() + 10 * u, r.bottom, fill(gold))
            bow(cv, u, r.centerX(), r.top - 30 * u, s / 260f, gold)
        }
        box(70 * u, h - 70 * u, 160f, c("#EC407A"))
        box(250 * u, h - 70 * u, 110f, c("#26A69A"))
        return FrameResult(RectF(140 * u, 300 * u, w - 140 * u, h - 330 * u), gold, c("#F3E5F5"), gold, CardStyle.WEST_MODERN, true)
    }

    // ═══════════════════════ CƯỚI ═══════════════════════

    private fun cloudCurl(cv: Canvas, u: Float, cx: Float, cy: Float, s: Float, col: Int, flip: Boolean) {
        val p = stroke(col, 4 * u)
        val d = if (flip) -1 else 1
        val path = Path().apply {
            moveTo(cx - d * 70 * s * u, cy)
            cubicTo(cx - d * 70 * s * u, cy - 50 * s * u, cx + d * 10 * s * u, cy - 55 * s * u, cx + d * 10 * s * u, cy - 15 * s * u)
            cubicTo(cx + d * 10 * s * u, cy + 10 * s * u, cx - d * 25 * s * u, cy + 8 * s * u, cx - d * 22 * s * u, cy - 12 * s * u)
            moveTo(cx - d * 70 * s * u, cy)
            cubicTo(cx - d * 40 * s * u, cy + 30 * s * u, cx + d * 60 * s * u, cy + 30 * s * u, cx + d * 80 * s * u, cy - 5 * s * u)
        }
        cv.drawPath(path, p)
    }

    private fun wedDoubleHappy(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#B71C1C"), c("#7F0000"))
        val gold = c("#F6D27A")
        border(cv, w, h, u, gold, 28f)
        cloudCurl(cv, u, 140 * u, 130 * u, 1f, gold, false)
        cloudCurl(cv, u, w - 140 * u, 130 * u, 1f, gold, true)
        cloudCurl(cv, u, 140 * u, h - 110 * u, 1f, gold, false)
        cloudCurl(cv, u, w - 140 * u, h - 110 * u, 1f, gold, true)
        val cy = 230 * u
        cv.drawCircle(w / 2, cy, 135 * u, fill(gold))
        cv.drawCircle(w / 2, cy, 118 * u, stroke(c("#B71C1C"), 3 * u))
        hanzi(cv, "囍", w / 2, cy, 170 * u, c("#B71C1C"))
        return FrameResult(RectF(120 * u, 400 * u, w - 120 * u, h - 160 * u), gold, c("#FFF3E0"), gold, CardStyle.EAST_CLASSIC, true)
    }

    private fun rose(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float, col: Int, dark: Int) {
        cv.drawCircle(cx, cy, r, fill(col))
        val p = stroke(dark, 2.2f * u)
        for (k in 1..3) {
            val rr = r * (1f - k * 0.25f)
            cv.drawArc(RectF(cx - rr, cy - rr, cx + rr, cy + rr), k * 70f, 250f, false, p)
        }
    }

    private fun wedWreath(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#FFF7F5"), c("#FCE4EC"))
        val cx = w / 2; val cy = h / 2
        val rr = min(w, h) * 0.42f
        cv.drawCircle(cx, cy, rr, stroke(c("#A5C8A0"), 3 * u))
        val leaf = fill(c("#81A77C"))
        val rnd = Random(8)
        for (k in 0 until 36) {
            val ang = k * 10.0 + rnd.nextFloat() * 4
            val x = cx + (rr * cos(Math.toRadians(ang))).toFloat()
            val y = cy + (rr * sin(Math.toRadians(ang))).toFloat()
            cv.save(); cv.rotate(ang.toFloat() + if (k % 2 == 0) 40f else -40f, x, y)
            cv.drawOval(RectF(x - 26 * u, y - 10 * u, x + 26 * u, y + 10 * u), leaf)
            cv.restore()
        }
        val roses = listOf(c("#E57373"), c("#F48FB1"), c("#FFCDD2"))
        for (k in 0 until 12) {
            val ang = Math.toRadians(k * 30.0 + 15)
            val x = cx + (rr * cos(ang)).toFloat()
            val y = cy + (rr * sin(ang)).toFloat()
            val col = roses[k % 3]
            rose(cv, u, x, y, (if (k % 3 == 0) 38f else 30f) * u, col, a(c("#880E4F"), 0.45f))
        }
        val inner = rr * 0.72f
        return FrameResult(RectF(cx - inner, cy - inner * 0.85f, cx + inner, cy + inner * 0.85f), c("#AD1457"), c("#5D4037"), c("#E91E63"), CardStyle.WEST_CLASSIC, false)
    }

    private fun wedRings(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#FFFDF7"), c("#F3E9D2"))
        val gold = c("#C9A13B")
        border(cv, w, h, u, gold, 30f)
        val cy = 200 * u
        cv.drawCircle(w / 2 - 48 * u, cy, 72 * u, stroke(gold, 13 * u))
        cv.drawCircle(w / 2 + 48 * u, cy, 72 * u, stroke(c("#E0C068"), 13 * u))
        val dx = w / 2 + 48 * u; val dy = cy - 84 * u
        val gem = Path().apply { moveTo(dx, dy - 22 * u); lineTo(dx + 20 * u, dy); lineTo(dx, dy + 18 * u); lineTo(dx - 20 * u, dy); close() }
        cv.drawPath(gem, fill(c("#E3F2FD")))
        cv.drawPath(gem, stroke(c("#90CAF9"), 2 * u))
        sparkle(cv, w / 2 + 130 * u, cy - 90 * u, 14 * u, gold)
        sparkle(cv, w / 2 - 140 * u, cy + 60 * u, 10 * u, gold)
        // ruy băng satin cuối thiệp
        val by = h - 150 * u
        val band = Path().apply {
            moveTo(0f, by); cubicTo(w * 0.3f, by - 40 * u, w * 0.7f, by + 40 * u, w, by - 10 * u)
            lineTo(w, by + 40 * u); cubicTo(w * 0.7f, by + 90 * u, w * 0.3f, by + 10 * u, 0f, by + 50 * u); close()
        }
        val bp = Paint(Paint.ANTI_ALIAS_FLAG)
        bp.shader = LinearGradient(0f, by, w, by, c("#E8D5A3"), c("#C9A13B"), Shader.TileMode.MIRROR)
        cv.drawPath(band, bp)
        bow(cv, u, w / 2, by + 20 * u, 1.2f, c("#D4AF37"))
        return FrameResult(RectF(120 * u, 330 * u, w - 120 * u, h - 230 * u), c("#8D6E2F"), c("#4E342E"), gold, CardStyle.WEST_CLASSIC, false)
    }

    // ═══════════════════════ MỪNG THỌ ═══════════════════════

    private fun lonCharacter(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#8E1B1B"), c("#4A0B0B"))
        val gold = c("#E8C25A")
        // viền hồi văn
        val p = stroke(a(gold, 0.7f), 2.5f * u)
        val step = 46f * u
        var x = 30 * u
        while (x < w - 40 * u) {
            for (yy in listOf(30 * u, h - 30 * u - 30 * u)) {
                cv.drawRect(x, yy, x + 30 * u, yy + 30 * u, p)
                cv.drawRect(x + 9 * u, yy + 9 * u, x + 21 * u, yy + 21 * u, p)
            }
            x += step
        }
        var y = 30 * u + step
        while (y < h - 60 * u - step) {
            for (xx in listOf(30 * u, w - 60 * u)) {
                cv.drawRect(xx, y, xx + 30 * u, y + 30 * u, p)
                cv.drawRect(xx + 9 * u, y + 9 * u, xx + 21 * u, y + 21 * u, p)
            }
            y += step
        }
        val cy = 240 * u
        glow(cv, w / 2, cy, 220 * u, a(gold, 0.3f))
        cv.drawCircle(w / 2, cy, 140 * u, fill(gold))
        cv.drawCircle(w / 2, cy, 122 * u, stroke(c("#7A1010"), 4 * u))
        hanzi(cv, "壽", w / 2, cy, 175 * u, c("#7A1010"))
        return FrameResult(RectF(120 * u, 420 * u, w - 120 * u, h - 120 * u), gold, c("#FFF3D6"), gold, CardStyle.EAST_CLASSIC, true)
    }

    private fun lonPeachCrane(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#FBF5E6"), c("#EFE3C6"))
        val wood = c("#5D4037"); val pine = c("#2E5E3A")
        // Cành tùng góc trái trên
        val main = Path().apply { moveTo(-10 * u, 110 * u); quadTo(180 * u, 60 * u, 360 * u, 170 * u) }
        cv.drawPath(main, stroke(wood, 12 * u))
        val needle = stroke(pine, 3 * u)
        for ((nx, ny) in listOf(90f to 80f, 190f to 95f, 280f to 130f, 350f to 168f, 140f to 150f)) {
            for (k in 0 until 14) {
                val ang = Math.toRadians(180.0 + k * (180.0 / 13))
                cv.drawLine(nx * u, ny * u, nx * u + (45 * u * cos(ang)).toFloat(), ny * u + (32 * u * sin(ang)).toFloat() + 28 * u, needle)
            }
        }
        // Hạc bay góc phải trên
        val hx = w - 230 * u; val hy = 170 * u
        cv.save(); cv.rotate(-12f, hx, hy)
        val wing = Path().apply { moveTo(hx - 20 * u, hy); quadTo(hx - 40 * u, hy - 110 * u, hx + 60 * u, hy - 140 * u); quadTo(hx + 20 * u, hy - 60 * u, hx + 30 * u, hy); close() }
        cv.drawPath(wing, fill(Color.WHITE)); cv.drawPath(wing, stroke(a(Color.BLACK, 0.5f), 2 * u))
        cv.drawOval(RectF(hx - 60 * u, hy - 18 * u, hx + 60 * u, hy + 18 * u), fill(Color.WHITE))
        cv.drawOval(RectF(hx - 60 * u, hy - 18 * u, hx + 60 * u, hy + 18 * u), stroke(a(Color.BLACK, 0.5f), 2 * u))
        val neck = Path().apply { moveTo(hx + 55 * u, hy - 5 * u); quadTo(hx + 110 * u, hy - 20 * u, hx + 150 * u, hy - 50 * u) }
        cv.drawPath(neck, stroke(Color.BLACK, 5 * u))
        cv.drawCircle(hx + 152 * u, hy - 52 * u, 9 * u, fill(Color.BLACK))
        cv.drawCircle(hx + 154 * u, hy - 60 * u, 5 * u, fill(c("#D32F2F")))
        cv.drawLine(hx + 160 * u, hy - 52 * u, hx + 190 * u, hy - 44 * u, stroke(c("#8D6E63"), 3 * u))
        cv.drawLine(hx - 55 * u, hy + 5 * u, hx - 150 * u, hy + 30 * u, stroke(Color.BLACK, 3 * u))
        cv.drawLine(hx - 55 * u, hy + 10 * u, hx - 150 * u, hy + 42 * u, stroke(Color.BLACK, 3 * u))
        cv.restore()
        // Đào tiên góc phải dưới
        fun peach(px: Float, py: Float, r: Float) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.shader = RadialGradient(px - r * 0.3f, py - r * 0.3f, r * 1.3f, c("#FFE0B2"), c("#F06292"), Shader.TileMode.CLAMP)
            val path = Path().apply {
                moveTo(px, py - r); cubicTo(px + r * 1.2f, py - r * 1.1f, px + r * 1.1f, py + r, px, py + r)
                cubicTo(px - r * 1.1f, py + r, px - r * 1.2f, py - r * 1.1f, px, py - r); close()
            }
            cv.drawPath(path, p)
            cv.drawLine(px, py - r * 0.9f, px + r * 0.1f, py + r * 0.6f, stroke(a(c("#C2185B"), 0.4f), 2 * u))
            cv.save(); cv.rotate(-30f, px, py - r)
            cv.drawOval(RectF(px - 8 * u, py - r - 16 * u, px + 46 * u, py - r + 6 * u), fill(c("#558B2F")))
            cv.restore()
        }
        peach(w - 200 * u, h - 150 * u, 62 * u)
        peach(w - 100 * u, h - 120 * u, 50 * u)
        peach(w - 150 * u, h - 240 * u, 48 * u)
        return FrameResult(RectF(130 * u, 320 * u, w - 130 * u, h - 320 * u), c("#8B1A1A"), c("#3E2723"), c("#2E7D32"), CardStyle.EAST_MODERN, false)
    }

    private fun xiangyun(cv: Canvas, u: Float, cx: Float, cy: Float, s: Float, col: Int) {
        val p = fill(a(col, 0.9f))
        cv.drawCircle(cx, cy, 34 * s * u, p)
        cv.drawCircle(cx + 42 * s * u, cy + 8 * s * u, 26 * s * u, p)
        cv.drawCircle(cx - 42 * s * u, cy + 10 * s * u, 24 * s * u, p)
        cv.drawRoundRect(RectF(cx - 66 * s * u, cy + 8 * s * u, cx + 68 * s * u, cy + 34 * s * u), 13 * s * u, 13 * s * u, p)
        val sp = stroke(c("#0D3B4F"), 3 * u)
        cv.drawArc(RectF(cx - 18 * s * u, cy - 18 * s * u, cx + 18 * s * u, cy + 18 * s * u), 180f, 270f, false, sp)
        cv.drawArc(RectF(cx + 30 * s * u, cy - 4 * s * u, cx + 54 * s * u, cy + 20 * s * u), 200f, 250f, false, sp)
    }

    private fun lonClouds(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#0D3B4F"), c("#06202B"))
        val gold = c("#E8C25A")
        border(cv, w, h, u, gold, 30f)
        xiangyun(cv, u, 150 * u, 130 * u, 1.2f, gold)
        xiangyun(cv, u, w - 150 * u, 130 * u, 1.2f, gold)
        xiangyun(cv, u, 150 * u, h - 140 * u, 1.2f, gold)
        xiangyun(cv, u, w - 150 * u, h - 140 * u, 1.2f, gold)
        xiangyun(cv, u, w / 2, 110 * u, 0.8f, gold)
        return FrameResult(RectF(140 * u, 220 * u, w - 140 * u, h - 230 * u), gold, c("#F5ECD7"), gold, CardStyle.EAST_CLASSIC, true)
    }

    // ═══════════════════════ CHIA BUỒN ═══════════════════════

    private fun mum(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float) {
        val petal = fill(Color.WHITE)
        val edge = stroke(a(c("#B0BEC5"), 0.8f), 1.2f * u)
        for (layer in 0..2) {
            val n = 24 - layer * 6
            val len = r * (1f - layer * 0.28f)
            for (k in 0 until n) {
                cv.save()
                cv.rotate(k * 360f / n + layer * 8f, cx, cy)
                val o = RectF(cx - len * 0.1f, cy - len, cx + len * 0.1f, cy)
                cv.drawOval(o, petal); cv.drawOval(o, edge)
                cv.restore()
            }
        }
        cv.drawCircle(cx, cy, r * 0.16f, fill(c("#E6E1B8")))
    }

    private fun conChrysanthemum(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#2B2E33"), c("#0E0F11"))
        val silver = c("#BDBDBD")
        border(cv, w, h, u, silver, 30f)
        val bx = w / 2; val by = h - 50 * u
        val heads = listOf(
            Triple(bx - 150 * u, h - 290 * u, 70f), Triple(bx, h - 340 * u, 82f), Triple(bx + 150 * u, h - 290 * u, 70f),
            Triple(bx - 80 * u, h - 200 * u, 60f), Triple(bx + 85 * u, h - 205 * u, 58f)
        )
        val stem = stroke(c("#4E6B4E"), 5 * u)
        for ((x, y, _) in heads) {
            val path = Path().apply { moveTo(bx, by); quadTo((bx + x) / 2, (by + y) / 2 + 30 * u, x, y) }
            cv.drawPath(path, stem)
        }
        val leaf = fill(c("#3D5A3D"))
        for ((lx, ly, ang) in listOf(Triple(bx - 90 * u, h - 120 * u, -35f), Triple(bx + 90 * u, h - 125 * u, 35f), Triple(bx - 40 * u, h - 160 * u, -60f))) {
            cv.save(); cv.rotate(ang, lx, ly); cv.drawOval(RectF(lx - 50 * u, ly - 15 * u, lx + 50 * u, ly + 15 * u), leaf); cv.restore()
        }
        for ((x, y, r) in heads) mum(cv, u, x, y, r * u)
        return FrameResult(RectF(130 * u, 150 * u, w - 130 * u, h - 450 * u), c("#E0E0E0"), c("#F5F5F5"), c("#9E9E9E"), CardStyle.EAST_CLASSIC, true)
    }

    private fun conCandle(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#1C1A17"), c("#0B0A09"))
        val cx = w / 2
        val baseY = h - 90 * u
        val topY = baseY - 230 * u
        glow(cv, cx, topY - 40 * u, 380 * u, a(c("#FFB74D"), 0.22f))
        // khói hương
        val smoke = stroke(a(Color.WHITE, 0.12f), 3 * u)
        for (k in -1..1) {
            val path = Path().apply {
                moveTo(cx + k * 6 * u, topY - 70 * u)
                cubicTo(cx + (k * 60 + 50) * u, topY - 220 * u, cx + (k * 60 - 60) * u, topY - 380 * u, cx + (k * 90 + 30) * u, topY - 560 * u)
            }
            cv.drawPath(path, smoke)
        }
        val body = Paint(Paint.ANTI_ALIAS_FLAG)
        body.shader = LinearGradient(cx - 50 * u, 0f, cx + 50 * u, 0f, c("#E8E0CF"), c("#FFFDF5"), Shader.TileMode.MIRROR)
        cv.drawRoundRect(RectF(cx - 48 * u, topY, cx + 48 * u, baseY), 10 * u, 10 * u, body)
        cv.drawOval(RectF(cx - 48 * u, topY - 10 * u, cx + 48 * u, topY + 12 * u), fill(c("#FFF8E7")))
        cv.drawLine(cx, topY, cx, topY - 18 * u, stroke(Color.DKGRAY, 3 * u))
        glow(cv, cx, topY - 50 * u, 60 * u, a(c("#FFEB3B"), 0.8f))
        val flame = Path().apply {
            moveTo(cx, topY - 95 * u); quadTo(cx + 22 * u, topY - 50 * u, cx, topY - 20 * u); quadTo(cx - 22 * u, topY - 50 * u, cx, topY - 95 * u); close()
        }
        cv.drawPath(flame, fill(c("#FFA726")))
        cv.drawOval(RectF(cx - 140 * u, baseY - 6 * u, cx + 140 * u, baseY + 22 * u), fill(c("#3E3A33")))
        return FrameResult(RectF(130 * u, 150 * u, w - 130 * u, topY - 160 * u), c("#F5E6C8"), c("#EDE7DF"), c("#BFA77A"), CardStyle.EAST_CLASSIC, true)
    }

    private fun conLotus(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#F7F7F5"), c("#E3E6E3"))
        val grey = c("#78909C")
        val baseY = h - 140 * u
        // lá sen
        cv.drawOval(RectF(80 * u, baseY - 10 * u, 420 * u, baseY + 60 * u), fill(c("#9DB39C")))
        cv.drawOval(RectF(w - 400 * u, baseY + 10 * u, w - 90 * u, baseY + 70 * u), fill(c("#8FA68E")))
        // sóng nước
        val wave = stroke(a(grey, 0.5f), 2 * u)
        for (k in 0..3) {
            val y = baseY + 80 * u + k * 22 * u
            val path = Path().apply {
                moveTo(0f, y)
                var x = 0f
                while (x < w) { quadTo(x + 40 * u, y - 10 * u, x + 80 * u, y); x += 80 * u }
            }
            cv.drawPath(path, wave)
        }
        // hoa sen trắng
        val cx = w / 2; val cy = baseY - 20 * u
        val petal = fill(Color.WHITE)
        val edge = stroke(a(grey, 0.9f), 2.2f * u)
        for ((ang, len) in listOf(-70f to 150f, 70f to 150f, -40f to 185f, 40f to 185f, 0f to 210f)) {
            cv.save(); cv.rotate(ang, cx, cy)
            val p = Path().apply {
                moveTo(cx, cy); cubicTo(cx - 55 * u, cy - len * 0.4f * u, cx - 30 * u, cy - len * 0.85f * u, cx, cy - len * u)
                cubicTo(cx + 30 * u, cy - len * 0.85f * u, cx + 55 * u, cy - len * 0.4f * u, cx, cy); close()
            }
            cv.drawPath(p, petal); cv.drawPath(p, edge)
            cv.restore()
        }
        cv.drawOval(RectF(cx - 30 * u, cy - 26 * u, cx + 30 * u, cy + 4 * u), fill(c("#E6D9A2")))
        return FrameResult(RectF(130 * u, 140 * u, w - 130 * u, cy - 240 * u), c("#37474F"), c("#424242"), grey, CardStyle.EAST_MODERN, false)
    }

    // ═══════════════════════ TRUNG THU ═══════════════════════

    private fun midMoon(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#0B1A3A"), c("#1D2F6B"))
        stars(cv, w, h, u, Random(9), 120, Color.WHITE, h * 0.7f)
        val mx = w * 0.72f; val my = 230 * u
        glow(cv, mx, my, 320 * u, a(c("#FFF3C4"), 0.35f))
        cv.drawCircle(mx, my, 150 * u, fill(c("#FFF3C4")))
        for ((dx, dy, r) in listOf(Triple(-50f, -30f, 26f), Triple(40f, 30f, 34f), Triple(-10f, 70f, 18f))) {
            cv.drawCircle(mx + dx * u, my + dy * u, r * u, fill(a(c("#E8D79A"), 0.7f)))
        }
        // đồi + thỏ ngọc
        val hill = Path().apply { moveTo(0f, h); lineTo(0f, h - 170 * u); quadTo(260 * u, h - 290 * u, 520 * u, h - 150 * u); lineTo(520 * u, h); close() }
        cv.drawPath(hill, fill(c("#142450")))
        val rx = 230 * u; val ry = h - 255 * u
        val white = fill(Color.WHITE)
        cv.drawOval(RectF(rx - 55 * u, ry - 40 * u, rx + 55 * u, ry + 45 * u), white)
        cv.drawCircle(rx + 45 * u, ry - 50 * u, 32 * u, white)
        cv.save(); cv.rotate(15f, rx + 45 * u, ry - 50 * u)
        cv.drawOval(RectF(rx + 30 * u, ry - 150 * u, rx + 50 * u, ry - 70 * u), white)
        cv.drawOval(RectF(rx + 55 * u, ry - 140 * u, rx + 75 * u, ry - 65 * u), white)
        cv.restore()
        cv.drawCircle(rx - 58 * u, ry + 20 * u, 14 * u, white)
        cv.drawCircle(rx + 58 * u, ry - 55 * u, 4 * u, fill(c("#E91E63")))
        return FrameResult(RectF(120 * u, 430 * u, w - 120 * u, h - 330 * u), c("#FFD54F"), c("#FDF6E3"), c("#FFD54F"), CardStyle.EAST_MODERN, true)
    }

    private fun starPath(cx: Float, cy: Float, r: Float, inner: Float): Path {
        val p = Path()
        for (k in 0 until 10) {
            val rr = if (k % 2 == 0) r else inner
            val ang = Math.toRadians(-90.0 + k * 36)
            val x = cx + (rr * cos(ang)).toFloat(); val y = cy + (rr * sin(ang)).toFloat()
            if (k == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close(); return p
    }

    private fun starLantern(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float) {
        glow(cv, cx, cy, r * 2.2f, a(c("#FFB300"), 0.25f))
        cv.drawPath(starPath(cx, cy, r, r * 0.42f), fill(c("#E53935")))
        cv.drawPath(starPath(cx, cy, r * 0.62f, r * 0.26f), fill(c("#FFCA28")))
        cv.drawPath(starPath(cx, cy, r, r * 0.42f), stroke(c("#8D6E63"), 3 * u))
        val t = stroke(c("#FFCA28"), 2.5f * u)
        for (k in -2..2) cv.drawLine(cx + k * 6 * u, cy + r * 0.5f, cx + k * 9 * u, cy + r * 1.2f, t)
    }

    private fun midStarLantern(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#2E1A47"), c("#120A24"))
        stars(cv, w, h, u, Random(4), 80, Color.WHITE)
        val pole = stroke(c("#A1887F"), 5 * u)
        cv.drawLine(0f, 330 * u, 200 * u, 150 * u, pole)
        starLantern(cv, u, 220 * u, 175 * u, 95 * u)
        cv.drawLine(w, 300 * u, w - 180 * u, 130 * u, pole)
        starLantern(cv, u, w - 200 * u, 150 * u, 80 * u)
        cv.drawLine(w, h - 40 * u, w - 150 * u, h - 160 * u, pole)
        starLantern(cv, u, w - 165 * u, h - 185 * u, 62 * u)
        return FrameResult(RectF(120 * u, 370 * u, w - 120 * u, h - 290 * u), c("#FFD54F"), c("#F3E5F5"), c("#FFD54F"), CardStyle.EAST_MODERN, true)
    }

    private fun midLanternCake(cv: Canvas, w: Float, h: Float, u: Float): FrameResult {
        grad(cv, w, h, c("#5D1A0E"), c("#2B0B05"))
        val gold = c("#FFCC80")
        lantern(cv, u, w * 0.18f, 60 * u, 60 * u, c("#E53935"), c("#FFC107"))
        lantern(cv, u, w * 0.5f, 20 * u, 50 * u, c("#FB8C00"), c("#FFC107"))
        lantern(cv, u, w * 0.82f, 80 * u, 60 * u, c("#E53935"), c("#FFC107"))
        // bánh trung thu
        val cx = w / 2; val cy = h - 200 * u; val r = 120 * u
        val edge = Path()
        for (k in 0..360 step 4) {
            val ang = Math.toRadians(k.toDouble())
            val rr = r * (1f + 0.07f * cos(Math.toRadians(k * 16.0)).toFloat())
            val x = cx + (rr * cos(ang)).toFloat(); val y = cy + (rr * sin(ang)).toFloat() * 0.92f
            if (k == 0) edge.moveTo(x, y) else edge.lineTo(x, y)
        }
        edge.close()
        cv.drawOval(RectF(cx - r * 1.2f, cy + r * 0.75f, cx + r * 1.2f, cy + r * 1.05f), fill(a(Color.BLACK, 0.35f)))
        val cp = Paint(Paint.ANTI_ALIAS_FLAG)
        cp.shader = RadialGradient(cx - r * 0.3f, cy - r * 0.3f, r * 1.4f, c("#F2B866"), c("#B66A25"), Shader.TileMode.CLAMP)
        cv.drawPath(edge, cp)
        cv.drawCircle(cx, cy, r * 0.7f, stroke(c("#8A4B16"), 4 * u))
        for (k in 0 until 8) {
            val ang = Math.toRadians(k * 45.0)
            cv.drawCircle(cx + (r * 0.55f * cos(ang)).toFloat(), cy + (r * 0.55f * sin(ang)).toFloat() * 0.92f, 8 * u, fill(c("#8A4B16")))
        }
        hanzi(cv, "月", cx, cy, r * 0.7f, c("#7A3F10"))
        return FrameResult(RectF(120 * u, 320 * u, w - 120 * u, cy - r - 70 * u), gold, c("#FFF3E0"), gold, CardStyle.EAST_CLASSIC, true)
    }
}
