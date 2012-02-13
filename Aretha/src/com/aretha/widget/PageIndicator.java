/*
 * Copyright 2012 Tang Ke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 *        
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aretha.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class PageIndicator extends View {
	private final static int DEFUALT_DOT_SPACING = 8;
	private final static int DEFUALT_DOT_RADIUS = 3;

	private float mScreenScale;

	private int mActiveDotIndex;
	private int mDotNumber;

	private int mDotRadius;
	private int mDotSpacing;

	private Paint mPaint;

	private float[] mDownPoint;
	private OnPageChangeListener mOnPageChangeListener;
	private int mTouchSlop;

	public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public PageIndicator(Context context) {
		super(context);
		initialize();
	}

	private void initialize() {
		mScreenScale = getResources().getDisplayMetrics().density;
		mDotSpacing = (int) (DEFUALT_DOT_SPACING * mScreenScale);
		mDotRadius = (int) (DEFUALT_DOT_RADIUS * mScreenScale);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.WHITE);

		mTouchSlop = new ViewConfiguration().getScaledTouchSlop();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int width = getWidth();
		final int height = getHeight();
		final int dotNumber = mDotNumber;
		final int dotSpacing = mDotSpacing;
		final float dotRadius = mDotRadius;
		int spacingLeft = (width - dotNumber * mDotRadius * 2 - (dotNumber - 1)
				* dotSpacing) / 2;
		int spacingTop = (height - getPaddingTop() - getPaddingBottom()) / 2
				+ getPaddingTop();

		for (int index = 0; index < dotNumber; index++) {
			if (mActiveDotIndex == index) {
				mPaint.setAlpha(255);
			} else {
				mPaint.setAlpha(100);
			}

			canvas.drawCircle(getPaddingLeft() + spacingLeft + index
					* dotRadius * 2 + index * dotSpacing + dotRadius,
					spacingTop, dotRadius, mPaint);
		}

		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int measuredWidth = width;
		int measuredHeight = height;

		final int dotRadius = mDotRadius;

		if (widthMode == MeasureSpec.AT_MOST) {
			int dotWidth = (int) (dotRadius * 2);
			measuredWidth = mDotNumber * dotWidth + (mDotNumber - 1)
					* mDotSpacing + getPaddingLeft() + getPaddingRight();
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			measuredHeight = 2 * dotRadius + getPaddingTop()
					+ getPaddingBottom();
		}

		setMeasuredDimension(measuredWidth, measuredHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final OnPageChangeListener onPageChangeListener = mOnPageChangeListener;
		if (onPageChangeListener == null) {
			return super.onTouchEvent(event);
		}

		int action = event.getAction();
		float y = event.getY();
		float x = event.getX();
		int touchSlop = mTouchSlop;

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mDownPoint == null) {
				mDownPoint = new float[2];
			}
			mDownPoint[0] = x;
			mDownPoint[1] = y;
			return true;
		case MotionEvent.ACTION_UP:
			if (x - mDownPoint[0] <= touchSlop
					&& y - mDownPoint[1] <= touchSlop) {
				if (x < getLeft() + getWidth() / 2) {
					onPageChangeListener.onPrevPage();
				} else {
					onPageChangeListener.onNextPage();
				}
			}
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public void setActivePage(int index) {
		this.mActiveDotIndex = Math.max(0, Math.min(index, mDotNumber));
		invalidate();
	}

	public void setPageNumber(int number) {
		this.mDotNumber = number;
		requestLayout();
		invalidate();
	}

	public int getActivePageIndex() {
		return mActiveDotIndex;
	}

	static class SavedState extends BaseSavedState {
		int activeDotIndex;
		int dotRadius;
		int dotSpacing;
		int dotNumber;

		public SavedState(Parcel source) {
			super(source);
			activeDotIndex = source.readInt();
			dotRadius = source.readInt();
			dotSpacing = source.readInt();
			dotNumber = source.readInt();
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(activeDotIndex);
			dest.writeInt(dotRadius);
			dest.writeInt(dotSpacing);
			dest.writeInt(dotNumber);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<PageIndicator.SavedState>() {

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}

			@Override
			public SavedState createFromParcel(Parcel source) {
				return new SavedState(source);
			}
		};
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		
		savedState.activeDotIndex = mActiveDotIndex;
		savedState.dotNumber = mDotNumber;
		savedState.dotRadius = mDotRadius;
		savedState.dotSpacing = mDotSpacing;
		
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mActiveDotIndex = savedState.activeDotIndex;
		mDotNumber = savedState.dotNumber;
		mDotRadius = savedState.dotRadius;
		mDotSpacing = savedState.dotSpacing;
	}

	public OnPageChangeListener getOnPageChangeListener() {
		return mOnPageChangeListener;
	}

	public void setOnPageChangeListener(
			OnPageChangeListener onPageChangeListener) {
		this.mOnPageChangeListener = onPageChangeListener;
	}

	public static interface OnPageChangeListener {
		public void onPageChange(int pageIndex);

		public void onNextPage();

		public void onPrevPage();
	}
}
