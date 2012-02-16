package com.aretha.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ReflectionImageView extends ImageView {
	private Bitmap mReflectionBitmap;

	private Paint mPaint;

	public ReflectionImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public ReflectionImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public ReflectionImageView(Context context) {
		super(context);
		initialize();
	}

	private void initialize() {
		mPaint = new Paint();
		mPaint.setColor(0xffffff00);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		buildReflectionBitmapCache();
		Rect rect = canvas.getClipBounds();
		rect.bottom += 200;
		canvas.clipRect(rect);
		canvas.drawCircle(0, rect.bottom, 40, mPaint);
		// if (mReflectionBitmap != null) {
		// Rect rect = canvas.getClipBounds();
		// rect.bottom += 200;
		// canvas.clipRect(rect);
		// }
	}

	protected void buildReflectionBitmapCache() {

	}
}
