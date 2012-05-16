package com.aretha.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
	protected final static int SCROLL_DURATION = 500;
	private int mTouchState;

	private Scroller mScroller;

	private float mLastMotionX;

	private int mMaximumVelocity;

	protected int mSnapVelocity = 500;

	protected int mCurrentChildIndex;

	private VelocityTracker mVelocityTracker;
	private WorkspaceListener mWorkspaceListener;
	private int mContentWidth;

	private int mWidth;
	private int mHeight;

	private float mTouchDownX;

	private int mTouchSlop;

	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public Workspace(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public Workspace(Context context) {
		super(context);
		initialize();
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
		if(ev.getAction() == MotionEvent.ACTION_DOWN){
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
				animateToChild(mCurrentChildIndex);
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
		animateToChild(++mCurrentChildIndex);
	}

	public void animationToPrevChild() {
		animateToChild(--mCurrentChildIndex);
	}

	public void animateToChild(int index) {
		final int scroll = getScrollX();
		final int childCount = getChildCount();

		mCurrentChildIndex = index = Math.max(0,
				Math.min(index, childCount - 1));
		View child = getChildAt(index);
		boolean isLast = index == childCount - 1;

		mScroller.startScroll(scroll, 0, child.getLeft()
				- scroll
				- getPaddingLeft()
				- (isLast ? getWidth() - child.getWidth() - getPaddingLeft()
						- getPaddingRight() : 0), 0, SCROLL_DURATION);
		invalidate();
	}

	private void acquireVelocityTrackerAndAddMovement(MotionEvent ev) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
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
}
