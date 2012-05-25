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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Scroller;

/**
 * A {@link ViewGroup} that position the children in a point and can be
 * expanded.
 * 
 * @author Tank
 * 
 */
public class SectorView extends ViewGroup implements OnClickListener {
	private int mChildCount;
	private boolean mIsExpand;
	private float mCurrentRadius;
	private float mRadius;
	private int mQuadrant;
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
		mQuadrant = a.getInt(R.styleable.SectorView_quadrant, 1);
		mAnimationOffset = a.getInt(R.styleable.SectorView_animationOffset, 30);
		mDuration = a.getInt(R.styleable.SectorView_duration, 600);

		a.recycle();

		initialize();
	}

	public SectorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectorView(Context context) {
		this(context, null);
	}

	private void initialize() {
		Context context = getContext();
		mInterpolator = new OvershootInterpolator();

		mSectorToggleRunnalbe = new SectorToggleRunnalbe();
		mScroller = new Scroller(context, new LinearInterpolator());
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
		final int quadrant = mQuadrant;

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

		final boolean isCenter = quadrant == 0;
		final boolean isLeftOrRightQuadrant = (quadrant == 6 || quadrant == 8);
		final boolean isTopOrBottomQuadrant = (quadrant == 5 || quadrant == 7);

		int measuredWidth = Math
				.round((isCenter || isTopOrBottomQuadrant ? 2 * radius : radius)
						+ (isCenter || isLeftOrRightQuadrant ? maxChildWidth
								: maxChildWidth / 2));

		int measuredHeight = Math
				.round((isCenter || isLeftOrRightQuadrant ? 2 * radius : radius)
						+ (isCenter || isLeftOrRightQuadrant ? maxChildHeight
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
		final int quadrant = mQuadrant;
		final int animationOffset = mAnimationOffset;
		final Interpolator interpolator = mInterpolator;
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
					index, r - l, b - t, childCount, quadrant);
			childView.layout(childCoordinate[0] - halfChildWidth,
					childCoordinate[1] - halfChildHeight, childCoordinate[0]
							+ halfChildWidth, childCoordinate[1]
							+ halfChildHeight);
		}
	}

	/**
	 * <pre>
	 * 
	 *  2  5  1
	 *     |
	 *  6--0--8
	 *     |
	 *  3  7  4
	 * </pre>
	 * 
	 * The default quadrant is 4th quadrant, the origin point in the left-top
	 * corner of this view
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
	 * @param quadrant
	 *            determine the origin of coordinates
	 *            <ul>
	 *            <li>0: center</li>
	 *            <li>1: left-bottom</li>
	 *            <li>2: right-bottom</li>
	 *            <li>3: right-top</li>
	 *            <li>4: right-bottom</li>
	 *            <li>5: top</li>
	 *            <li>6: left</li>
	 *            <li>7: bottom</li>
	 *            <li>8: right</li>
	 *            </ul>
	 * @return
	 */
	protected int[] getChildCenterCoordinate(int radius, int index, int width,
			int height, int childCount, int quadrant) {
		double degreePerChild;
		double degree;

		if (quadrant == 0) {
			degreePerChild = Math.PI * 2 / childCount;
		} else if (quadrant == 5 || quadrant == 6 || quadrant == 7
				|| quadrant == 8) {
			degreePerChild = Math.PI / (childCount + 1);
		} else {
			degreePerChild = Math.PI / 2 / (childCount + 1);
		}
		degree = degreePerChild * (index + 1);

		int xRange = (int) Math.round(radius * Math.cos(degree));
		int yRange = (int) Math.round(radius * Math.sin(degree));
		int x, y;
		switch (quadrant) {
		default:
		case 0:
			x = width / 2 + xRange;
			y = height / 2 + yRange;
			break;
		case 1:
			x = xRange;
			y = height - yRange;
			break;
		case 2:
			x = width - xRange;
			y = height - yRange;
			break;
		case 3:
			x = width - xRange;
			y = yRange;
			break;
		case 4:
			x = xRange;
			y = yRange;
			break;
		case 5:
			x = width / 2 + xRange;
			y = yRange;
			break;
		case 6:
			x = yRange;
			y = height / 2 + xRange;
			break;
		case 7:
			x = width / 2 + xRange;
			y = height - yRange;
			break;
		case 8:
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

	public int getQuadrant() {
		return mQuadrant;
	}

	public void setQuadrant(int quadrant) {
		this.mQuadrant = quadrant;
		requestLayout();
	}

	public int getAnimationOffset() {
		return mAnimationOffset;
	}

	public void setAnimationOffset(int animationOffset) {
		this.mAnimationOffset = animationOffset;
	}

	// public boolean getOrder() {
	// return mOrder;
	// }
	//
	// /**
	// *
	// * @param order
	// * true Clockwise, false Counterclockwise
	// */
	// public void setOrder(boolean order) {
	// this.mOrder = order;
	// }

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
		savedState.quadrant = mQuadrant;
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
		mQuadrant = savedState.quadrant;
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
		public int quadrant;
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
			quadrant = in.readInt();
			radius = in.readInt();
			currentRadius = in.readInt();
			isExpand = in.readInt();
			animationOffset = in.readInt();
			duration = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(quadrant);
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
