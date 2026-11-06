package vn.ecard.app.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import vn.ecard.app.data.Lang
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/**
 * Vẽ thiệp ra Bitmap. Cùng một hàm dùng cho xem trước (scale nhỏ) và xuất ảnh (scale 1),
 * nên ảnh xuất ra giống hệt bản xem trước.
 */
object CardRenderer {

    fun render(spec: CardSpec, scale: Float = 1f): Bitmap {
        val w = (spec.aspect.w * scale).toInt()
        val h = (spec.aspect.h * scale).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp), w.toFloat(), h.toFloat(), spec)
        return bmp
    }

    private fun alpha(color: Int, a: Float): Int =
        Color.argb((a * 255).toInt().coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))

    private fun luminance(color: Int): Float =
        (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f

    fun draw(cv: Canvas, w: Float, h: Float, spec: CardSpec) {
        val u = w / 1000f
        val p = spec.tone.palette
        val solemn = spec.tone == Tone.SOLEMN
        val rnd = Random(spec.occasion.ordinal * 31 + spec.style.ordinal * 7 + spec.tone.ordinal)
        val hasPhoto = spec.photo != null

        // Nền tối hay sáng quyết định màu chữ
        val darkBg = hasPhoto || spec.style == CardStyle.EAST_CLASSIC || spec.style == CardStyle.WEST_MODERN

        // ── 1. Nền ──
        if (hasPhoto) {
            drawPhoto(cv, w, h, spec.photo!!, spec.photoDim)
        } else when (spec.style) {
            CardStyle.EAST_CLASSIC -> {
                fillGradient(cv, w, h, p.deep1, p.deep2, vertical = true)
                latticePattern(cv, w, h, u, alpha(p.accent, 0.07f))
                vignette(cv, w, h, 0.45f)
            }
            CardStyle.EAST_MODERN -> {
                fillGradient(cv, w, h, p.light1, p.light2, vertical = true)
                paperGrain(cv, w, h, u, rnd, alpha(p.inkDark, 0.025f))
            }
            CardStyle.WEST_CLASSIC -> {
                fillGradient(cv, w, h, p.light1, p.light2, vertical = true)
                paperGrain(cv, w, h, u, rnd, alpha(p.inkDark, 0.03f))
                vignetteColor(cv, w, h, alpha(p.accent2, 0.22f))
            }
            CardStyle.WEST_MODERN -> {
                val paint = Paint()
                paint.shader = LinearGradient(0f, 0f, w, h, p.deep1, p.deep2, Shader.TileMode.CLAMP)
                cv.drawRect(0f, 0f, w, h, paint)
            }
        }

        // ── 2. Hoạ tiết ──
        when (spec.style) {
            CardStyle.EAST_CLASSIC -> {
                if (!hasPhoto) {
                    if (solemn) {
                        chrysanthemum(cv, 0.14f * w, h - 0.13f * w, 70 * u, Color.WHITE, p.accent2)
                        chrysanthemum(cv, 0.26f * w, h - 0.08f * w, 48 * u, alpha(Color.WHITE, 0.9f), p.accent2)
                        chrysanthemum(cv, w - 0.15f * w, 0.15f * w, 52 * u, alpha(Color.WHITE, 0.85f), p.accent2)
                    } else {
                        blossomBranch(cv, w, h, u, p.accent, alpha(Color.BLACK, 0.55f), topLeft = true, rnd = rnd)
                        blossomBranch(cv, w, h, u, p.accent, alpha(Color.BLACK, 0.55f), topLeft = false, rnd = rnd)
                    }
                }
                orientalFrame(cv, w, h, u, if (hasPhoto) alpha(Color.WHITE, 0.85f) else p.accent)
            }
            CardStyle.EAST_MODERN -> {
                if (!hasPhoto) {
                    val sunColor = if (solemn) alpha(p.accent2, 0.35f) else alpha(p.accent2, 0.85f)
                    val sp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = sunColor }
                    cv.drawCircle(w * 0.76f, h * 0.2f, 95 * u, sp)
                    inkMountains(cv, w, h, u, p.inkDark)
                    if (solemn) {
                        lotus(cv, w * 0.5f, h - 0.1f * w, 60 * u, alpha(p.inkDark, 0.55f))
                    } else {
                        thinBranch(cv, w, h, u, alpha(p.inkDark, 0.75f), p.accent2, rnd)
                    }
                }
                val lp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (hasPhoto) alpha(Color.WHITE, 0.7f) else alpha(p.accent2, 0.8f); strokeWidth = 3 * u
                }
                cv.drawLine(60 * u, 80 * u, 60 * u, h - 80 * u, lp)
            }
            CardStyle.WEST_CLASSIC -> {
                westernFrame(cv, w, h, u, if (hasPhoto) alpha(Color.WHITE, 0.85f) else p.accent, if (hasPhoto) alpha(Color.WHITE, 0.6f) else p.accent2, solemn)
            }
            CardStyle.WEST_MODERN -> {
                if (!hasPhoto) {
                    bokeh(cv, w, h, u, rnd, if (solemn) Color.WHITE else p.accent)
                    if (!solemn) confetti(cv, w, h, u, rnd, intArrayOf(p.accent, p.accent2, Color.WHITE))
                }
                val fp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE; strokeWidth = 2.5f * u; color = alpha(Color.WHITE, 0.45f)
                }
                cv.drawRoundRect(RectF(36 * u, 36 * u, w - 36 * u, h - 36 * u), 36 * u, 36 * u, fp)
            }
        }

        // ── 3. Chữ ──
        val titleColor: Int
        val bodyColor: Int
        when {
            spec.textColor != null -> { titleColor = spec.textColor; bodyColor = spec.textColor }
            hasPhoto -> { titleColor = Color.WHITE; bodyColor = Color.WHITE }
            darkBg -> { titleColor = p.accent; bodyColor = p.inkLight }
            spec.style == CardStyle.EAST_MODERN && !solemn -> { titleColor = p.accent2; bodyColor = p.inkDark }
            else -> { titleColor = p.inkDark; bodyColor = p.inkDark }
        }
        val shadow = hasPhoto || (spec.textColor != null && luminance(spec.textColor) > 0.6f && !darkBg)
        drawTexts(cv, w, h, u, spec, titleColor, bodyColor, shadow, if (hasPhoto) Color.WHITE else if (darkBg) p.accent else p.accent2)

        // Ấn triện (Đông phương cổ điển)
        if (spec.style == CardStyle.EAST_CLASSIC && !hasPhoto) {
            seal(cv, w - 150 * u, h - 165 * u, 62 * u, if (solemn) "奠" else "福", if (solemn) p.accent2 else p.accent2, p.inkLight, darkBg)
        }
    }

    // ───────────────────────── chữ ─────────────────────────

    private fun typeface(spec: CardSpec, bold: Boolean): Typeface =
        Typeface.create(spec.font.family, if (bold) Typeface.BOLD else Typeface.NORMAL)

    private fun layout(text: String, paint: TextPaint, width: Int): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.12f)
            .setIncludePad(false)
            .build()

    private fun drawTexts(
        cv: Canvas, w: Float, h: Float, u: Float, spec: CardSpec,
        titleColor: Int, bodyColor: Int, shadow: Boolean, ornamentColor: Int
    ) {
        val langs = spec.langs.ifEmpty { listOf(Lang.VI) }
        val left = if (spec.style == CardStyle.EAST_MODERN) 110 * u else 120 * u
        val right = w - 120 * u
        val width = (right - left).toInt()
        val sigReserve = if (spec.signature.isNotBlank()) 130 * u else 40 * u
        val top = 150 * u
        val bottom = h - 150 * u - sigReserve

        var s = spec.textScale
        var blocks: List<Triple<StaticLayout, Float, Boolean>> = emptyList()
        var total = 0f
        for (attempt in 0 until 8) {
            val b = mutableListOf<Triple<StaticLayout, Float, Boolean>>() // layout, khoảng cách trước, là tiêu đề?
            langs.forEachIndexed { i, l ->
                val t = spec.title[l].orEmpty().trim()
                if (t.isNotEmpty()) {
                    val tp = textPaint(spec, l, true, titleColor, (if (i == 0) 84f else 50f) * u * s, shadow, u)
                    b += Triple(layout(t, tp, width), if (b.isEmpty()) 0f else 14 * u * s, true)
                }
            }
            val titleCount = b.size
            val msgs = langs.mapNotNull { l -> spec.message[l]?.trim()?.takeIf { it.isNotEmpty() }?.let { l to it } }
            msgs.forEachIndexed { i, (l, m) ->
                val size = (if (msgs.size == 1) 46f else if (i == 0) 42f else 36f) * u * s
                val mp = textPaint(spec, l, false, bodyColor, size, shadow, u)
                val gap = if (i == 0) (if (titleCount > 0) 110 * u * s else 0f) else 34 * u * s
                b += Triple(layout(m, mp, width), gap, false)
            }
            blocks = b
            total = b.sumOf { (it.first.height + it.second).toDouble() }.toFloat()
            if (total <= bottom - top || s < 0.4f) break
            s *= 0.88f
        }

        val avail = bottom - top
        var y = top + max(0f, (avail - total) / 2f) + spec.textOffset * avail * 0.5f
        y = y.coerceIn(top - avail * 0.15f, max(top, bottom - total) + avail * 0.15f)

        var titlesDone = false
        blocks.forEachIndexed { idx, (lay, gap, isTitle) ->
            if (!titlesDone && idx > 0 && !isTitle) {
                titlesDone = true
                // Đường phân cách hoa văn giữa tiêu đề và lời chúc
                divider(cv, w / 2f, y + gap / 2f, 120 * u, ornamentColor, u, spec.style)
            }
            y += gap
            cv.save()
            cv.translate(left, y)
            lay.draw(cv)
            cv.restore()
            y += lay.height
        }

        if (spec.signature.isNotBlank()) {
            val sp = textPaint(spec, Lang.VI, false, bodyColor, 40f * u, shadow, u)
            sp.typeface = Typeface.create(spec.font.family, Typeface.ITALIC)
            sp.textAlign = Paint.Align.RIGHT
            val sx = if (spec.style == CardStyle.EAST_CLASSIC && spec.photo == null) w - 230 * u else right
            cv.drawText("— " + spec.signature.trim(), sx, h - 140 * u, sp)
        }
    }

    private fun textPaint(spec: CardSpec, l: Lang, bold: Boolean, color: Int, size: Float, shadow: Boolean, u: Float): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = typeface(spec, bold)
            textLocale = l.locale
            if (shadow) setShadowLayer(8 * u, 0f, 2 * u, alpha(Color.BLACK, 0.75f))
        }

    private fun divider(cv: Canvas, cx: Float, cy: Float, half: Float, color: Int, u: Float, style: CardStyle) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; strokeWidth = 2.5f * u; this.style = Paint.Style.STROKE }
        val f = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        when (style) {
            CardStyle.EAST_CLASSIC, CardStyle.EAST_MODERN -> {
                cv.drawLine(cx - half, cy, cx - 22 * u, cy, p)
                cv.drawLine(cx + 22 * u, cy, cx + half, cy, p)
                // hình thoi + vòng tròn kiểu đồng tiền
                cv.drawCircle(cx, cy, 12 * u, p)
                val d = Path().apply {
                    moveTo(cx, cy - 7 * u); lineTo(cx + 7 * u, cy); lineTo(cx, cy + 7 * u); lineTo(cx - 7 * u, cy); close()
                }
                cv.drawPath(d, f)
            }
            CardStyle.WEST_CLASSIC -> {
                val path = Path()
                path.moveTo(cx - half, cy)
                path.cubicTo(cx - half * 0.6f, cy - 18 * u, cx - half * 0.3f, cy + 18 * u, cx - 14 * u, cy)
                path.moveTo(cx + half, cy)
                path.cubicTo(cx + half * 0.6f, cy - 18 * u, cx + half * 0.3f, cy + 18 * u, cx + 14 * u, cy)
                cv.drawPath(path, p)
                val d = Path().apply {
                    moveTo(cx, cy - 10 * u); lineTo(cx + 10 * u, cy); lineTo(cx, cy + 10 * u); lineTo(cx - 10 * u, cy); close()
                }
                cv.drawPath(d, f)
            }
            CardStyle.WEST_MODERN -> {
                p.strokeCap = Paint.Cap.ROUND
                p.strokeWidth = 4 * u
                cv.drawLine(cx - half * 0.5f, cy, cx + half * 0.5f, cy, p)
            }
        }
    }

    // ───────────────────────── nền ─────────────────────────

    private fun fillGradient(cv: Canvas, w: Float, h: Float, c1: Int, c2: Int, vertical: Boolean) {
        val p = Paint()
        p.shader = if (vertical) LinearGradient(0f, 0f, 0f, h, c1, c2, Shader.TileMode.CLAMP)
        else LinearGradient(0f, 0f, w, 0f, c1, c2, Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, p)
    }

    private fun vignette(cv: Canvas, w: Float, h: Float, strength: Float) =
        vignetteColor(cv, w, h, alpha(Color.BLACK, strength))

    private fun vignetteColor(cv: Canvas, w: Float, h: Float, edge: Int) {
        val p = Paint()
        p.shader = RadialGradient(w / 2, h / 2, max(w, h) * 0.75f, intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, edge), floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, p)
    }

    private fun drawPhoto(cv: Canvas, w: Float, h: Float, photo: Bitmap, dim: Float) {
        val sw = photo.width.toFloat()
        val sh = photo.height.toFloat()
        val scale = max(w / sw, h / sh)
        val cw = w / scale
        val ch = h / scale
        val sx = ((sw - cw) / 2f).toInt()
        val sy = ((sh - ch) / 2f).toInt()
        val src = Rect(sx, sy, (sx + cw).toInt(), (sy + ch).toInt())
        cv.drawBitmap(photo, src, RectF(0f, 0f, w, h), Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
        val p = Paint()
        p.color = alpha(Color.BLACK, dim)
        cv.drawRect(0f, 0f, w, h, p)
        p.color = Color.BLACK
        p.shader = LinearGradient(0f, h * 0.35f, 0f, h, Color.TRANSPARENT, alpha(Color.BLACK, 0.45f), Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, p)
    }

    private fun latticePattern(cv: Canvas, w: Float, h: Float, u: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 2 * u }
        val step = 70 * u
        var y = 0f
        var row = 0
        while (y < h + step) {
            var x = if (row % 2 == 0) 0f else step / 2
            while (x < w + step) {
                cv.drawCircle(x, y, step * 0.36f, p)
                x += step
            }
            y += step * 0.5f
            row++
        }
    }

    private fun paperGrain(cv: Canvas, w: Float, h: Float, u: Float, rnd: Random, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        repeat(900) {
            cv.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, (0.8f + rnd.nextFloat() * 2.2f) * u, p)
        }
    }

    // ───────────────────────── hoạ tiết Đông phương ─────────────────────────

    private fun orientalFrame(cv: Canvas, w: Float, h: Float, u: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 5 * u }
        val o = 40 * u
        cv.drawRect(o, o, w - o, h - o, p)
        p.strokeWidth = 2 * u
        val i = 58 * u
        cv.drawRect(i, i, w - i, h - i, p)
        // Góc hồi văn 回
        val s = 46 * u
        for ((cx, cy) in listOf(o to o, w - o to o, o to h - o, w - o to h - o)) {
            val f = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 4 * u }
            val bg = Paint().apply { this.color = Color.TRANSPARENT }
            cv.drawRect(cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, bg)
            cv.drawRect(cx - s / 2, cy - s / 2, cx + s / 2, cy + s / 2, f)
            f.strokeWidth = 3 * u
            cv.drawRect(cx - s / 4, cy - s / 4, cx + s / 4, cy + s / 4, f)
            cv.drawPoint(cx, cy, f)
        }
    }

    private fun blossomBranch(cv: Canvas, w: Float, h: Float, u: Float, petal: Int, branch: Int, topLeft: Boolean, rnd: Random) {
        cv.save()
        if (!topLeft) {
            cv.rotate(180f, w / 2, h / 2)
        }
        val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = branch; style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND; strokeWidth = 11 * u
        }
        val main = Path().apply {
            moveTo(-10 * u, 230 * u)
            cubicTo(90 * u, 200 * u, 170 * u, 120 * u, 330 * u, 95 * u)
        }
        cv.drawPath(main, bp)
        bp.strokeWidth = 6 * u
        val twig1 = Path().apply { moveTo(150 * u, 150 * u); quadTo(190 * u, 210 * u, 260 * u, 235 * u) }
        val twig2 = Path().apply { moveTo(230 * u, 110 * u); quadTo(250 * u, 60 * u, 300 * u, 40 * u) }
        cv.drawPath(twig1, bp)
        cv.drawPath(twig2, bp)
        val spots = listOf(
            100f to 195f, 175f to 140f, 255f to 108f, 330f to 95f, 262f to 235f, 210f to 205f, 300f to 42f, 245f to 70f
        )
        spots.forEachIndexed { i, (x, y) ->
            val r = (if (i % 3 == 0) 26f else 19f) * u
            flower5(cv, x * u, y * u, r, petal, rnd.nextFloat() * 72f)
        }
        // nụ hoa
        val bud = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = petal }
        cv.drawCircle(60 * u, 205 * u, 7 * u, bud)
        cv.drawCircle(345 * u, 75 * u, 7 * u, bud)
        cv.restore()
    }

    private fun flower5(cv: Canvas, cx: Float, cy: Float, r: Float, color: Int, rot: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        for (k in 0 until 5) {
            val a = Math.toRadians((rot + k * 72).toDouble())
            cv.drawCircle(cx + (r * 0.55f * cos(a)).toFloat(), cy + (r * 0.55f * sin(a)).toFloat(), r * 0.5f, p)
        }
        p.color = Color.parseColor("#B3261E")
        cv.drawCircle(cx, cy, r * 0.22f, p)
        p.color = Color.parseColor("#FFF3B0")
        for (k in 0 until 6) {
            val a = Math.toRadians((k * 60).toDouble())
            cv.drawCircle(cx + (r * 0.32f * cos(a)).toFloat(), cy + (r * 0.32f * sin(a)).toFloat(), r * 0.06f, p)
        }
    }

    private fun chrysanthemum(cv: Canvas, cx: Float, cy: Float, r: Float, petal: Int, center: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = petal }
        for (layer in 0..2) {
            val n = 22 - layer * 5
            val len = r * (1f - layer * 0.25f)
            for (k in 0 until n) {
                cv.save()
                cv.rotate(k * 360f / n + layer * 9f, cx, cy)
                cv.drawOval(RectF(cx - len * 0.11f, cy - len, cx + len * 0.11f, cy), p)
                cv.restore()
            }
        }
        p.color = center
        cv.drawCircle(cx, cy, r * 0.16f, p)
    }

    private fun seal(cv: Canvas, x: Float, y: Float, s: Float, ch: String, fill: Int, textColor: Int, darkBg: Boolean) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = if (darkBg) Color.parseColor("#E8C25A") else fill }
        cv.drawRoundRect(RectF(x, y, x + s, y + s), 6f, 6f, p)
        val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (darkBg) Color.parseColor("#7A1010") else textColor
            textSize = s * 0.72f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textLocale = java.util.Locale.TRADITIONAL_CHINESE
        }
        val fm = tp.fontMetrics
        cv.drawText(ch, x + s / 2, y + s / 2 - (fm.ascent + fm.descent) / 2, tp)
    }

    private fun inkMountains(cv: Canvas, w: Float, h: Float, u: Float, ink: Int) {
        val layers = listOf(0.07f to 0.70f, 0.12f to 0.78f, 0.2f to 0.87f)
        layers.forEachIndexed { i, (a, base) ->
            val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = alpha(ink, a) }
            val path = Path()
            val y0 = h * base
            path.moveTo(0f, h)
            path.lineTo(0f, y0)
            val peaks = 3 + i
            val seg = w / peaks
            for (k in 0 until peaks) {
                val x0 = k * seg
                val peakY = y0 - (60 + ((k * 37 + i * 53) % 90)) * u * (1.4f - i * 0.25f)
                path.cubicTo(x0 + seg * 0.25f, y0, x0 + seg * 0.35f, peakY, x0 + seg * 0.5f, peakY)
                path.cubicTo(x0 + seg * 0.65f, peakY, x0 + seg * 0.75f, y0, x0 + seg, y0)
            }
            path.lineTo(w, h)
            path.close()
            cv.drawPath(path, p)
        }
    }

    private fun thinBranch(cv: Canvas, w: Float, h: Float, u: Float, ink: Int, blossom: Int, rnd: Random) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ink; style = Paint.Style.STROKE; strokeWidth = 4 * u; strokeCap = Paint.Cap.ROUND }
        val path = Path().apply {
            moveTo(w, 60 * u)
            cubicTo(w - 150 * u, 90 * u, w - 260 * u, 170 * u, w - 420 * u, 190 * u)
        }
        cv.drawPath(path, p)
        val f = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blossom }
        listOf(w - 120 * u to 82 * u, w - 230 * u to 140 * u, w - 330 * u to 178 * u, w - 410 * u to 188 * u, w - 180 * u to 118 * u)
            .forEach { (x, y) -> cv.drawCircle(x + rnd.nextFloat() * 8 * u, y + 10 * u, (7 + rnd.nextFloat() * 6) * u, f) }
    }

    private fun lotus(cv: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = r * 0.05f }
        for (k in -2..2) {
            cv.save()
            cv.rotate(k * 24f, cx, cy)
            val path = Path().apply {
                moveTo(cx, cy)
                cubicTo(cx - r * 0.35f, cy - r * 0.5f, cx - r * 0.2f, cy - r * 0.9f, cx, cy - r)
                cubicTo(cx + r * 0.2f, cy - r * 0.9f, cx + r * 0.35f, cy - r * 0.5f, cx, cy)
            }
            cv.drawPath(path, p)
            cv.restore()
        }
        cv.drawLine(cx - r * 1.2f, cy + r * 0.05f, cx + r * 1.2f, cy + r * 0.05f, p)
    }

    // ───────────────────────── hoạ tiết Tây phương ─────────────────────────

    private fun westernFrame(cv: Canvas, w: Float, h: Float, u: Float, color: Int, accent2: Int, solemn: Boolean) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.STROKE; strokeWidth = 3 * u }
        val o = 48 * u
        val i = 62 * u
        cv.drawRect(o, o, w - o, h - o, p)
        p.strokeWidth = 1.5f * u
        cv.drawRect(i, i, w - i, h - i, p)
        // Hoa văn cuộn ở 4 góc
        val corners = listOf(Triple(o, o, 0f), Triple(w - o, o, 90f), Triple(w - o, h - o, 180f), Triple(o, h - o, 270f))
        p.strokeWidth = 3 * u
        for ((x, y, rot) in corners) {
            cv.save()
            cv.rotate(rot, x, y)
            val path = Path().apply {
                moveTo(x + 10 * u, y + 150 * u)
                cubicTo(x + 10 * u, y + 60 * u, x + 60 * u, y + 10 * u, x + 150 * u, y + 10 * u)
                moveTo(x + 40 * u, y + 40 * u)
                cubicTo(x + 90 * u, y + 30 * u, x + 110 * u, y + 70 * u, x + 80 * u, y + 85 * u)
                cubicTo(x + 60 * u, y + 95 * u, x + 50 * u, y + 70 * u, x + 68 * u, y + 64 * u)
                moveTo(x + 40 * u, y + 40 * u)
                cubicTo(x + 30 * u, y + 90 * u, x + 70 * u, y + 110 * u, x + 85 * u, y + 80 * u)
            }
            cv.drawPath(path, p)
            val f = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = if (solemn) color else accent2 }
            rosette(cv, x + 30 * u, y + 30 * u, 16 * u, f)
            cv.restore()
        }
        // hoạ tiết giữa cạnh trên & dưới
        for (yy in listOf(o, h - o)) {
            val f = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
            val d = Path().apply {
                moveTo(w / 2, yy - 14 * u); lineTo(w / 2 + 14 * u, yy); lineTo(w / 2, yy + 14 * u); lineTo(w / 2 - 14 * u, yy); close()
            }
            cv.drawPath(d, f)
        }
    }

    private fun rosette(cv: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        for (k in 0 until 8) {
            val a = Math.toRadians((k * 45).toDouble())
            cv.drawCircle(cx + (r * 0.6f * cos(a)).toFloat(), cy + (r * 0.6f * sin(a)).toFloat(), r * 0.38f, p)
        }
        val c = Paint(p)
        c.color = Color.WHITE
        cv.drawCircle(cx, cy, r * 0.3f, c)
    }

    private fun bokeh(cv: Canvas, w: Float, h: Float, u: Float, rnd: Random, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(26) {
            val r = (20 + rnd.nextFloat() * 110) * u
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h
            p.shader = RadialGradient(x, y, r, alpha(color, 0.10f + rnd.nextFloat() * 0.18f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            cv.drawCircle(x, y, r, p)
        }
    }

    private fun confetti(cv: Canvas, w: Float, h: Float, u: Float, rnd: Random, colors: IntArray) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        repeat(70) {
            val x = rnd.nextFloat() * w
            val y = if (rnd.nextBoolean()) rnd.nextFloat() * h * 0.22f else h - rnd.nextFloat() * h * 0.18f
            p.color = alpha(colors[rnd.nextInt(colors.size)], 0.85f)
            cv.save()
            cv.rotate(rnd.nextFloat() * 180f, x, y)
            if (rnd.nextInt(3) == 0) cv.drawCircle(x, y, (4 + rnd.nextFloat() * 5) * u, p)
            else cv.drawRect(x - 9 * u, y - 4 * u, x + 9 * u, y + 4 * u, p)
            cv.restore()
        }
        // dải sao lấp lánh
        p.color = alpha(Color.WHITE, 0.9f)
        repeat(10) {
            val x = rnd.nextFloat() * w
            val y = rnd.nextFloat() * h
            star(cv, x, y, (6 + rnd.nextFloat() * 10) * u, p)
        }
    }

    private fun star(cv: Canvas, cx: Float, cy: Float, r: Float, p: Paint) {
        val path = Path()
        path.moveTo(cx, cy - r)
        path.quadTo(cx, cy, cx + r, cy)
        path.quadTo(cx, cy, cx, cy + r)
        path.quadTo(cx, cy, cx - r, cy)
        path.quadTo(cx, cy, cx, cy - r)
        path.close()
        cv.drawPath(path, p)
    }

    @Suppress("unused")
    private fun clamp(v: Float, lo: Float, hi: Float) = min(hi, max(lo, v))
}
