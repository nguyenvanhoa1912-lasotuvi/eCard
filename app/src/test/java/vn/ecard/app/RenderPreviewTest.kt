package vn.ecard.app

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import vn.ecard.app.data.Lang
import vn.ecard.app.data.Occasion
import vn.ecard.app.data.PhraseLibrary
import vn.ecard.app.render.CardFont
import vn.ecard.app.render.CardFonts
import vn.ecard.app.render.CardRenderer
import vn.ecard.app.render.CardSpec
import vn.ecard.app.render.ThemeFrames
import java.io.File
import java.io.FileOutputStream

/** Vẽ thử toàn bộ khung chủ đề ra PNG để kiểm tra bố cục (chạy trên GitHub Actions). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RenderPreviewTest {

    @Test
    fun renderAllFrames() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        CardFonts.init(ctx)
        val out = File("build/previews").apply { mkdirs() }
        val occs = listOf(Occasion.TET, Occasion.BIRTHDAY, Occasion.WEDDING, Occasion.LONGEVITY, Occasion.CONDOLENCE, Occasion.MID_AUTUMN)
        for (o in occs) {
            val ph = PhraseLibrary.byOccasion(o).first()
            for (f in 1..ThemeFrames.labels(o).size) {
                val font = listOf(CardFont.SERIF, CardFont.CALLI_BOLD, CardFont.CALLI_LIGHT, CardFont.CALLI_BRUSH)[(o.ordinal + f) % 4]
                val spec = CardSpec(
                    occasion = o, frame = f,
                    langs = listOf(Lang.VI, Lang.ZH),
                    message = Lang.values().associateWith { ph.text.get(it) },
                    signature = "Gia đình Hòa",
                    font = font,
                    bigWord = if (f == 1 && (o == Occasion.TET || o == Occasion.LONGEVITY)) (if (o == Occasion.TET) "Phúc" else "Thọ") else ""
                )
                val bmp = CardRenderer.render(spec, 0.5f)
                FileOutputStream(File(out, "${o.ordinal.toString().padStart(2, '0')}_${o.name}_$f.png")).use {
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
    }
}
