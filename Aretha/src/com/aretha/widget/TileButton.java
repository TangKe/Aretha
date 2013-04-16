/* Copyright (c) 2011-2012 Tang Ke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aretha.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Scroller;

import com.aretha.R;

public class TileButton extends Button {
	private Camera mCamera;
	private Matrix mMatrix;
	private int mCenterX;
	private int mCenterY;

	private int[] mCurrentRotate;
	private int mCurrentDepth;

	private int mMaxDepth;
	private int mMaxRotateDegree;

	private Scroller mScroller;
	private Scroller mDepthScroller;

	public TileButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TileButton, defStyle, 0);

		mMaxDepth = a.getInt(R.styleable.TileButton_maxDepth, 100);
		mMaxRotateDegree = Math.abs(a.getInt(
				R.styleable.TileButton_maxRotateDegree, 15));

		a.recycle();

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
		final int maxRotateDegree = mMaxRotateDegree;
		rotate[0] = (int) Math.min(Math.max(-(y - centerY) * maxRotateDegree
				/ centerY, -maxRotateDegree), maxRotateDegree);
		rotate[1] = (int) Math.min(Math.max((x - centerX) * maxRotateDegree
				/ centerX, -maxRotateDegree), maxRotateDegree);
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
		canvas.save();
		canvas.concat(mMatrix);
		super.draw(canvas);
		canvas.restore();
	}

	public int getMaxDepth() {
		return mMaxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.mMaxDepth = maxDepth;
	}

	public int getMaxRotateDegree() {
		return mMaxRotateDegree;
	}

	public void setMaxRotateDegree(int maxRotateDegree) {
		this.mMaxRotateDegree = maxRotateDegree;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.maxDepth = mMaxDepth;
		savedState.maxRotateDegree = mMaxRotateDegree;
		return savedState;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mMaxDepth = savedState.maxDepth;
		mMaxRotateDegree = savedState.maxRotateDegree;
	}

	/**
	 * Base class for save the state of this view.
	 * 
	 * @author Tank
	 * 
	 */
	static class SavedState extends BaseSavedState {
		public int maxDepth;
		public int maxRotateDegree;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			maxDepth = in.readInt();
			maxRotateDegree = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(maxDepth);
			out.writeInt(maxRotateDegree);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
