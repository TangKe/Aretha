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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
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
	private int mCurrentRadius;
	private int mRadius = 250;
	private int mQuadrant = 1;
	private int mAnimationOffset = 30;
	private int mDuration = 600;
	// private boolean mOrder = false;

	private Scroller mScroller;
	private Interpolator mInterpolator;
	private SectorToggleRunnalbe mSectorToggleRunnalbe;

	private OnSectorClickListener mOnSectorClickListener;

	public SectorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public SectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public SectorView(Context context) {
		super(context);
		initialize();
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
		final int radius = mRadius;
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		final int quadrant = mQuadrant;

		int maxChildWidth = 0;
		int maxChildHeight = 0;

		for (int index = 0; index < childCount; index++) {
			View childView = getChildAt(index);
			LayoutParams layoutParams = childView.getLayoutParams();

			int childWidth = layoutParams.width > 0 ? layoutParams.width
					: width;
			int childHeight = layoutParams.height > 0 ? layoutParams.height
					: height;

			int childWidthMode = layoutParams.width > 0 ? MeasureSpec.EXACTLY
					: MeasureSpec.AT_MOST;
			int childHeightMode = layoutParams.height > 0 ? MeasureSpec.EXACTLY
					: MeasureSpec.AT_MOST;

			childView.measure(
					MeasureSpec.makeMeasureSpec(childWidth, childWidthMode),
					MeasureSpec.makeMeasureSpec(childHeight, childHeightMode));

			int childMeasuredWidth = childView.getMeasuredWidth();
			int childMeasuredHeight = childView.getMeasuredHeight();

			maxChildWidth = Math.max(maxChildWidth, childMeasuredWidth);
			maxChildHeight = Math.max(maxChildHeight, childMeasuredHeight);
		}

		int measuredWidth = width;
		int measuredHeight = height;
		final boolean isFifthQuadrant = quadrant == 5;

		if (widthMode == MeasureSpec.AT_MOST) {
			measuredWidth = Math.min((isFifthQuadrant ? 2 * radius : radius)
					+ (isFifthQuadrant ? maxChildWidth : maxChildWidth / 2),
					measuredWidth);
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			measuredHeight = Math.min((isFifthQuadrant ? 2 * radius : radius)
					+ (isFifthQuadrant ? maxChildHeight : maxChildHeight / 2),
					measuredHeight);
		}
		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = mChildCount;
		final int currentRadius = mCurrentRadius;
		final int radius = mRadius;
		final int minBorderSize = Math.min(r - l, b - t);
		final int quadrant = mQuadrant;
		final int animationOffset = mAnimationOffset;
		final Interpolator interpolator = mInterpolator;
		// final boolean order = mOrder;

		for (int index = 0; index < childCount; index++) {
			View childView = getChildAt(index);

			int halfChildWidth = Math.round(childView.getMeasuredWidth() / 2);
			int halfChildHeight = Math.round(childView.getMeasuredHeight() / 2);

			int childRadius = Math.max(
					0,
					Math.min(currentRadius - animationOffset * index,
							Math.min(radius, minBorderSize)));

			float interpolation = interpolator
					.getInterpolation((childRadius * 1.0f) / radius);
			childRadius = (int) (interpolation * radius);

			int[] childCoordinate = getChildCenterCoordinate(childRadius,
					index, l, t, r, b, childCount, quadrant);
			childView.layout(childCoordinate[0] - halfChildWidth,
					childCoordinate[1] - halfChildHeight, childCoordinate[0]
							+ halfChildWidth, childCoordinate[1]
							+ halfChildHeight);
		}
	}

	/**
	 * <pre>
	 *  2 | 1
	 * ---5---
	 *  3 | 4
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
	 *            <li>1: left-bottom</li>
	 *            <li>2: right-bottom</li>
	 *            <li>3: right-top</li>
	 *            <li>4: right-bottom</li>
	 *            </ul>
	 * @return
	 */
	protected int[] getChildCenterCoordinate(int radius, int index, int left,
			int top, int right, int bottom, int childCount, int quadrant) {
		double degreePerChild;
		double degree;

		if (quadrant == 5) {
			degreePerChild = Math.PI * 2 / childCount;
		} else {
			degreePerChild = Math.PI / 2 / (childCount + 1);
		}
		degree = degreePerChild * (index + 1);

		int xRange = (int) Math.round(radius * Math.cos(degree));
		int yRange = (int) Math.round(radius * Math.sin(degree));
		int x, y;
		switch (quadrant) {
		case 1:
			x = left + xRange;
			y = bottom - yRange;
			break;
		case 2:
			x = right - xRange;
			y = bottom - yRange;
			break;
		case 3:
			x = right - yRange;
			y = top + xRange;
			break;
		default:
		case 4:
			x = left + yRange;
			y = top + xRange;
			break;
		case 5:
			x = (right - left) / 2 + xRange;
			y = (bottom - top) / 2 + yRange;
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
	 * @return
	 */
	public int getRadius() {
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

	public void setRadius(int radius) {
		boolean needExpand = radius > this.mRadius;
		this.mRadius = radius;

		if (needExpand) {
			expand();
		} else {
			shrink();
		}
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

	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (isExpanded()) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (isExpanded()) {
				shrink();
			}
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
			final int currentRadius = mCurrentRadius;

			if (mIsAnimating) {
				removeCallbacks(this);
				scroller.abortAnimation();
			}
			if (toggle) {
				scroller.startScroll(currentRadius, 0, mRadius + mChildCount
						* mAnimationOffset - currentRadius, 0, mDuration);
			} else {
				scroller.startScroll(currentRadius, 0, 0 - mChildCount
						* mAnimationOffset - currentRadius, 0, mDuration);
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
		public int radius;
		public int currentRadius;
		public int isExpand;
		public int animationOffset;
		public int duration;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			quadrant = in.readInt();
			in.setDataPosition(1);
			radius = in.readInt();
			in.setDataPosition(2);
			currentRadius = in.readInt();
			in.setDataPosition(3);
			isExpand = in.readInt();
			in.setDataPosition(4);
			animationOffset = in.readInt();
			in.setDataPosition(5);
			duration = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(quadrant);
			out.writeInt(radius);
			out.writeInt(currentRadius);
			out.writeInt(isExpand);
			out.writeInt(animationOffset);
			out.writeInt(duration);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
