package vn.ecard.app.render

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
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
import vn.ecard.app.data.Lang
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Vẽ tranh thư pháp (thủy mặc + chữ) ra Bitmap. */
object PaintingRenderer {

    fun render(spec: PaintingSpec, scale: Float = 1f): Bitmap {
        val w = (spec.aspect.w * scale).toInt()
        val h = (spec.aspect.h * scale).toInt()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        draw(Canvas(bmp), w.toFloat(), h.toFloat(), spec)
        return bmp
    }

    // ───────── tiện ích ─────────
    private fun c(hex: String) = Color.parseColor(hex)
    private fun a(color: Int, al: Float) =
        Color.argb((al * 255).toInt().coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    private fun stroke(color: Int, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; style = Paint.Style.STROKE; strokeWidth = w; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private fun blurred(color: Int, r: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; if (r > 0.5f) maskFilter = BlurMaskFilter(r, BlurMaskFilter.Blur.NORMAL)
    }

    private class Ctx(val w: Float, val h: Float, val u: Float, val area: RectF, val dark: Boolean, val inkMotif: Int, val rnd: Random)

    fun draw(cv: Canvas, w: Float, h: Float, spec: PaintingSpec) {
        val u = w / 1000f
        val photo = spec.photo
        val dark = photo != null || spec.paper == Paper.DARK
        val rnd = Random(spec.headline.hashCode() * 31 + spec.motif.ordinal)

        // ── 1. Nền ──
        var area = RectF(0f, 0f, w, h)
        if (photo != null) {
            drawPhoto(cv, w, h, photo, spec.photoDim)
        } else when (spec.paper) {
            Paper.RICE -> paperBg(cv, w, h, u, c("#F6EEDC"), c("#E6D8BC"), c("#8C6A3C"), rnd)
            Paper.WHITE -> paperBg(cv, w, h, u, c("#FAF8F4"), c("#EEEAE3"), c("#7A7064"), rnd)
            Paper.DARK -> paperBg(cv, w, h, u, c("#1D2026"), c("#0C0E11"), c("#000000"), rnd)
            Paper.STONE -> area = stone(cv, w, h, u, rnd)
        }
        val inkMotif = if (dark) c("#D9D2C2") else c("#1E1A16")
        val ctx = Ctx(w, h, u, area, dark, inkMotif, rnd)

        // ── 2. Họa tiết ──
        if (photo == null) {
            cv.save()
            if (spec.paper == Paper.STONE) {
                val clip = Path().apply { addRoundRect(area, 70 * u, 70 * u, Path.Direction.CW) }
                cv.clipPath(clip)
            }
            if (spec.layout == PaintLayout.HAN_VERTICAL) cv.scale(-1f, 1f, w / 2f, 0f)
            when (spec.motif) {
                Motif.BAMBOO_BOAT -> { mountains(cv, ctx, 0.72f); bamboo(cv, ctx); boat(cv, ctx) }
                Motif.PLUM -> plum(cv, ctx, c("#D32F2F"), c("#6D0F0F"), spec.layout)
                Motif.PEACH -> plum(cv, ctx, c("#F48FB1"), c("#AD1457"), spec.layout)
                Motif.LOTUS -> lotus(cv, ctx)
                Motif.PINE_MOUNTAIN -> { mountains(cv, ctx, 0.70f); pine(cv, ctx) }
                Motif.MOON -> { moon(cv, ctx); mountains(cv, ctx, 0.74f); boat(cv, ctx) }
                Motif.NONE -> {}
            }
            cv.restore()
        }

        // ── 3. Chữ ──
        when (spec.layout) {
            PaintLayout.VN_FLOW -> vnFlow(cv, ctx, spec)
            PaintLayout.VN_STEP -> vnStep(cv, ctx, spec)
            PaintLayout.HAN_VERTICAL -> hanVertical(cv, ctx, spec)
        }
    }

    // ═════════════ NỀN ═════════════

    private fun paperBg(cv: Canvas, w: Float, h: Float, u: Float, c1: Int, c2: Int, grain: Int, rnd: Random) {
        val p = Paint()
        p.shader = LinearGradient(0f, 0f, w * 0.3f, h, c1, c2, Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, p)
        val g = fill(a(grain, 0.08f))
        repeat(2600) { cv.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, (0.6f + rnd.nextFloat() * 1.6f) * u, g) }
        // sợi giấy
        val f = stroke(a(grain, 0.05f), 1.2f * u)
        repeat(160) {
            val x = rnd.nextFloat() * w; val y = rnd.nextFloat() * h
            cv.drawLine(x, y, x + (rnd.nextFloat() - 0.5f) * 60 * u, y + (rnd.nextFloat() - 0.5f) * 20 * u, f)
        }
        val v = Paint()
        v.shader = RadialGradient(w / 2, h / 2, max(w, h) * 0.75f, intArrayOf(Color.TRANSPARENT, Color.TRANSPARENT, a(grain, 0.18f)), floatArrayOf(0f, 0.6f, 1f), Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, v)
    }

    private fun stone(cv: Canvas, w: Float, h: Float, u: Float, rnd: Random): RectF {
        val bg = Paint()
        bg.shader = LinearGradient(0f, 0f, 0f, h, c("#56634F"), c("#232A22"), Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w, h, bg)
        // lá xanh mờ hai bên
        val leaf = blurred(a(c("#7FA36A"), 0.35f), 18 * u)
        repeat(14) {
            val x = if (rnd.nextBoolean()) rnd.nextFloat() * 120 * u else w - rnd.nextFloat() * 120 * u
            cv.drawCircle(x, rnd.nextFloat() * h, (30 + rnd.nextFloat() * 50) * u, leaf)
        }
        val r = RectF(55 * u, 70 * u, w - 55 * u, h - 45 * u)
        cv.drawRoundRect(RectF(r.left + 10 * u, r.top + 18 * u, r.right + 10 * u, r.bottom + 18 * u), 70 * u, 70 * u, blurred(a(Color.BLACK, 0.5f), 20 * u))
        val sp = Paint(Paint.ANTI_ALIAS_FLAG)
        sp.shader = LinearGradient(r.left, r.top, r.right, r.bottom, c("#DDD3BF"), c("#B9AD95"), Shader.TileMode.CLAMP)
        cv.drawRoundRect(r, 70 * u, 70 * u, sp)
        cv.save()
        cv.clipPath(Path().apply { addRoundRect(r, 70 * u, 70 * u, Path.Direction.CW) })
        repeat(7000) {
            val x = r.left + rnd.nextFloat() * r.width(); val y = r.top + rnd.nextFloat() * r.height()
            val v = 130 + rnd.nextInt(100)
            cv.drawCircle(x, y, (0.6f + rnd.nextFloat() * 1.8f) * u, fill(Color.argb(70, v, v - 6, v - 20)))
        }
        val blot = blurred(a(c("#6B5E48"), 0.10f), 25 * u)
        repeat(18) { cv.drawCircle(r.left + rnd.nextFloat() * r.width(), r.top + rnd.nextFloat() * r.height(), (20 + rnd.nextFloat() * 60) * u, blot) }
        cv.drawRoundRect(r, 70 * u, 70 * u, stroke(a(Color.BLACK, 0.25f), 14 * u).apply { maskFilter = BlurMaskFilter(14 * u, BlurMaskFilter.Blur.NORMAL) })
        cv.restore()
        return r
    }

    private fun drawPhoto(cv: Canvas, w: Float, h: Float, photo: Bitmap, dim: Float) {
        val s = max(w / photo.width, h / photo.height)
        val cw = w / s; val ch = h / s
        val sx = ((photo.width - cw) / 2).toInt(); val sy = ((photo.height - ch) / 2).toInt()
        cv.drawBitmap(photo, Rect(sx, sy, (sx + cw).toInt(), (sy + ch).toInt()), RectF(0f, 0f, w, h), Paint(Paint.FILTER_BITMAP_FLAG))
        cv.drawRect(0f, 0f, w, h, fill(a(Color.BLACK, dim)))
    }

    // ═════════════ HỌA TIẾT THỦY MẶC ═════════════

    private fun mountains(cv: Canvas, x: Ctx, baseFrac: Float) {
        val r = x.area; val u = x.u
        val base = r.top + r.height() * baseFrac
        for ((k, al) in listOf(0.16f, 0.26f, 0.40f).withIndex()) {
            val y0 = base + k * 40 * u
            val path = Path().apply {
                moveTo(r.left, r.bottom); lineTo(r.left, y0)
                var px = r.left
                while (px < r.right) {
                    val nx = px + (90 + x.rnd.nextFloat() * 140) * u
                    val peak = y0 - (30 + x.rnd.nextFloat() * 130) * u * (1.2f - k * 0.25f)
                    quadTo((px + nx) / 2, peak, nx, y0 + (x.rnd.nextFloat() - 0.5f) * 30 * u)
                    px = nx
                }
                lineTo(r.right, r.bottom); close()
            }
            val mp = blurred(a(x.inkMotif, al), (10 - k * 3) * u)
            mp.shader = LinearGradient(0f, y0 - 150 * u, 0f, y0 + 120 * u + (r.bottom - y0) * 0.5f, a(x.inkMotif, al), a(x.inkMotif, al * 0.05f), Shader.TileMode.CLAMP)
            cv.drawPath(path, mp)
        }
        // sương
        val mist = Paint()
        val mc = if (x.dark) Color.BLACK else Color.WHITE
        mist.shader = LinearGradient(0f, base - 40 * u, 0f, base + 160 * u, intArrayOf(Color.TRANSPARENT, a(mc, 0.30f), Color.TRANSPARENT), floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP)
        cv.drawRect(r.left, base - 40 * u, r.right, base + 160 * u, mist)
    }

    private fun bambooLeaf(cv: Canvas, x0: Float, y0: Float, ang: Float, len: Float, wd: Float, col: Int) {
        val rad = Math.toRadians(ang.toDouble())
        val ca = cos(rad).toFloat(); val sa = sin(rad).toFloat()
        val p = Path()
        for (k in 0..20) {
            val t = k / 20f; val ww = sin(Math.PI * t).toFloat() * wd * (1f - 0.3f * t)
            val px = x0 + ca * len * t - sa * ww; val py = y0 + sa * len * t + ca * ww
            if (k == 0) p.moveTo(px, py) else p.lineTo(px, py)
        }
        for (k in 20 downTo 0) {
            val t = k / 20f; val ww = sin(Math.PI * t).toFloat() * wd * 0.2f
            p.lineTo(x0 + ca * len * t + sa * ww, y0 + sa * len * t - ca * ww)
        }
        p.close()
        cv.drawPath(p, fill(col))
    }

    private fun bamboo(cv: Canvas, x: Ctx) {
        val r = x.area; val u = x.u
        val stalks = listOf(Triple(r.right - 120 * u, 17f, 0.85f), Triple(r.right - 55 * u, 12f, 0.55f), Triple(r.right - 190 * u, 10f, 0.4f))
        for ((sx, wd, al) in stalks) {
            var y = r.bottom + 20 * u
            val col = a(x.inkMotif, al)
            while (y > r.top + 40 * u) {
                val seg = (110 + x.rnd.nextFloat() * 60) * u
                val sp = Paint(Paint.ANTI_ALIAS_FLAG)
                sp.shader = LinearGradient(sx - wd * u, 0f, sx + wd * u, 0f, a(x.inkMotif, al * 0.6f), col, Shader.TileMode.MIRROR)
                cv.drawRoundRect(RectF(sx - wd / 2 * u, y - seg + 6 * u, sx + wd / 2 * u, y), 3 * u, 3 * u, sp)
                cv.drawLine(sx - wd / 2 * u - 4 * u, y - seg + 5 * u, sx + wd / 2 * u + 4 * u, y - seg + 5 * u, stroke(col, 4 * u))
                y -= seg
                if (x.rnd.nextFloat() < 0.65f) {
                    val n = 3 + x.rnd.nextInt(4)
                    val dir = if (x.rnd.nextBoolean()) 1 else -1
                    repeat(n) {
                        val ang = (if (dir > 0) 200f else 330f) + x.rnd.nextFloat() * 40f - 20f
                        bambooLeaf(cv, sx, y + 10 * u, ang, (70 + x.rnd.nextFloat() * 50) * u, (10 + x.rnd.nextFloat() * 5) * u, a(x.inkMotif, al * 0.9f))
                    }
                }
            }
        }
    }

    private fun boat(cv: Canvas, x: Ctx) {
        val r = x.area; val u = x.u
        val cx = r.left + r.width() * 0.42f; val by = r.bottom - r.height() * 0.09f
        val hull = Path().apply {
            moveTo(cx - 150 * u, by - 10 * u); quadTo(cx, by + 30 * u, cx + 150 * u, by - 14 * u)
            lineTo(cx + 120 * u, by + 12 * u); quadTo(cx, by + 34 * u, cx - 125 * u, by + 10 * u); close()
        }
        cv.drawPath(hull, fill(a(if (x.dark) c("#C9B79A") else c("#4A2E1E"), 0.9f)))
        val roof = RectF(cx - 40 * u, by - 62 * u, cx + 70 * u, by + 6 * u)
        cv.drawArc(roof, 180f, 180f, true, fill(a(if (x.dark) c("#E0CDA8") else c("#9A7445"), 0.9f)))
        val man = a(x.inkMotif, 0.9f)
        cv.drawLine(cx - 95 * u, by - 12 * u, cx - 88 * u, by - 70 * u, stroke(man, 6 * u))
        cv.drawCircle(cx - 87 * u, by - 80 * u, 9 * u, fill(man))
        cv.drawLine(cx - 70 * u, by - 75 * u, cx - 150 * u, by + 30 * u, stroke(man, 3 * u))
        val wave = stroke(a(x.inkMotif, 0.25f), 2 * u)
        for (k in 0..2) cv.drawLine(cx - 170 * u + k * 20 * u, by + 30 * u + k * 10 * u, cx + 170 * u - k * 20 * u, by + 30 * u + k * 10 * u, wave)
    }

    private fun blossom(cv: Canvas, u: Float, cx: Float, cy: Float, r: Float, petal: Int, core: Int, rot: Float) {
        val p = fill(petal)
        for (k in 0 until 5) {
            val ang = Math.toRadians((rot + k * 72).toDouble())
            cv.drawCircle(cx + (r * 0.55f * cos(ang)).toFloat(), cy + (r * 0.55f * sin(ang)).toFloat(), r * 0.52f, p)
        }
        cv.drawCircle(cx, cy, r * 0.2f, fill(core))
        val s = stroke(core, 1.3f * u)
        for (k in 0 until 7) {
            val ang = Math.toRadians(k * 51.0 + rot)
            val ex = cx + (r * 0.55f * cos(ang)).toFloat(); val ey = cy + (r * 0.55f * sin(ang)).toFloat()
            cv.drawLine(cx, cy, ex, ey, s); cv.drawCircle(ex, ey, 2.2f * u, fill(core))
        }
    }

    /** Vẽ nét cành có độ dày thay đổi theo đường cong bậc 2. */
    private fun brushBranch(cv: Canvas, p0x: Float, p0y: Float, p1x: Float, p1y: Float, p2x: Float, p2y: Float, w0: Float, w1: Float, col: Int): List<Pair<Float, Float>> {
        val pts = ArrayList<Pair<Float, Float>>()
        val len = kotlin.math.hypot((p1x - p0x).toDouble(), (p1y - p0y).toDouble()) + kotlin.math.hypot((p2x - p1x).toDouble(), (p2y - p1y).toDouble())
        val n = max(40, (len / max(1.0, min(w0, w1) * 0.25)).toInt()).coerceAtMost(900)
        val paint = fill(col)
        for (i in 0..n) {
            val t = i / n.toFloat()
            val x = (1 - t) * (1 - t) * p0x + 2 * (1 - t) * t * p1x + t * t * p2x
            val y = (1 - t) * (1 - t) * p0y + 2 * (1 - t) * t * p1y + t * t * p2y
            pts += x to y
            cv.drawCircle(x, y, (w0 * (1 - t) + w1 * t) / 2, paint)
        }
        return pts
    }

    private fun plum(cv: Canvas, x: Ctx, petal: Int, core: Int, layout: PaintLayout) {
        val r = x.area; val u = x.u
        val ink = a(x.inkMotif, 0.92f)
        val vertical = layout == PaintLayout.VN_STEP
        val main = if (vertical)
            brushBranch(cv, r.right - 40 * u, r.top - 10 * u, r.right - 260 * u, r.top + r.height() * 0.45f, r.right - 90 * u, r.bottom + 10 * u, 10 * u, 26 * u, ink)
        else
            brushBranch(cv, r.right + 10 * u, r.top + 120 * u, r.right - 200 * u, r.top + r.height() * 0.35f, r.right - 60 * u, r.bottom - 60 * u, 24 * u, 8 * u, ink)
        val flowers = ArrayList<Pair<Float, Float>>()
        for (k in 1..8) {
            val (bx, by) = main[(k * main.size / 9).coerceAtMost(main.size - 1)]
            val dir = if (k % 2 == 0) -1f else 0.35f
            val ex = bx + dir * (90 + x.rnd.nextFloat() * 110) * u
            val ey = by - (20 + x.rnd.nextFloat() * 90) * u
            val br = brushBranch(cv, bx, by, (bx + ex) / 2, by - 40 * u, ex, ey, 9 * u, 2.5f * u, ink)
            flowers += ex to ey
            flowers += br[br.size / 2]
            if (x.rnd.nextBoolean()) {
                val (mx, my) = br[br.size * 2 / 3]
                val tx = mx + dir * 50 * u; val ty = my - 50 * u
                brushBranch(cv, mx, my, mx, my - 25 * u, tx, ty, 4 * u, 1.5f * u, ink)
                cv.drawCircle(tx, ty, 6 * u, fill(petal))
            }
        }
        for ((fx, fy) in flowers) blossom(cv, u, fx, fy, (20 + x.rnd.nextFloat() * 12) * u, petal, core, x.rnd.nextFloat() * 72)
        // nụ
        repeat(6) {
            val (bx, by) = main[x.rnd.nextInt(main.size)]
            cv.drawCircle(bx + (x.rnd.nextFloat() - 0.5f) * 30 * u, by + (x.rnd.nextFloat() - 0.5f) * 30 * u, 7 * u, fill(petal))
        }
    }

    private fun lotus(cv: Canvas, x: Ctx) {
        val r = x.area; val u = x.u
        mountains(cv, x, 0.80f)
        val cx = r.right - r.width() * 0.27f; val base = r.bottom - r.height() * 0.07f
        // lá sen mực
        cv.drawOval(RectF(cx - 260 * u, base - 40 * u, cx + 40 * u, base + 40 * u), blurred(a(x.inkMotif, 0.55f), 6 * u))
        cv.drawOval(RectF(cx - 20 * u, base - 20 * u, cx + 220 * u, base + 45 * u), blurred(a(x.inkMotif, 0.4f), 6 * u))
        // cọng + hoa
        val fy = base - 230 * u
        brushBranch(cv, cx - 60 * u, base, cx - 40 * u, base - 120 * u, cx, fy + 40 * u, 7 * u, 5 * u, a(x.inkMotif, 0.8f))
        val pink = c("#F8BBD0"); val edge = c("#C2185B")
        for ((ang, len) in listOf(-62f to 120f, 62f to 120f, -32f to 150f, 32f to 150f, 0f to 170f)) {
            cv.save(); cv.rotate(ang, cx, fy + 40 * u)
            val p = Path().apply {
                val ox = cx; val oy = fy + 40 * u
                moveTo(ox, oy); cubicTo(ox - 45 * u, oy - len * 0.4f * u, ox - 25 * u, oy - len * 0.85f * u, ox, oy - len * u)
                cubicTo(ox + 25 * u, oy - len * 0.85f * u, ox + 45 * u, oy - len * 0.4f * u, ox, oy); close()
            }
            val pp = Paint(Paint.ANTI_ALIAS_FLAG)
            pp.shader = LinearGradient(0f, fy + 40 * u, 0f, fy + 40 * u - len * u, Color.WHITE, pink, Shader.TileMode.CLAMP)
            cv.drawPath(p, pp); cv.drawPath(p, stroke(a(edge, 0.5f), 1.6f * u))
            cv.restore()
        }
        // nụ sen
        brushBranch(cv, cx + 80 * u, base, cx + 100 * u, base - 100 * u, cx + 120 * u, base - 170 * u, 5 * u, 4 * u, a(x.inkMotif, 0.75f))
        cv.drawOval(RectF(cx + 98 * u, base - 230 * u, cx + 142 * u, base - 160 * u), fill(pink))
    }

    private fun pine(cv: Canvas, x: Ctx) {
        val r = x.area; val u = x.u
        val ink = a(x.inkMotif, 0.9f)
        val main = brushBranch(cv, r.right + 10 * u, r.top + 60 * u, r.right - 170 * u, r.top + 40 * u, r.right - 300 * u, r.top + 150 * u, 20 * u, 7 * u, ink)
        val needle = stroke(a(c(if (x.dark) "#B9C9A8" else "#2E4A30"), 0.85f), 3 * u)
        for (k in 2..8) {
            val (bx, by) = main[k * main.size / 9]
            val cxn = bx + (x.rnd.nextFloat() - 0.5f) * 40 * u; val cyn = by + 20 * u
            for (j in 0 until 16) {
                val ang = Math.toRadians(190.0 + j * (160.0 / 15))
                cv.drawLine(cxn, cyn, cxn + (48 * u * cos(ang)).toFloat(), cyn + (34 * u * sin(ang)).toFloat() + 30 * u, needle)
            }
        }
    }

    private fun moon(cv: Canvas, x: Ctx) {
        val r = x.area; val u = x.u
        val mx = r.right - r.width() * 0.25f; val my = r.top + r.height() * 0.2f
        val g = Paint(Paint.ANTI_ALIAS_FLAG)
        g.shader = RadialGradient(mx, my, 260 * u, a(c("#FFF3C4"), if (x.dark) 0.45f else 0.6f), Color.TRANSPARENT, Shader.TileMode.CLAMP)
        cv.drawCircle(mx, my, 260 * u, g)
        cv.drawCircle(mx, my, 120 * u, fill(a(c(if (x.dark) "#F6E7B0" else "#F2D58A"), 0.9f)))
    }

    // ═════════════ CHỮ ═════════════

    private fun inkColor(ink: Ink, dark: Boolean) = c(if (dark) ink.onDark else ink.hex)

    private fun vnPaint(f: CardFont, size: Float, color: Int, dark: Boolean, u: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = CardFonts.typeface(f, Typeface.NORMAL); textSize = size; this.color = color
        textLocale = Lang.VI.locale
        if (dark) setShadowLayer(6 * u, 0f, 2 * u, a(Color.BLACK, 0.7f))
    }

    private fun seal(cv: Canvas, u: Float, x: Float, y: Float, s: Float, ch: String) {
        if (ch.isBlank()) return
        cv.drawRoundRect(RectF(x, y, x + s, y + s), 6 * u, 6 * u, fill(c("#B71C1C")))
        cv.drawRoundRect(RectF(x + 4 * u, y + 4 * u, x + s - 4 * u, y + s - 4 * u), 4 * u, 4 * u, stroke(c("#F6E3C8"), 1.5f * u))
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = CardFonts.han(); textSize = s * 0.68f; color = c("#FBEBD6"); textAlign = Paint.Align.CENTER
            textLocale = Locale.TRADITIONAL_CHINESE
        }
        val fm = p.fontMetrics
        cv.drawText(ch.take(1), x + s / 2, y + s / 2 - (fm.ascent + fm.descent) / 2, p)
    }

