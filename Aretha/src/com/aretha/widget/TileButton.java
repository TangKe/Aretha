package com.aretha.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Scroller;

public class TileButton extends Button {
	private Camera mCamera;
	private Matrix mMatrix;
	private int mCenterX;
	private int mCenterY;

	private int[] mCurrentRotate;
	private int mCurrentDepth;

	private int mMaxDepth = 100;

	private Scroller mScroller;
	private Scroller mDepthScroller;

	public TileButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mCamera = new Camera();
		mMatrix = new Matrix();
		mScroller = new Scroller(context);
		mDepthScroller = new Scroller(context);

		mCurrentRotate = new int[2];
	}

	public TileButton(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.buttonStyle);
	}

	public TileButton(Context context) {
		this(context, null);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int centerX = mCenterX;
		final int centerY = mCenterY;
		final float x = event.getX();
		final float y = event.getY();
		final int[] currentRotate = mCurrentRotate;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			final int[] rotate = new int[2];
			mDepthScroller.startScroll(mCurrentDepth, 0, mMaxDepth
					- mCurrentDepth, 0);
			computeRotate(rotate, x, y, centerX, centerY);
			mScroller.startScroll(currentRotate[0], currentRotate[1], rotate[0]
					- currentRotate[0], rotate[1] - currentRotate[1]);
			break;
		case MotionEvent.ACTION_MOVE:
			computeRotate(currentRotate, x, y, centerX, centerY);
			applyRotate(currentRotate[0], currentRotate[1], mCurrentDepth);
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_UP:
			mDepthScroller.startScroll(mCurrentDepth, 0, 0 - mCurrentDepth, 0);
			mScroller.startScroll(currentRotate[0], currentRotate[1],
					0 - currentRotate[0], 0 - currentRotate[1]);
			break;
		}
		invalidate();
		requestLayout();
		return super.onTouchEvent(event);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		final int[] currentRotate = mCurrentRotate;
		if (mDepthScroller.computeScrollOffset()) {
			mCurrentDepth = mDepthScroller.getCurrX();
			applyRotate(currentRotate[0], currentRotate[1], mCurrentDepth);
			invalidate();
			requestLayout();
		}

		if (mScroller.computeScrollOffset()) {
			currentRotate[0] = mScroller.getCurrX();
			currentRotate[1] = mScroller.getCurrY();
			applyRotate(currentRotate[0], currentRotate[1], mCurrentDepth);
			invalidate();
			requestLayout();
		}
	}

	protected void computeRotate(int[] rotate, float x, float y, int centerX,
			int centerY) {
		rotate[0] = (int) Math.min(
				Math.max(-(y - centerY) * 15 / centerY, -15), 15);
		rotate[1] = (int) Math.min(Math.max((x - centerX) * 15 / centerX, -15),
				15);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCenterX = w / 2;
		mCenterY = h / 2;
	}

	private void applyRotate(float x, float y, float depth) {
		final Camera camera = mCamera;
		final Matrix matrix = mMatrix;
		camera.save();
		camera.translate(0.0f, 0.0f, depth);
		camera.rotateY(y);
		camera.rotateX(x);
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-mCenterX, -mCenterY);
		matrix.postTranslate(mCenterX, mCenterY);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.concat(mMatrix);
		super.draw(canvas);
	}
}
