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

import com.aretha.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

/**
 * A simple widget to show the progress just like Windows Phone 7
 * 
 * @author Tank
 * 
 */
public class AccelerateLinearDecelerateProgressBar extends View {
	private final static float LIEAR_REGION_START_PERCENT = 0.35f;
	private final static float LIEAR_REGION_END_PERCENT = 0.65f;

	private Interpolator mAccelerateInterpolator;
	private Interpolator mLinearInterpolator;
	private Interpolator mDecelerateInterpolator;

	private float mDotRadius;
	private float mDotSpacing;
	private int mDotCount;
	private int mDuration;
	private int mDotColor;

	private int mHeight;
	private int mWidht;
	private int mLinearRegionStartX;
	private int mLinearRegionEndX;

	private Paint mPaint;

	private Scroller mScroller;

	public AccelerateLinearDecelerateProgressBar(Context context,
			AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AccelerateLinearDecelerateProgressBar);

		mDotRadius = a.getDimension(
				R.styleable.AccelerateLinearDecelerateProgressBar_dotRadius, 2);
		mDotSpacing = a.getDimension(
				R.styleable.AccelerateLinearDecelerateProgressBar_dotSpacing,
				20);
		mDotCount = a.getInt(
				R.styleable.AccelerateLinearDecelerateProgressBar_dotCount, 5);
		mDuration = a.getInt(
				R.styleable.AccelerateLinearDecelerateProgressBar_duration,
				4000);
		mDotColor = a.getColor(
				R.styleable.AccelerateLinearDecelerateProgressBar_dotColor,
				Color.WHITE);

		a.recycle();