    private fun motifRightReserve(spec: PaintingSpec, w: Float): Float = when {
        spec.photo != null || spec.motif == Motif.NONE -> 0.08f * w
        spec.motif == Motif.BAMBOO_BOAT -> 0.30f * w
        spec.motif == Motif.PLUM || spec.motif == Motif.PEACH -> 0.30f * w
        spec.motif == Motif.PINE_MOUNTAIN || spec.motif == Motif.MOON -> 0.22f * w
        else -> 0.12f * w
    }

    private fun motifBottomReserve(spec: PaintingSpec, h: Float, area: RectF): Float = when {
        spec.photo != null || spec.motif == Motif.NONE -> 0.06f * h
        spec.motif == Motif.PLUM || spec.motif == Motif.PEACH -> 0.06f * h
        spec.motif == Motif.LOTUS -> area.height() * 0.36f
        else -> area.height() * 0.24f
    }

    private fun vnFlow(cv: Canvas, x: Ctx, spec: PaintingSpec) {
        val u = x.u; val w = x.w; val r = x.area
        val left = r.left + 0.07f * w
        val right = r.right - motifRightReserve(spec, w)
        val textW = right - left
        val bottom = r.bottom - motifBottomReserve(spec, x.h, r)
        val words = spec.headline.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val l1 = words.firstOrNull() ?: ""
        val l2 = words.drop(1).joinToString(" ")
        val lines = spec.body.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        val hc = inkColor(spec.headInk, x.dark); val bc = inkColor(spec.bodyInk, x.dark)

        var s = spec.textScale
        repeat(10) {
            val big = 260f * u * s * spec.headFont.scale / 1.2f
            val hp = vnPaint(spec.headFont, big, hc, x.dark, u)
            val indent2 = if (l2.isNotEmpty()) 0.13f * w else 0f
            val headW = max(hp.measureText(l1), indent2 + hp.measureText(l2))
            val bp = vnPaint(spec.bodyFont, 52f * u * s * spec.bodyFont.scale / 1.3f, bc, x.dark, u)
            val bodyW = (lines.maxOfOrNull { bp.measureText(it) } ?: 0f) + 0.08f * w
            val headH = big * (if (l2.isNotEmpty()) 1.65f else 1.0f)
            val total = headH + 0.04f * w + lines.size * bp.textSize * 1.45f + (if (spec.source.isNotBlank()) bp.textSize * 1.4f else 0f)
            if ((headW <= textW && bodyW <= textW && total <= bottom - r.top - 0.06f * w) || s < 0.35f) {
                var y = r.top + 0.06f * w + big * 0.85f
                cv.drawText(l1, left, y, hp)
                if (l2.isNotEmpty()) { y += big * 0.8f; cv.drawText(l2, left + indent2, y, hp) }
                y += 0.04f * w + bp.textSize * 0.6f
                val ind = floatArrayOf(0f, 0.05f, 0.015f, 0.07f, 0.03f, 0.06f)
                var lastX = left
                lines.forEachIndexed { i, t ->
                    y += bp.textSize * 1.45f
                    val lx = left + ind[i % ind.size] * w
                    cv.drawText(t, lx, y, bp); lastX = max(lastX, lx + bp.measureText(t))
                }
                if (spec.source.isNotBlank()) {
                    y += bp.textSize * 1.4f
                    val sp = vnPaint(spec.bodyFont, bp.textSize * 0.7f, a(bc, 0.8f), x.dark, u)
                    sp.textAlign = Paint.Align.RIGHT
                    cv.drawText("— " + spec.source, min(lastX, right), y, sp)
                }
                val ss = 52 * u
                seal(cv, u, min(lastX + 0.02f * w, right - ss), y + 0.02f * w, ss, spec.seal)
                return
            }
            s *= 0.9f
        }
    }

