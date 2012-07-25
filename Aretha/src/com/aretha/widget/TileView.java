package com.aretha.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class TileView extends ImageView {
	private Camera mCamera;
	private Matrix mMatrix;
	private int mWidth;
	private int mHeight;

	public TileView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mMatrix = new Matrix();
		mCamera = new Camera();
		mCamera.translate(0, 0, 100);
		// camera.rotateY(50);
		// camera.rotateX(20);
		// camera.getMatrix(mMatrix);

		mMatrix.preTranslate(-150, -150);
		mMatrix.postTranslate(150, 150);
	}

	public TileView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TileView(Context context) {
		this(context, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final Camera camera = mCamera;
		final Matrix matrix = mMatrix;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			camera.save();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			camera.restore();
			break;
		}
//		matrix.preTranslate(mWidth / 2, mHeight / 2);
		invalidate();
		return true;
	}

	private int computeDistanceFromCenter(float x, float y) {
		final float centerX = mWidth / 2;
		final float centerY = mHeight / 2;

		int distance = (int) Math.round(Math.sqrt(Math.pow(centerX - x, 2)
				+ Math.pow(centerY - y, 2)));

		return distance;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.concat(mMatrix);
		super.onDraw(canvas);
	}
}
