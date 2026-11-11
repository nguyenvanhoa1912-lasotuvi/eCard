package vn.ecard.app

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import vn.ecard.app.render.CardFonts
import vn.ecard.app.render.PaintPresets
import vn.ecard.app.render.PaintingRenderer
import java.io.File
import java.io.FileOutputStream

/** Vẽ thử toàn bộ mẫu tranh thư pháp ra PNG (chạy trên GitHub Actions). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PaintingPreviewTest {
    @Test
    fun renderAllPresets() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        CardFonts.init(ctx)
        val out = File("build/previews_paint").apply { mkdirs() }
        PaintPresets.all.forEachIndexed { i, p ->
            val bmp = PaintingRenderer.render(p.toSpec(), 0.5f)
            FileOutputStream(File(out, "${i.toString().padStart(2, '0')}_${p.layout.name}.png")).use {
                bmp.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