    private fun vnStep(cv: Canvas, x: Ctx, spec: PaintingSpec) {
        val u = x.u; val w = x.w; val r = x.area
        val left = r.left + 0.06f * w
        val right = r.right - motifRightReserve(spec, w)
        val bottom = r.bottom - 0.06f * x.h
        val heads = spec.headline.split("/").map { it.trim() }.filter { it.isNotEmpty() }
        val groups = spec.body.split(Regex("\\n\\s*\\n")).map { g -> g.split("\n").map { it.trim() }.filter { it.isNotEmpty() } }
        val hc = inkColor(spec.headInk, x.dark); val bc = inkColor(spec.bodyInk, x.dark)
        var s = spec.textScale
        repeat(10) {
            val big = 230f * u * s * spec.headFont.scale / 1.2f
            val hp = vnPaint(spec.headFont, big, hc, x.dark, u)
            val bp = vnPaint(spec.bodyFont, 62f * u * s * spec.bodyFont.scale / 1.3f, bc, x.dark, u)
            var total = 0f
            var maxW = 0f
            heads.forEachIndexed { i, hd ->
                total += big * 1.0f
                maxW = max(maxW, hp.measureText(hd))
                val g = groups.getOrNull(i).orEmpty()
                g.forEachIndexed { k, t -> total += bp.textSize * 1.3f; maxW = max(maxW, 0.1f * w + k * 0.035f * w + bp.measureText(t)) }
                total += 0.05f * w
            }
            if ((total <= bottom - r.top - 0.06f * w && maxW <= right - left) || s < 0.35f) {
                var y = r.top + 0.05f * w
                heads.forEachIndexed { i, hd ->
                    y += big * 0.95f
                    cv.drawText(hd, left, y, hp)
                    y += big * 0.08f
                    groups.getOrNull(i).orEmpty().forEachIndexed { k, t ->
                        y += bp.textSize * 1.3f
                        cv.drawText(t, left + 0.1f * w + k * 0.035f * w, y, bp)
                    }
                    y += 0.05f * w
                }
                seal(cv, u, left + 0.02f * w, min(y, bottom - 60 * u), 56 * u, spec.seal)
                return
            }
            s *= 0.9f
        }
    }

