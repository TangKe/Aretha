package com.aretha.widget.graphic;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;

public class BitmapEffectBuilder {
	private static BitmapEffectBuilder mBitmapEffectBuilder;

	private Paint mPaint;
	private Matrix mMatrix;
	private Canvas mCanvas;

	/**
	 * Resources for outer glow
	 */
	private Paint mBlurPaint;

	private BitmapEffectBuilder() {
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		mBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

		mMatrix = new Matrix();
		mCanvas = new Canvas();
	}

	public static BitmapEffectBuilder getInstance() {
		if (null == mBitmapEffectBuilder) {
			mBitmapEffectBuilder = new BitmapEffectBuilder();
		}
		return mBitmapEffectBuilder;
	}

	/**
	 * Draw {@link Bitmap} with outer glow
	 * 
	 * @param bitmap
	 * @param radius
	 * @param glowColor
	 * @param needMerge
	 * @return
	 */
	public Bitmap buildOuterGlow(Bitmap bitmap, float radius, int glowColor,
			boolean needMerge) {
		return buildBlurLayer(bitmap, radius, glowColor, 0, 0, Blur.OUTER,
				needMerge);
	}

	/**
	 * Draw {@link Bitmap} with shadow
	 * 
	 * @param bitmap
	 * @param radius
	 * @param shadowColor
	 * @param xOffset
	 * @param yOffset
	 * @param needMerge
	 * @return
	 */
	public Bitmap buildDropShadow(Bitmap bitmap, float radius, int shadowColor,
			int xOffset, int yOffset, boolean needMerge) {
		return buildBlurLayer(bitmap, radius, shadowColor, xOffset, yOffset,
				Blur.NORMAL, needMerge);
	}

	public Bitmap buildBlurLayer(Bitmap bitmap, float radius, int glowColor,
			int xOffset, int yOffset, Blur blur, boolean needMerge) {
		if (null == bitmap) {
			return null;
		}
		final Paint blurPaint = mBlurPaint;

		int lengthToExtend = (int) Math.ceil(radius) * 2;
		Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getWidth()
				+ lengthToExtend, bitmap.getHeight() + lengthToExtend,
				Config.ARGB_8888);
		final Canvas canvas = mCanvas;
		canvas.setBitmap(canvasBitmap);
		Bitmap alphaBitmap = bitmap.extractAlpha();

		blurPaint.setMaskFilter(new BlurMaskFilter(radius, blur));
		blurPaint.setColor(glowColor);
		canvas.drawBitmap(alphaBitmap, radius + xOffset, radius + yOffset,
				blurPaint);

		if (needMerge) {
			canvas.drawBitmap(bitmap, radius, radius, mPaint);
		}
		alphaBitmap.recycle();
		return canvasBitmap;
	}

	/**
	 * Draw {@link Bitmap} with reflection
	 * 
	 * @param bitmap
	 * @param reflectionPercent
	 * @param needMerge
	 * @return
	 */
	public Bitmap buildReflection(Bitmap bitmap, float reflectionPercent,
			boolean needMerge) {
		if (null == bitmap) {
			return null;
		}
		final Paint paint = mPaint;
		final Matrix matrix = mMatrix;
		matrix.reset();
		matrix.setScale(1, -1);

		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int reflectionY = needMerge ? height : 0;
		Bitmap reflection = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		Bitmap canvasBitmap = Bitmap.createBitmap(width, height + reflectionY,
				Config.ARGB_8888);
		final Canvas canvas = mCanvas;
		canvas.setBitmap(canvasBitmap);

		canvas.drawBitmap(reflection, 0, reflectionY, paint);
		canvas.save();
		paint.setShader(new LinearGradient(0, reflectionY, 0, reflectionY
				+ height * reflectionPercent, Color.BLACK, Color.TRANSPARENT,
				TileMode.CLAMP));
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		canvas.drawRect(0, reflectionY, width, needMerge ? 2 * height : height,
				paint);
		canvas.restore();
		paint.setShader(null);
		paint.setXfermode(null);

		if (needMerge) {
			canvas.drawBitmap(bitmap, 0, 0, paint);
		}

		reflection.recycle();

		return canvasBitmap;
	}

	/**
	 * Make a gradient cover on the origin image
	 * 
	 * @param bitmap
	 * @param highlightStartColor
	 * @param highlightEndColor
	 * @param highlightPercent
	 * @param needMerge
	 * @return
	 */
	public Bitmap buildHighlight(Bitmap bitmap, int highlightStartColor,
			int highlightEndColor, float highlightPercent, boolean needMerge) {
		final Paint paint = mPaint;

		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		Bitmap canvasBitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		final Canvas canvas = mCanvas;
		canvas.setBitmap(canvasBitmap);

		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.save();
		paint.setShader(new LinearGradient(0, 0, 0, height * highlightPercent,
				highlightStartColor, highlightEndColor, TileMode.CLAMP));
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
		canvas.drawRect(0, 0, width, height * highlightPercent, paint);
		canvas.restore();
		paint.setShader(null);
		paint.setXfermode(null);

		return canvasBitmap;
	}
}
