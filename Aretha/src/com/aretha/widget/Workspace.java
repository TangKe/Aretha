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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.aretha.R;
import com.aretha.util.Utils;

public class Workspace extends ViewGroup {
	protected final static int TOUCH_STATE_IDLE = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected final static int TOUCH_STATE_FLING = 3;

	private Context mContext;

	private int mTouchState;

	private Scroller mScroller;

	private int mDuration;
	private boolean mIsBounceEnable;

	private float mLastMotionX;
	private int mMaximumVelocity;
	protected int mSnapVelocity;

	protected int mCurrentChildIndex;

	private VelocityTracker mVelocityTracker;
	private OnWorkspaceChangeListener mOnWorkspaceChangeListener;
	private int mContentWidth;

	private int mWidth;

	private float mTouchDownX;

	private int mTouchSlop;

	private boolean mTouchedInIngnoreChild;
	private boolean mIsShowInitializeItem;

	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Workspace);
		mDuration = a.getInteger(R.styleable.Workspace_duration, 300);
		mCurrentChildIndex = a.getInteger(R.styleable.Workspace_showPage, 0);
		mSnapVelocity = a.getInteger(R.styleable.Workspace_snapVelocity, 500);
		mIsBounceEnable = a.getBoolean(R.styleable.Workspace_bounce, true);
		int interpolator = a.getResourceId(R.styleable.Workspace_interpolator,
				-1);

		if (interpolator != -1) {
			mScroller = new Scroller(getContext(),
					AnimationUtils.loadInterpolator(context, interpolator));
		} else {
			mScroller = new Scroller(getContext());
		}
		a.recycle();

		mMaximumVelocity = ViewConfiguration.get(getContext())
				.getScaledMaximumFlingVelocity();
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Workspace(Context context) {
		this(context, null);
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		super.addView(child, index, params);
		if (mOnWorkspaceChangeListener != null) {
			mOnWorkspaceChangeListener.onPageCountChange();
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childCount = getChildCount();
		int layoutedChildWidth = 0;

		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();

		final int height = b - t;

		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int childMeasureWidth = child.getMeasuredWidth();
			int childMeasureHeight = child.getMeasuredHeight();
			child.layout(layoutedChildWidth + paddingLeft, paddingTop
					+ (height - paddingTop - paddingBottom) / 2
					- childMeasureHeight / 2, layoutedChildWidth + paddingLeft
					+ childMeasureWidth, paddingTop
					+ (height - paddingTop - paddingBottom) / 2
					+ childMeasureHeight / 2);
			layoutedChildWidth += childMeasureWidth;
		}
		if (!mIsShowInitializeItem) {
			scrollToPage(mCurrentChildIndex, false);
			mIsShowInitializeItem = true;
		}
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			if (mTouchState != TOUCH_STATE_FLING) {
				mTouchState = TOUCH_STATE_FLING;
			}

			scrollTo(mScroller.getCurrX(), 0);
			invalidate();
		} else {
			if (mTouchState == TOUCH_STATE_FLING) {
				mTouchState = TOUCH_STATE_IDLE;
			}

			if (mOnWorkspaceChangeListener != null
					&& mTouchState == TOUCH_STATE_IDLE) {
				mOnWorkspaceChangeListener.onPageChange(mCurrentChildIndex);
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mTouchDownX = mLastMotionX = ev.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(mTouchDownX - ev.getX()) > mTouchSlop) {
				return true;
			}
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN
				&& event.getEdgeFlags() != 0) {
			// Don't handle edge touches immediately -- they may actually belong
			// to one of our
			// descendants.
			return false;
		}

		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final boolean superResult = super.onTouchEvent(event);

		if (getChildCount() <= 0)
			return superResult;

		acquireVelocityTrackerAndAddMovement(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			/**
			 * If user touch down in the View with id R.id.workspace_ignore,
			 * this view will not response to the touch event.
			 */
			mTouchedInIngnoreChild = false;
			int childCount = getChildCount();
			Rect rect = new Rect();
			final int offsetX = getScrollX();
			for (int index = 0; index < childCount; index++) {
				View child = getChildAt(index);
				child.getHitRect(rect);

				/**
				 * Find the View with id R.id.workspace_ignore which been
				 * touched in.
				 */
				if (rect.contains(x + offsetX, y)
						&& child.getId() == R.id.workspaceIngnore) {
					mTouchedInIngnoreChild = true;
					break;
				}
			}
			return !mTouchedInIngnoreChild;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(mTouchDownX - x) > mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
				requestDisallowInterceptTouchEvent(true);
			}

			if (mTouchState != TOUCH_STATE_SCROLLING) {
				return superResult;
			}

			float xDiff = event.getX() - mLastMotionX;
			final int scrollX = getScrollX();
			// fake value, we will fix it later
			float scroll = scrollX - xDiff;
			/**
			 * Fix scroll value
			 */
			if (scroll <= 0 || scroll >= mContentWidth - mWidth) {
				if (!mIsBounceEnable) {
					scroll = scroll <= 0 ? 0 : ((scroll >= mContentWidth
							- mWidth) ? mContentWidth - mWidth : scroll);
				} else {
					scroll = scrollX - xDiff / 2;
				}
			}

			scrollTo((int) scroll, 0);
			mLastMotionX = event.getX();

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocityX = (int) velocityTracker.getXVelocity();
			if (velocityX > mSnapVelocity) {
				animationToPrevPage();
			} else if (velocityX < -mSnapVelocity) {
				animationToNextPage();
			} else {
				scrollToPage(mCurrentChildIndex, true);
			}
			velocityTracker.recycle();

			requestDisallowInterceptTouchEvent(false);
			break;
		}
		return superResult;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int childCount = getChildCount();
		int measuredWidth = 0;
		int measuredHeight = 0;
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			measuredWidth += child.getMeasuredWidth();
			measuredHeight = Math
					.max(measuredHeight, child.getMeasuredHeight());
		}
		mContentWidth = measuredWidth;
		measuredWidth += getPaddingLeft() + getPaddingRight();
		measuredHeight += getPaddingBottom() + getPaddingTop();

		measuredWidth = Math.max(getSuggestedMinimumWidth(), measuredWidth);
		measuredHeight = Math.max(getSuggestedMinimumHeight(), measuredHeight);

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	public void animationToNextPage() {
		scrollToPage(++mCurrentChildIndex, true);
	}

	public void animationToPrevPage() {
		scrollToPage(--mCurrentChildIndex, true);
	}

	public void scrollToPage(View child, boolean isAnimated) {
		scrollToPage(indexOfChild(child), isAnimated);
	}

	public void scrollToPage(int index, boolean isAnimated) {
		final int scroll = getScrollX();
		final int childCount = getChildCount();
		final int width = getWidth();

		mCurrentChildIndex = index = Math.max(0,
				Math.min(index, childCount - 1));
		View child = getChildAt(index);
		boolean isLast = index == childCount - 1;

		/**
		 * total width of children which after this index child is less than
		 * parent width
		 */
		int lastChildWidth = 0;
		boolean widthLessThanParnent = true;
		for (; index < childCount; index++) {
			View lastChild = getChildAt(index);
			lastChildWidth += lastChild.getWidth();
			if (lastChildWidth > width) {
				widthLessThanParnent = false;
				break;
			}
		}

		mScroller.startScroll(
				scroll,
				0,
				child.getLeft()
						- scroll
						- getPaddingLeft()
						- (isLast || widthLessThanParnent ? getWidth()
								- child.getWidth() - getPaddingLeft()
								- getPaddingRight() : 0), 0,
				isAnimated ? mDuration : 0);
		invalidate();
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	public void setInterpolator(Interpolator interpolator) {
		mScroller = new Scroller(mContext, interpolator);
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public int getDuration() {
		return this.mDuration;
	}

	public void setSnapVelocity(int snapVelocity) {
		mSnapVelocity = snapVelocity;
	}

	public int getSnapVelocity() {
		return this.mSnapVelocity;
	}

	public int getCurrentChildIndex() {
		return mCurrentChildIndex;
	}

	public void setBounceEnable(boolean enable) {
		this.mIsBounceEnable = enable;
	}

	public boolean getBounceEnable() {
		return this.mIsBounceEnable;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
	}

	public OnWorkspaceChangeListener getOnWorkspaceChangeListener() {
		return mOnWorkspaceChangeListener;
	}

	public void setOnWorkspaceChangeListener(
			OnWorkspaceChangeListener onWorkspaceChangeListener) {
		this.mOnWorkspaceChangeListener = onWorkspaceChangeListener;
	}

	public interface OnWorkspaceChangeListener {
		public void onPageChange(int pageIndex);

		public void onPageCountChange();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.duration = mDuration;
		savedState.currentChildIndex = mCurrentChildIndex;
		savedState.snapVelocity = mSnapVelocity;
		savedState.isBounceEnable = mIsBounceEnable;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mDuration = savedState.duration;
		mCurrentChildIndex = savedState.currentChildIndex;
		mSnapVelocity = savedState.snapVelocity;
		mIsBounceEnable = savedState.isBounceEnable;
	}

	static class SavedState extends BaseSavedState {
		public int duration;
		public int snapVelocity;
		public int currentChildIndex;
		public boolean isBounceEnable;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			duration = in.readInt();
			snapVelocity = in.readInt();
			currentChildIndex = in.readInt();
			isBounceEnable = in.readInt() == 0 ? false : true;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(duration);
			out.writeFloat(snapVelocity);
			out.writeFloat(currentChildIndex);
			out.writeInt(isBounceEnable ? 1 : 0);
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
