/* Copyright (c) 2011-2013 Tank Tang
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.aretha.R;

/**
 * Custom implement of FliperView base on AdapterView Lazy load the View inside
 * AdapterViewFliper to avoid OOM
 * 
 * @author tank
 * 
 */
public class AdapterViewFlipper extends AdapterView<Adapter> {
	private Adapter mAdapter;
	private int mCurrentPosition;
	private int mLastPosition;
	private int mItemCount;

	private Animation mInAnimation;
	private Animation mOutAnimation;

	private Recyclebin mRecyclebin;

	private AdapterDataSetObserver mAdapterDataSetObserver;

	private boolean mIsFlipping;
	private int mFlipInterval;
	private boolean mIsAutoStart;
	private int mMaxAnimationDuration;
	private Runnable mAutoStartRunnable = new Runnable() {
		@Override
		public void run() {
			showNext();
			postDelayed(this, mMaxAnimationDuration + mFlipInterval);
		}
	};

	public AdapterViewFlipper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AdapterViewFlipper, defStyle, 0);
		int inAnimationId = a.getResourceId(
				R.styleable.AdapterViewFlipper_inAnimation, 0);
		if (0 != inAnimationId) {
			setInAnimation(AnimationUtils.loadAnimation(context, inAnimationId));
		}

		int outAnimationId = a.getResourceId(
				R.styleable.AdapterViewFlipper_outAnimation, 0);
		if (0 != outAnimationId) {
			setOutAnimation(AnimationUtils.loadAnimation(context,
					outAnimationId));
		}
		mIsAutoStart = a.getBoolean(R.styleable.AdapterViewFlipper_autoStart,
				false);
		mFlipInterval = a.getInteger(
				R.styleable.AdapterViewFlipper_flipInterval, 1000);
		a.recycle();
		mRecyclebin = new Recyclebin();
		mAdapterDataSetObserver = new AdapterDataSetObserver();
	}

	public AdapterViewFlipper(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.adapterViewFlipperStyle);
	}

	public AdapterViewFlipper(Context context) {
		this(context, null);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		final int count = getChildCount();
		final int parentLeft = getPaddingLeft();
		final int parentTop = getPaddingTop();
		final int parentRight = right - left - getPaddingRight();
		final int parentBottom = bottom - top - getPaddingBottom();
		for (int index = 0; index < count; index++) {
			View child = getChildAt(index);

			if (child.getVisibility() == GONE) {
				continue;
			}
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			final int gravity = lp.gravity;
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			int childLeft = parentLeft;
			int childTop = parentTop;

			// Resolve the gravity
			if (gravity != Gravity.NO_GRAVITY) {

				final int horizontalGravity = gravity
						& Gravity.HORIZONTAL_GRAVITY_MASK;
				final int verticalGravity = gravity
						& Gravity.VERTICAL_GRAVITY_MASK;

				switch (verticalGravity) {
				case Gravity.TOP:
					childTop += lp.leftMargin;
					break;
				case Gravity.CENTER_HORIZONTAL:
					childTop = parentTop
							+ (parentBottom - parentTop + lp.topMargin
									+ lp.bottomMargin - childHeight) / 2;
					break;
				case Gravity.BOTTOM:
					childTop = parentBottom - childHeight - lp.bottomMargin;
					break;
				}

				switch (horizontalGravity) {
				case Gravity.LEFT:
					childLeft = lp.leftMargin;
					break;
				case Gravity.CENTER_VERTICAL:
					childLeft = parentLeft
							+ (parentRight - parentLeft + lp.leftMargin
									+ lp.rightMargin - childWidth) / 2;
					break;
				case Gravity.RIGHT:
					childLeft = parentRight - lp.rightMargin - childWidth;
					break;
				}
			}
			child.layout(childLeft, childTop, childLeft + childWidth, childTop
					+ childHeight);
		}

		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		LayoutParams layoutParams = new LayoutParams(getContext(), attrs);
		return layoutParams;
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter(Adapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mAdapterDataSetObserver);
		}
		mAdapter = adapter;
		mItemCount = adapter.getCount();
		mRecyclebin.reset();

		if (null != mAdapter) {
			mAdapter.registerDataSetObserver(mAdapterDataSetObserver);
		}
		removeAllViewsInLayout();
		updateViews();
	}

	public void startFlipping() {
		mIsFlipping = true;
		removeCallbacks(mAutoStartRunnable);
		postDelayed(mAutoStartRunnable, mMaxAnimationDuration + mFlipInterval);
	}

	public void stopFlipping() {
		mIsFlipping = false;
		stopFlippingInner();
	}

	private void stopFlippingInner() {
		removeCallbacks(mAutoStartRunnable);
	}

	public void setFlipInterval(int milliseconds) {
		mFlipInterval = milliseconds;
	}

	public boolean isAutoStart() {
		return mIsAutoStart;
	}

	public void setAutoStart(boolean autoStart) {
		mIsAutoStart = autoStart;
	}

	public Animation getInAnimation() {
		return mInAnimation;
	}

	public void setInAnimation(Animation inAnimation) {
		this.mInAnimation = inAnimation;
		updateFlipInterval(inAnimation);
	}

	public Animation getOutAnimation() {
		return mOutAnimation;
	}

	public void setOutAnimation(Animation outAnimation) {
		this.mOutAnimation = outAnimation;
		updateFlipInterval(outAnimation);
	}

	public boolean isFlipping() {
		return mIsFlipping;
	}

	private void updateFlipInterval(Animation animation) {
		int duration = 0;
		if (null != animation) {
			duration = (int) animation.getDuration();
		}
		mMaxAnimationDuration = Math.max(duration, mMaxAnimationDuration);
	}

	// @Override
	// protected Parcelable onSaveInstanceState() {
	// SavedState savedState = new SavedState(super.onSaveInstanceState());
	// savedState.isFlipping = mIsFlipping;
	// stopFlippingInner();
	// return savedState;
	// }
	//
	// @Override
	// protected void onRestoreInstanceState(Parcelable state) {
	// SavedState savedState = (SavedState) state;
	// super.onRestoreInstanceState(savedState.getSuperState());
	//
	// mIsFlipping = savedState.isFlipping;
	// if (mIsFlipping) {
	// startFlipping();
	// }
	// }

	private void updateViews() {
		if (0 == mItemCount) {
			return;
		}

		final int currentPosition = mCurrentPosition;
		final int lastPosition = mLastPosition;

		int currentViewType = 0, lastViewType = 0;
		if (1 < mAdapter.getViewTypeCount()) {
			currentViewType = mAdapter.getItemViewType(currentPosition);
			lastViewType = mAdapter.getItemViewType(lastPosition);
		}

		View scrapeView = mRecyclebin.getScrapView(currentViewType);
		View currentView = mAdapter.getView(currentPosition, scrapeView, this);
		if (null == currentView) {
			throw new IllegalStateException(
					"Adapter.getView must return a view");
		}

		LayoutParams layoutParams = (LayoutParams) currentView
				.getLayoutParams();
		if (null == layoutParams) {
			layoutParams = new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		addViewInLayout(currentView, getChildCount(), layoutParams);
		if (null != mInAnimation) {
			currentView.startAnimation(mInAnimation);
		}

		if (getChildCount() > 1) {
			View lastView = getChildAt(0);
			if (null != mOutAnimation) {
				lastView.startAnimation(mOutAnimation);
			}
			removeViewInLayout(lastView);
			mRecyclebin.addScrapView(lastView, lastViewType);
		}
		requestLayout();
		invalidate();
		if(mIsAutoStart){
			startFlipping();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void showNext() {
		setSelection(++mCurrentPosition);
	}

	public void showPrevious() {
		setSelection(--mCurrentPosition);
	}

	@Override
	public void setSelection(int position) {
		if (position >= mItemCount) {
			position = 0;
		} else if (position < 0) {
			position = mItemCount - 1;
		}

		mLastPosition = mCurrentPosition;
		mCurrentPosition = position;
		updateViews();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (visibility == View.INVISIBLE || visibility == View.GONE) {
			stopFlippingInner();
		} else if (isFlipping()) {
			startFlipping();
		}
	}

	public static class LayoutParams extends ViewGroup.MarginLayoutParams {
		public int gravity;

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.AdapterViewFlipper_Layout);
			this.gravity = a.getInt(
					R.styleable.AdapterViewFlipper_Layout_layout_gravity,
					Gravity.NO_GRAVITY);
			a.recycle();
		}

		public LayoutParams(int width, int height) {
			this(width, height, Gravity.NO_GRAVITY);
		}

		public LayoutParams(int width, int hegith, int gravity) {
			super(width, hegith);
			this.gravity = gravity;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

	}

	class AdapterDataSetObserver extends DataSetObserver {
		@Override
		public void onInvalidated() {
			super.onInvalidated();
		}

		@Override
		public void onChanged() {
			mItemCount = mAdapter.getCount();
			super.onChanged();
		}
	}

	/**
	 * Collect the view removed from {@link AdapterViewFlipper} for later use
	 * Can store all kind all view with specify view type
	 * 
	 * @author tank
	 * 
	 */
	class Recyclebin {
		private HashMap<Integer, Queue<View>> mScrapViews;

		public Recyclebin() {
			mScrapViews = new HashMap<Integer, Queue<View>>();
		}

		/**
		 * Clear all the saved view
		 */
		public void reset() {
			mScrapViews.clear();
		}

		/**
		 * 
		 * @param viewType
		 * @return
		 */
		public View getScrapView(int viewType) {
			Queue<View> specifyTypeViews = mScrapViews.get(viewType);
			if (null == specifyTypeViews || 0 == specifyTypeViews.size()) {
				return null;
			}
			return specifyTypeViews.poll();
		}

		/**
		 * Add scrap view with view type
		 * 
		 * @param view
		 * @param viewType
		 */
		public void addScrapView(View view, int viewType) {
			Queue<View> specifyTypeViews = mScrapViews.get(viewType);
			if (null == specifyTypeViews) {
				specifyTypeViews = new LinkedList<View>();
				mScrapViews.put(viewType, specifyTypeViews);
			}

			specifyTypeViews.offer(view);
		}
	}

	static class SavedState extends BaseSavedState {
		public boolean isFlipping;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			isFlipping = in.readInt() == 0 ? false : true;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(isFlipping ? 1 : 0);
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
