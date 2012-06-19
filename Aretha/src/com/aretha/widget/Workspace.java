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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class Workspace extends ViewGroup {
	protected final static int TOUCH_STATE_IDLE = 0;
	protected final static int TOUCH_STATE_SCROLLING = 1;
	protected final static int TOUCH_STATE_FLING = 3;

	private int mTouchState;

	private Scroller mScroller;

	private int mDuration;

	private float mLastMotionX;
	private int mMaximumVelocity;
	protected int mSnapVelocity;

	protected int mCurrentChildIndex;

	private VelocityTracker mVelocityTracker;
	private WorkspaceListener mWorkspaceListener;
	private int mContentWidth;

	private int mWidth;

	private float mTouchDownX;

	private int mTouchSlop;

	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Workspace);
		mDuration = a.getInteger(R.styleable.Workspace_duration, 300);
		mCurrentChildIndex = a.getInteger(R.styleable.Workspace_showChild, 0);
		mSnapVelocity = a.getInteger(R.styleable.Workspace_snapVelocity, 500);

		initialize();
	}

	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Workspace(Context context) {
		this(context, null);
	}

	void initialize() {
		mScroller = new Scroller(getContext());
		mMaximumVelocity = ViewConfiguration.get(getContext())
				.getScaledMaximumFlingVelocity();
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		super.addView(child, index, params);
		if (mWorkspaceListener != null) {
			mWorkspaceListener.onChildInvalidate();
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
		scrollToChild(mCurrentChildIndex, false);
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

			if (mWorkspaceListener != null && mTouchState == TOUCH_STATE_IDLE) {
				mWorkspaceListener.onChildChanged(mCurrentChildIndex);
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			mLastMotionX = ev.getX();
		}
		return super.dispatchTouchEvent(ev);
	};

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchDownX = ev.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(mTouchDownX - ev.getX()) > mTouchSlop) {
				return true;
			}
			break;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int childrenCount = getChildCount();
		if (childrenCount <= 0)
			return super.onTouchEvent(event);

		acquireVelocityTrackerAndAddMovement(event);

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			float scroll = event.getX() - mLastMotionX;

			// Slow down the speed. when scroll to the edge of content
			final int scrollX = getScrollX();
			if (scrollX <= 0 || scrollX >= mContentWidth - mWidth) {
				scroll /= 2;
			}

			scrollBy((int) -scroll, 0);
			mLastMotionX = event.getX();

			if (mTouchState != TOUCH_STATE_SCROLLING) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
			int velocityX = (int) velocityTracker.getXVelocity();
			if (velocityX > mSnapVelocity) {
				animationToPrevChild();
			} else if (velocityX < -mSnapVelocity) {
				animationToNextChild();
			} else {
				scrollToChild(mCurrentChildIndex, true);
			}
			velocityTracker.recycle();
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int childCount = getChildCount();
		int maxWidth = 0;
		int maxHeight = 0;
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			maxWidth += child.getMeasuredWidth();
			maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
		}
		mContentWidth = maxWidth;
		maxWidth += getPaddingLeft() + getPaddingRight();
		maxHeight += getPaddingBottom() + getPaddingTop();

		setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
				resolveSize(maxHeight, heightMeasureSpec));
	}

	public void animationToNextChild() {
		scrollToChild(++mCurrentChildIndex, true);
	}

	public void animationToPrevChild() {
		scrollToChild(--mCurrentChildIndex, true);
	}

	public void scrollToChild(int index, boolean isAnimated) {
		final int scroll = getScrollX();
		final int childCount = getChildCount();

		mCurrentChildIndex = index = Math.max(0,
				Math.min(index, childCount - 1));
		View child = getChildAt(index);
		boolean isLast = index == childCount - 1;

		// total width of children which after this index child is less than
		// parent width
		int lastChildWidth = 0;
		for (; index < childCount; index++) {
			View lastChild = getChildAt(index);
			lastChildWidth += lastChild.getWidth();
		}
		boolean widthLessThanParnent = lastChildWidth <= getWidth();

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

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
	}

	public WorkspaceListener getWorkspaceListener() {
		return mWorkspaceListener;
	}

	public void setWorkspaceListener(WorkspaceListener workspaceListener) {
		this.mWorkspaceListener = workspaceListener;
	}

	public interface WorkspaceListener {
		public void onChildChanged(int childIndex);

		public void onChildInvalidate();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.duration = mDuration;
		savedState.currentChildIndex = mCurrentChildIndex;
		savedState.snapVelocity = mSnapVelocity;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mDuration = savedState.duration;
		mCurrentChildIndex = savedState.currentChildIndex;
		mSnapVelocity = savedState.snapVelocity;
	}

	static class SavedState extends BaseSavedState {
		public int duration;
		public int snapVelocity;
		public int currentChildIndex;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			duration = in.readInt();
			snapVelocity = in.readInt();
			currentChildIndex = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(duration);
			out.writeFloat(snapVelocity);
			out.writeFloat(currentChildIndex);
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