		initialize();
	}

	public AccelerateLinearDecelerateProgressBar(Context context,
			AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AccelerateLinearDecelerateProgressBar(Context context) {
		this(context, null);
	}

	private void initialize() {
		mAccelerateInterpolator = new AccelerateInterpolator(2f);
		mLinearInterpolator = new LinearInterpolator();
		mDecelerateInterpolator = new DecelerateInterpolator(2f);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(mDotColor);

		mScroller = new Scroller(getContext(), mLinearInterpolator);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);

		if (heightMode == MeasureSpec.AT_MOST) {
			measuredHeight = (int) Math.ceil(2 * mDotRadius);
		}
		measuredWidth += (getPaddingLeft() + getPaddingRight());
		measuredHeight += (getPaddingTop() + getPaddingBottom());

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		final Scroller scroller = mScroller;

		if (!scroller.computeScrollOffset()) {
			prepareScroll();
			return;
		}

		final float halfHeight = mHeight / 2;
		final float dotRadius = mDotRadius;
		final Paint paint = mPaint;
		final int linearRegionStartX = mLinearRegionStartX;
		final int linearRegionEndX = mLinearRegionEndX;
		final Interpolator accelerateInterpolator = mAccelerateInterpolator;
		final Interpolator decelerateInterpolator = mDecelerateInterpolator;
		final int endX = computeEndX();
		final int startXABS = Math.abs(computeStartX());
		final int dotCount = mDotCount;
		final float dotSpacing = mDotSpacing;

		for (int i = 0; i < dotCount; i++) {
			float dotXDelta = scroller.getCurrX() - i * dotSpacing;
			if (dotXDelta < linearRegionStartX) {
				dotXDelta = accelerateInterpolator
						.getInterpolation((dotXDelta + startXABS)
								/ (linearRegionStartX + startXABS))
						* (linearRegionStartX + startXABS) - startXABS;
			} else if (dotXDelta > linearRegionEndX) {
				dotXDelta = decelerateInterpolator
						.getInterpolation((dotXDelta - linearRegionEndX)
								/ (endX - linearRegionEndX))
						* (endX - linearRegionEndX) + linearRegionEndX;
			}
			canvas.drawCircle(dotXDelta, halfHeight, dotRadius, paint);
		}
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidht = w;
		mHeight = h;
		mLinearRegionStartX = (int) (LIEAR_REGION_START_PERCENT * w);
		mLinearRegionEndX = (int) (LIEAR_REGION_END_PERCENT * w);
		prepareScroll();
	}

	/**
	 * Set color of dot
	 * 
	 * @param color
	 */
	public void setDotColor(int color) {
		this.mDotColor = color;
		this.mPaint.setColor(color);
	}

	/**
	 * Get the color of dot
	 * 
	 * @return
	 */
	public int getDotColor() {
		return this.mDotColor;
	}

	/**
	 * Get the progress dot radius
	 * 
	 * @return size in pixel
	 */
	public float getDotRadius() {
		return mDotRadius;
	}

	/**
	 * Set the progress dot radius
	 * 
	 * @param dotRadius
	 *            (size in scaled pixel)
	 */
	public void setDotRadius(float dotRadius) {
		setDotRadius(dotRadius, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set the progress dot radius
	 * 
	 * @param dotRadius
	 * @param unit
	 */
	public void setDotRadius(float dotRadius, int unit) {
		final Resources resources = getContext().getResources();
		this.mDotRadius = TypedValue.applyDimension(unit, dotRadius,
				resources.getDisplayMetrics());
	}

	/**
	 * Set the dot space
	 * 
	 * @param space
	 *            (size in scaled pixel)
	 */
	public void setDotSpacing(float space) {
		setDotSpacing(space, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set the dot space
	 * 
	 * @param space
	 * @param unit
	 */
	public void setDotSpacing(float space, int unit) {
		final Resources resources = getContext().getResources();
		this.mDotSpacing = TypedValue.applyDimension(unit, space,
				resources.getDisplayMetrics());
	}

	/**
	 * Get the duration of animation
	 * 
	 * @return
	 */
	public int getDuration() {
		return mDuration;
	}

	/**
	 * Set the duration of animation
	 * 
	 * @param duration
	 *            duration in milliseconds
	 */
	public void setDuration(int duration) {
		this.mDuration = duration;
	}

	/**
	 * Get the dot space
	 * 
	 * @return (size in pixel)
	 */
	public float getDotSpacing() {
		return this.mDotSpacing;
	}

	/**
	 * Set the dot count
	 * 
	 * @param count
	 */
	public void setDotCount(int count) {
		this.mDotCount = count;
	}

	/**
	 * Get the dot count
	 * 
	 * @return
	 */
	public int getDotCount() {
		return this.mDotCount;
	}

	private void prepareScroll() {
		mScroller.abortAnimation();
		mScroller.startScroll(computeStartX(), 0, computeEndX()
				- computeStartX(), 0, mDuration);
		invalidate();
	}

	protected int computeStartX() {
		return (int) (0 - mDotRadius - (mDotRadius * 2 + mDotSpacing
				* (mDotCount - 1)));
	}

	protected int computeEndX() {
		return (int) (mWidht + mDotRadius * 2 * mDotCount + mDotSpacing
				* (mDotCount - 1));
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.dotRadius = mDotRadius;
		savedState.dotSpace = mDotSpacing;
		savedState.dotCount = mDotCount;
		savedState.duration = mDuration;
		savedState.dotColor = mDotColor;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mDotRadius = savedState.dotRadius;
		mDotSpacing = savedState.dotSpace;
		mDotCount = savedState.dotCount;
		mDuration = savedState.duration;
		mDotColor = savedState.dotColor;
	}

	/**
	 * Base class for save the state of this view.
	 * 
	 * @author Tank
	 * 
	 */
	static class SavedState extends BaseSavedState {
		public float dotRadius;
		public float dotSpace;
		public int dotCount;
		public int duration;
		public int dotColor;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			dotRadius = in.readFloat();
			dotSpace = in.readFloat();
			dotCount = in.readInt();
			duration = in.readInt();
			dotColor = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeFloat(dotRadius);
			out.writeFloat(dotSpace);
			out.writeFloat(dotCount);
			out.writeInt(duration);
			out.writeInt(dotColor);
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
