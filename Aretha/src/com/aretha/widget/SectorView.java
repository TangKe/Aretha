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

import android.R.integer;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

/**
 * {@link SectorView} is a {@link ViewGroup} that position the children in a
 * point and be expanded by code.
 * 
 * @author Tank
 * 
 */
public class SectorView extends ViewGroup implements OnClickListener {
	private int mChildCount;
	private boolean mIsExpand;
	private float mCurrentRadius;
	private float mRadius;
	private int mGravity;
	private int mAnimationOffset;
	private int mDuration;
	// private boolean mOrder = false;

	private Scroller mScroller;
	private Interpolator mInterpolator;
	private SectorToggleRunnalbe mSectorToggleRunnalbe;

	private OnSectorClickListener mOnSectorClickListener;

	public SectorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SectorView);

		mRadius = a.getDimension(R.styleable.SectorView_radius, 300);
		mGravity = a.getInt(R.styleable.SectorView_gravity, Gravity.LEFT
				| Gravity.BOTTOM);
		mAnimationOffset = a.getInt(R.styleable.SectorView_animationOffset, 30);
		mDuration = a.getInt(R.styleable.SectorView_duration, 600);
		int interpolator = a.getResourceId(R.styleable.SectorView_interpolator,
				-1);
		a.recycle();

		if (interpolator != -1) {
			mInterpolator = AnimationUtils.loadInterpolator(context,
					interpolator);
		} else {
			mInterpolator = new OvershootInterpolator();
		}

		mSectorToggleRunnalbe = new SectorToggleRunnalbe();
		mScroller = new Scroller(context, new LinearInterpolator());
	}

	public SectorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectorView(Context context) {
		this(context, null);
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		super.addView(child, index, params);
		child.setOnClickListener(this);
		mChildCount = getChildCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int childCount = mChildCount;
		final float radius = mRadius;
		final int gravity = mGravity;

		int maxChildWidth = 0;
		int maxChildHeight = 0;

		measureChildren(widthMeasureSpec, heightMeasureSpec);

		for (int index = 0; index < childCount; index++) {
			View childView = getChildAt(index);

			maxChildWidth = Math.max(maxChildWidth,
					childView.getMeasuredWidth());
			maxChildHeight = Math.max(maxChildHeight,
					childView.getMeasuredHeight());
		}

		final boolean isCenter = gravity == Gravity.CENTER;
		final boolean isLeftOrRight = (gravity == Gravity.LEFT || gravity == Gravity.RIGHT);
		final boolean isTopOrBottom = (gravity == Gravity.TOP || gravity == Gravity.BOTTOM);

		int measuredWidth = Math.round((isCenter || isTopOrBottom ? 2 * radius
				: radius)
				+ (isCenter || isLeftOrRight ? maxChildWidth
						: maxChildWidth / 2));

		int measuredHeight = Math.round((isCenter || isLeftOrRight ? 2 * radius
				: radius)
				+ (isCenter || isLeftOrRight ? maxChildHeight
						: maxChildHeight / 2));

		measuredWidth += (getPaddingLeft() + getPaddingRight());
		measuredHeight += (getPaddingTop() + getPaddingBottom());

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = mChildCount;
		final float currentRadius = mCurrentRadius;
		final float radius = mRadius;
		final int gravity = mGravity;
		final int animationOffset = mAnimationOffset;
		final Interpolator interpolator = mInterpolator;
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingRight = getPaddingRight();
		final int paddingBottom = getPaddingBottom();
		// final boolean order = mOrder;

		for (int index = 0; index < childCount; index++) {
			View childView = getChildAt(index);

			int halfChildWidth = Math.round(childView.getMeasuredWidth() / 2);
			int halfChildHeight = Math.round(childView.getMeasuredHeight() / 2);

			int childRadius = Math.round(Math.max(0,
					Math.min(currentRadius - animationOffset * index, radius)));
			float interpolation = interpolator
					.getInterpolation((childRadius * 1.0f) / radius);
			childRadius = (int) (interpolation * radius);

			int[] childCoordinate = getChildCenterCoordinate(childRadius,
					index, r - l, b - t, childCount, gravity, paddingLeft,
					paddingTop, paddingRight, paddingBottom);
			childView.layout(childCoordinate[0] - halfChildWidth + paddingLeft,
					childCoordinate[1] - halfChildHeight, childCoordinate[0]
							+ halfChildWidth + paddingLeft, childCoordinate[1]
							+ halfChildHeight);
		}
	}

	/**
	 * 
	 * The default gravity is center, the origin point in the left-top corner of
	 * this view
	 * 
	 * @param radius
	 *            the radius of all sector
	 * @param index
	 *            index of child in this view group
	 * @param left
	 *            left bounds of this view group
	 * @param top
	 *            top bounds of this view group
	 * @param right
	 *            right bounds of this view group
	 * @param bottom
	 *            bottom bounds of this view group
	 * @param gravity
	 *            determine the gravity of this view
	 *            <ul>
	 *            <li>center</li>
	 *            <li>left|bottom</li>
	 *            <li>right|bottom</li>
	 *            <li>right|top</li>
	 *            <li>left|top</li>
	 *            <li>top</li>
	 *            <li>left</li>
	 *            <li>bottom</li>
	 *            <li>right</li>
	 *            </ul>
	 * @return
	 */
	protected int[] getChildCenterCoordinate(int radius, int index, int width,
			int height, int childCount, int gravity, int paddingLeft,
			int paddingTop, int paddingRight, int paddingBottom) {
		double degreePerChild;
		double degree;

		if (gravity == Gravity.CENTER) {
			degreePerChild = Math.PI * 2 / childCount;
		} else if (gravity == Gravity.TOP || gravity == Gravity.LEFT
				|| gravity == Gravity.BOTTOM || gravity == Gravity.RIGHT) {
			degreePerChild = Math.PI / (childCount + 1);
		} else {
			degreePerChild = Math.PI / 2 / (childCount + 1);
		}
		degree = degreePerChild * (index + 1);

		int xRange = (int) Math.round(radius * Math.cos(degree));
		int yRange = (int) Math.round(radius * Math.sin(degree));
		int x, y;
		switch (gravity) {
		default:
		case Gravity.CENTER:
			x = width / 2 + xRange;
			y = height / 2 + yRange;
			break;
		case Gravity.LEFT | Gravity.BOTTOM:
			x = xRange + paddingLeft;
			y = height - yRange - paddingBottom;
			break;
		case Gravity.RIGHT | Gravity.BOTTOM:
			x = width - xRange - paddingRight;
			y = height - yRange - paddingBottom;
			break;
		case Gravity.RIGHT | Gravity.TOP:
			x = width - xRange - paddingRight;
			y = yRange + paddingTop;
			break;
		case Gravity.LEFT | Gravity.TOP:
			x = xRange + paddingLeft;
			y = yRange + paddingTop;
			break;
		case Gravity.TOP:
			x = width / 2 + xRange;
			y = yRange;
			break;
		case Gravity.LEFT:
			x = yRange;
			y = height / 2 + xRange;
			break;
		case Gravity.BOTTOM:
			x = width / 2 + xRange;
			y = height - yRange;
			break;
		case Gravity.RIGHT:
			x = width - yRange;
			y = height / 2 + xRange;
			break;
		}

		return new int[] { x, y };
	}

	/**
	 * Expand the children
	 */
	public void expand() {
		toggle(true);
	}

	/**
	 * Shrink the children
	 */
	public void shrink() {
		toggle(false);
	}

	/**
	 * Expand or shrink the children
	 * 
	 * @param toggle
	 */
	public void toggle(boolean toggle) {
		mSectorToggleRunnalbe.toggle(toggle);
	}

	/**
	 * Get the {@link Interpolator} of the expand and shrink animation
	 * 
	 * @return
	 */
	public Interpolator getInterpolator() {
		return mInterpolator;
	}

	/**
	 * Set the {@link Interpolator} of the expand and shrink animation
	 * 
	 * @param interpolator
	 */
	public void setInterpolator(Interpolator interpolator) {
		this.mInterpolator = interpolator;
	}

	/**
	 * Return the radius of this view group
	 * 
	 * @return the size (in pixels).
	 */
	public float getRadius() {
		return mRadius;
	}

	/**
	 * Set the radius of this view group
	 * 
	 * @return
	 */
	public OnSectorClickListener getOnSectorClickListener() {
		return mOnSectorClickListener;
	}

	public void setOnSectorClickListener(
			OnSectorClickListener onSectorClickListener) {
		this.mOnSectorClickListener = onSectorClickListener;
	}

	/**
	 * Set radius (in scaled pixel)
	 * 
	 * @param radius
	 */
	public void setRadius(float radius) {
		setRadius(radius, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set radius
	 * 
	 * @param radius
	 * @param unit
	 *            the unit to use, see {@link TypedValue}
	 */
	public void setRadius(float radius, int unit) {
		final Resources resources = getContext().getResources();
		this.mRadius = Math.max(
				0,
				TypedValue.applyDimension(unit, radius,
						resources.getDisplayMetrics()));
		requestLayout();
	}

	public int getGravity() {
		return mGravity;
	}

	public void setGravity(int gravity) {
		this.mGravity = gravity;
		requestLayout();
	}

	public int getAnimationOffset() {
		return mAnimationOffset;
	}

	public void setAnimationOffset(int animationOffset) {
		this.mAnimationOffset = animationOffset;
	}

	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
		this.mDuration = duration;
	}

	public boolean isExpanded() {
		return mIsExpand;
	}

	@Override
	public void onClick(View v) {
		if (mOnSectorClickListener != null
				&& mOnSectorClickListener.onSectorClick(v)) {
			shrink();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (isExpanded()) {
				requestDisallowInterceptTouchEvent(true);
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isExpanded()) {
				shrink();
			}
			requestDisallowInterceptTouchEvent(false);
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.currentRadius = mCurrentRadius;
		savedState.isExpand = mIsExpand ? 1 : 0;
		savedState.gravity = mGravity;
		savedState.radius = mRadius;
		savedState.animationOffset = mAnimationOffset;
		savedState.duration = mDuration;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mCurrentRadius = savedState.currentRadius;
		mIsExpand = savedState.isExpand == 0 ? false : true;
		mGravity = savedState.gravity;
		mRadius = savedState.radius;
		mAnimationOffset = savedState.animationOffset;
		mDuration = savedState.duration;
	}

	/**
	 * Inner class for animate the every sector.
	 * 
	 * @author Tank
	 * 
	 */
	private class SectorToggleRunnalbe implements Runnable {
		private boolean mIsAnimating;

		@Override
		public void run() {
			Scroller scroller = mScroller;
			if (scroller.computeScrollOffset()) {
				mCurrentRadius = scroller.getCurrX();
				requestLayout();
				post(this);
			} else {
				mIsAnimating = false;
			}
		}

		/**
		 * Animate to expand or shrink all the sector.
		 * 
		 * @param toggle
		 */
		public void toggle(boolean toggle) {
			mIsExpand = toggle;
			Scroller scroller = mScroller;
			final float currentRadius = mCurrentRadius;

			if (mIsAnimating) {
				removeCallbacks(this);
				scroller.abortAnimation();
			}
			if (toggle) {
				scroller.startScroll(
						Math.round(currentRadius),
						0,
						Math.round(mRadius + mChildCount * mAnimationOffset
								- currentRadius), 0, mDuration);
			} else {
				scroller.startScroll(
						Math.round(currentRadius),
						0,
						Math.round(0 - mChildCount * mAnimationOffset
								- currentRadius), 0, mDuration);
			}
			mIsAnimating = true;
			post(this);
		}
	}

	/**
	 * Listener for sector click event
	 * 
	 * @author Tank
	 * 
	 */
	public static interface OnSectorClickListener {
		/**
		 * invoked when sector been clicked
		 * 
		 * @param sector
		 * @return return true to close all the sector, otherwise stay expand.
		 */
		public boolean onSectorClick(View sector);
	}

	/**
	 * Base class for save the state of this view.
	 * 
	 * @author Tank
	 * 
	 */
	static class SavedState extends BaseSavedState {
		public int gravity;
		public float radius;
		public float currentRadius;
		public int isExpand;
		public int animationOffset;
		public int duration;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			gravity = in.readInt();
			radius = in.readInt();
			currentRadius = in.readInt();
			isExpand = in.readInt();
			animationOffset = in.readInt();
			duration = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(gravity);
			out.writeFloat(radius);
			out.writeFloat(currentRadius);
			out.writeInt(isExpand);
			out.writeInt(animationOffset);
			out.writeInt(duration);
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