    private fun hanPaint(size: Float, color: Int, dark: Boolean, u: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = CardFonts.han(); textSize = size; this.color = color; textAlign = Paint.Align.CENTER
        textLocale = Locale.TRADITIONAL_CHINESE
        if (dark) setShadowLayer(6 * u, 0f, 2 * u, a(Color.BLACK, 0.7f))
    }

    private fun drawCharCentered(cv: Canvas, ch: String, cx: Float, cy: Float, p: Paint) {
        val fm = p.fontMetrics
        cv.drawText(ch, cx, cy - (fm.ascent + fm.descent) / 2, p)
    }

    private fun hanVertical(cv: Canvas, x: Ctx, spec: PaintingSpec) {
        val u = x.u; val w = x.w; val r = x.area
        val pad = 0.07f * w
        val top = r.top + pad
        val bottom = r.bottom - motifBottomReserve(spec, x.h, r) - 0.02f * w
        val regionH = bottom - top
        val headChars = spec.headline.filter { !it.isWhitespace() }.map { it.toString() }
        val hc = inkColor(spec.headInk, x.dark); val bc = inkColor(spec.bodyInk, x.dark)

        // cột chữ lớn bên phải
        val n = max(1, headChars.size)
        val bigS = min(0.17f * w * spec.textScale, regionH / n * 0.98f)
        val bigX = r.right - pad - bigS / 2
        val hp = hanPaint(bigS, hc, x.dark, u)
        headChars.forEachIndexed { i, ch -> drawCharCentered(cv, ch, bigX, top + bigS * (i + 0.5f), hp) }
        seal(cv, u, bigX - 26 * u, min(top + bigS * n + 0.02f * w, bottom - 52 * u), 52 * u, spec.seal)

        // các cột chữ nhỏ, đọc từ phải sang trái
        val clauses = spec.body.split(Regex("[，。；？！、,.;?!]")).map { it.trim() }.filter { it.isNotEmpty() }
        val minLeft = r.left + pad + (if (spec.photo == null && (spec.motif == Motif.BAMBOO_BOAT || spec.motif == Motif.PLUM || spec.motif == Motif.PEACH)) 0.24f * w else 0f)
        var small = 0.055f * w * spec.textScale
        repeat(12) {
            val perCol = max(1, (regionH / (small * 1.12f)).toInt())
            val cols = ArrayList<String>()
            for (cl in clauses) cl.chunked(perCol).forEach { cols += it }
            val startX = bigX - bigS / 2 - 0.06f * w - small / 2
            val needLeft = startX - (cols.size - 1) * small * 1.45f - small / 2
            val srcH = if (spec.source.isNotBlank()) small * 1.4f else 0f
            if (needLeft >= minLeft || small < 18 * u) {
                val bp = hanPaint(small, bc, x.dark, u)
                cols.forEachIndexed { ci, col ->
                    val cx = startX - ci * small * 1.45f
                    col.forEachIndexed { k, ch -> drawCharCentered(cv, ch.toString(), cx, top + small * 1.12f * (k + 0.5f), bp) }
                }
                if (spec.source.isNotBlank()) {
                    val longest = cols.maxOfOrNull { it.length } ?: 0
                    val sy = top + small * 1.12f * longest + srcH
                    val sp = Paint(hanPaint(small * 0.62f, a(bc, 0.8f), x.dark, u)).apply { textAlign = Paint.Align.RIGHT }
                    cv.drawText("— " + spec.source, startX + small / 2, min(sy, bottom), sp)
                }
                return
            }
            small *= 0.9f
        }
    }
}
