/* Copyright (c) 2011-2012 Tank Tang
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

import com.aretha.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class WaveLayout extends ViewGroup {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int mGravity;
	private int mOrientation;

	private int mCurrentWaveCrestPosition;
	private int mCurrentWaveAmplitude;
	private int mMaxWaveAmplitude;

	private int mTouchSlop;
	private float mPressedX;
	private float mPressedY;

	private Scroller mScroller;

	private OnWaveLayoutChangeListener mOnWaveLayoutChangeListener;
	private Rect mPointCheckRect;
	private int mLastPointIndex;;

	public WaveLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.WaveLayout);
		mMaxWaveAmplitude = (int) a.getDimension(
				R.styleable.WaveLayout_maxAmplitude, TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources()
								.getDisplayMetrics()));
		mOrientation = a.getInt(R.styleable.WaveLayout_orientation, HORIZONTAL);
		mGravity = a.getInt(R.styleable.WaveLayout_gravity, Gravity.BOTTOM);
		a.recycle();
		initialize(context);
	}

	public WaveLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveLayout(Context context) {
		this(context, null);
	}

	private void initialize(Context context) {
		mScroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mPointCheckRect = new Rect();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		final int currentWaveCrestPosition = mCurrentWaveCrestPosition;
		final boolean isHorizontal = mOrientation == HORIZONTAL;
		final int currentWaveRadius = mCurrentWaveAmplitude;
		final int gravity = mGravity;

		int height = b - t;
		int width = r - l;
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();
		int paddingTop = getPaddingTop();
		int paddingLeft = getPaddingBottom();
		int offset = 0;
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			int position = computeChildPosition(childWidth, childHeight,
					offset, currentWaveCrestPosition, currentWaveRadius,
					isHorizontal);

			if (isHorizontal) {
				switch (gravity) {
				case Gravity.TOP:
					child.layout(offset + paddingLeft, position + paddingTop,
							offset + childWidth + paddingLeft, childHeight
									+ position + paddingTop);
					break;
				case Gravity.BOTTOM:
					child.layout(offset + paddingLeft, height - childHeight
							- position - paddingBottom, offset + childWidth
							+ paddingLeft, height - position - paddingBottom);
					break;
				}

			} else {
				switch (gravity) {
				case Gravity.LEFT:
					child.layout(position + paddingLeft, offset + paddingTop,
							position + childWidth + paddingLeft, offset
									+ childHeight + paddingTop);
					break;
				case Gravity.RIGHT:
					child.layout(width - position - childWidth - paddingRight,
							offset + paddingTop, width - position
									- paddingRight, offset + childHeight
									+ paddingTop);
					break;
				}

			}

			offset += isHorizontal ? childWidth : childHeight;
		}
	}

	protected int computeChildPosition(int width, int height, int offset,
			int currentWaveCrestPosition, int waveRadius, boolean isHorizontal) {
		int var = (isHorizontal ? offset + width / 2 : offset + height / 2)
				- currentWaveCrestPosition;
		return (int) (Math.max(0, -Math.pow(var, 2) / waveRadius / 2
				+ waveRadius));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		final int childCount = getChildCount();

		int maxChildWidth = 0, maxChildHeight = 0;
		int totalChildrenWidth = 0, totalChildHeight = 0;

		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int measuredChildWidth = child.getMeasuredWidth();
			int measuredChildHeight = child.getMeasuredHeight();

			totalChildrenWidth += measuredChildWidth;
			totalChildHeight += measuredChildHeight;

			maxChildWidth = Math.max(maxChildWidth, measuredChildWidth);
			maxChildHeight = Math.max(maxChildHeight, measuredChildHeight);
		}

		int measuredWidth = 0;
		int measuredHeight = 0;

		switch (mOrientation) {
		case HORIZONTAL:
			measuredWidth = totalChildrenWidth;
			measuredHeight = maxChildHeight + mCurrentWaveAmplitude;
			break;
		case VERTICAL:
			measuredWidth = maxChildWidth + mCurrentWaveAmplitude;
			measuredHeight = totalChildHeight;
			break;
		}

		measuredWidth += getPaddingLeft() + getPaddingRight();
		measuredHeight += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mCurrentWaveAmplitude = mScroller.getCurrX();
			invalidate();
			requestLayout();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPressedX = ev.getX();
			mPressedY = ev.getY();
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent(event);
		final float x = event.getX();
		final float y = event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mScroller.startScroll(mCurrentWaveAmplitude, 0, mMaxWaveAmplitude
					- mCurrentWaveAmplitude, 0);
		case MotionEvent.ACTION_MOVE:
			int orientation = mOrientation;
			int touchSlop = mTouchSlop;
			if ((orientation == HORIZONTAL && Math.abs(x - mPressedX) > touchSlop)
					|| (orientation == VERTICAL && Math.abs(y - mPressedY) > touchSlop)) {
				requestDisallowInterceptTouchEvent(true);
			}

			mCurrentWaveCrestPosition = (int) (mOrientation == HORIZONTAL ? x
					: y);
			if (null != mOnWaveLayoutChangeListener) {
				final int count = getChildCount();
				final Rect pointCheckRect = mPointCheckRect;
				for (int index = 0; index < count; index++) {
					View child = getChildAt(index);
					child.getHitRect(pointCheckRect);
					if (pointCheckRect.contains(pointCheckRect.centerX(),
							(int) y)) {
						if (mLastPointIndex != index) {
							mOnWaveLayoutChangeListener.onIndexChange(child,
									index);
						}
						mLastPointIndex = index;
						break;
					}
				}
			}
			result = true;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			requestDisallowInterceptTouchEvent(false);
			mScroller.startScroll(mCurrentWaveAmplitude, 0,
					0 - mCurrentWaveAmplitude, 0);
			break;
		}
		invalidate();
		requestLayout();
		return result;
	}

	public int getGravity() {
		return mGravity;
	}

	public void setGravity(int gravity) {
		this.mGravity = gravity;
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setOrientation(int orientation) {
		this.mOrientation = orientation;
	}

	public int getMaxWaveAmplitude() {
		return mMaxWaveAmplitude;
	}

	public void setMaxWaveAmplitude(int maxWaveAmplitude) {
		this.mMaxWaveAmplitude = maxWaveAmplitude;
	}

	public OnWaveLayoutChangeListener getOnWaveLayoutChangeListener() {
		return mOnWaveLayoutChangeListener;
	}

	public void setOnWaveLayoutChangeListener(
			OnWaveLayoutChangeListener onWaveLayoutChangeListener) {
		this.mOnWaveLayoutChangeListener = onWaveLayoutChangeListener;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.gravity = mGravity;
		savedState.orientation = mOrientation;
		savedState.maxWaveAmplitude = mMaxWaveAmplitude;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mGravity = savedState.gravity;
		mOrientation = savedState.orientation;
		mMaxWaveAmplitude = savedState.maxWaveAmplitude;
	}

	public interface OnWaveLayoutChangeListener {
		public void onIndexChange(View child, int index);
	}

	static class SavedState extends BaseSavedState {
		public int gravity;
		public int orientation;
		public int maxWaveAmplitude;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			gravity = in.readInt();
			orientation = in.readInt();
			maxWaveAmplitude = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(gravity);
			out.writeInt(orientation);
			out.writeInt(maxWaveAmplitude);
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
