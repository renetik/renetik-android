package renetik.android.imaging

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.Paint
import android.graphics.Paint.Style.STROKE
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.core.graphics.scale
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import renetik.android.core.kotlin.debugClassName
import java.security.MessageDigest


class BorderBitmapTransformation(
    private val borderWidth: Float = 1.5f,
    private val radius: Float = 5f,
    @ColorInt private val color: Int = BLACK) : BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) =
        messageDigest.update(debugClassName.toByteArray(CHARSET))

    private val paint1 = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
        color = this@BorderBitmapTransformation.color
    }

    private val paint2 = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
        xfermode = PorterDuffXfermode(SRC_IN)
    }

    private val stroke = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
        color = this@BorderBitmapTransformation.color
        style = STROKE
        strokeWidth = borderWidth
    }

    override fun transform(pool: BitmapPool, bmp: Bitmap, outWidth: Int,
        outHeight: Int): Bitmap {
        val bitmap = bmp.scale(
            (bmp.getWidth() - borderWidth * 2).toInt(),
            (bmp.getHeight() - borderWidth * 2).toInt(), false)
        val output = createBitmap(bmp.width, bmp.height, ARGB_8888)
        val canvas = Canvas(output)
        val rectF = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawRoundRect(rectF, radius, radius, paint1)

        val src = Rect(0, 0, bitmap.width, bitmap.height)
        val dst = Rect(borderWidth.toInt(), borderWidth.toInt(),
            bmp.width - borderWidth.toInt(), bmp.height - borderWidth.toInt())
        canvas.drawBitmap(bitmap, src, dst, paint2)

        canvas.drawRoundRect(rectF, radius, radius, stroke)
        return output
    }
}