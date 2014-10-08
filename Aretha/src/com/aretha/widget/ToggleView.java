/* Copyright (c) 2011-2012 Tang Ke
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
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.aretha.R;

public class ToggleView extends ViewGroup {
	private final static String LOG_TAG = "ToggleView";

	private final static int TOUCH_STATE_IDLE = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private final static int TOUCH_STATE_FLING = 2;

	private final static int ANIMATION_DURATION = 200;

	private int mTouchState;

	private boolean mToggleState;
	private boolean mTouchedInHandle;

	private int mTouchSlop;

	private float mDownScrollX;
	private float mDownX;

	private Rect mHandleFrame;
	private int mHandleWidth;

	private Scroller mScroller;

	private float mClipRadius;
	private RectF mClipRect;
	private View mHandle;

	public ToggleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ToggleView, defStyle, 0);

		mToggleState = a.getInt(R.styleable.ToggleView_toggle, 1) != 0 ? true
				: false;
		mClipRadius = a.getDimension(R.styleable.ToggleView_radius, 10);
		a.recycle();

		mClipRect = new RectF();

		initialize(context);
	}

	public ToggleView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.toggleViewStyle);
	}

	public ToggleView(Context context) {
		this(context, null);
	}

	private void initialize(Context context) {
		mTouchSlop = new ViewConfiguration().getScaledTouchSlop();
		mHandleFrame = new Rect();
		mScroller = new Scroller(context, new LinearInterpolator());
	}

	@Override
	public void draw(Canvas canvas) {
		Path clipPath = new Path();
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB
				&& canvas.isHardwareAccelerated()) {
			// find method to resolve clipPath not support exception
		} else {
			mClipRect.set(getScrollX(), 0, getScaleX() + getWidth(),
					getHeight());
			clipPath.addRoundRect(mClipRect, mClipRadius, mClipRadius,
					Path.Direction.CW);
			canvas.clipPath(clipPath);
		}
		super.draw(canvas);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		final int paddingLeft = getPaddingLeft();
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);

			final int childMeasuredWidth = child.getMeasuredWidth();
			final int childMeasuredHeight = child.getMeasuredHeight();

			final int id = child.getId();

			int childY = Math
					.round((getHeight() - paddingTop - paddingBottom - childMeasuredHeight)
							* 1.0f / 2 + paddingTop);
			int childX = 0;

			if (id == R.id.toggleHandle) {
				childX = paddingLeft;
				mHandleWidth = childMeasuredWidth;
			} else if (id == R.id.toggleOff) {
				childX = paddingLeft - childMeasuredWidth;
			} else if (id == R.id.toggleOn) {
				childX = mHandleWidth + paddingLeft;
			}

			child.layout(childX, childY, childX + childMeasuredWidth, childY
					+ childMeasuredHeight);
		}

		setToggle(mToggleState, false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int childCount = getChildCount();
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int measuredWidth = 0;
		int measuredHeight = 0;

		int handleWidth = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getId() == R.id.toggleHandle) {
				handleWidth = child.getMeasuredWidth();
			} else {
				measuredWidth = Math.max(
						handleWidth + child.getMeasuredWidth(), measuredWidth);
			}

			measuredHeight = Math
					.max(child.getMeasuredHeight(), measuredHeight);
		}

		measuredWidth += getPaddingLeft() + getPaddingRight();
		measuredHeight += getPaddingTop() + getPaddingBottom();

		measuredWidth = Math.max(getSuggestedMinimumWidth(), measuredWidth);
		measuredHeight = Math.max(getSuggestedMinimumHeight(), measuredHeight);

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mTouchState != TOUCH_STATE_FLING) {
			return;
		}

		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), 0);
		} else {
			mTouchState = TOUCH_STATE_IDLE;
		}

		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final View handle = getChildAt(0);
		final float x = event.getX();
		final float y = event.getY();
		final int scrollX = getScrollX();
		final int touchState = mTouchState;

		final boolean superResults = super.onTouchEvent(event);

		if (handle == null) {
			return superResults;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			requestDisallowInterceptTouchEvent(false);
			handle.setPressed(false);
			if (mTouchedInHandle && touchState == TOUCH_STATE_IDLE) {
				toggle();
			} else if (touchState == TOUCH_STATE_SCROLLING) {
				int centerHandleX = scrollX - handle.getWidth() / 2;
				int centerX = getWidth() / 2;
				setToggle(!(Math.abs(centerHandleX) > centerX), true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			// is the scroll began
			if (touchState == TOUCH_STATE_IDLE) {
				mTouchState = Math.abs(x - mDownX) > mTouchSlop ? TOUCH_STATE_SCROLLING
						: TOUCH_STATE_IDLE;
			}

			if (!mTouchedInHandle || touchState == TOUCH_STATE_IDLE) {
				return superResults;
			}

			int scroll = Math.max(
					Math.min((int) (mDownScrollX - (x - mDownX)), 0),
					computeScrollBoundsEndX(mHandleWidth));
			scrollTo(scroll, 0);
			break;
		case MotionEvent.ACTION_DOWN:
			mDownScrollX = scrollX;
			mDownX = x;
			mHandleFrame.set(-scrollX, handle.getTop(),
					-scrollX + handle.getRight(), handle.getBottom());
			mTouchedInHandle = mHandleFrame.contains((int) x, (int) y);
			handle.setPressed(mTouchedInHandle);
			// If user touched the handle
			requestDisallowInterceptTouchEvent(mTouchedInHandle);
			return true;
		}

		return super.onTouchEvent(event);
	}

	protected int computeScrollBoundsEndX(int handleWidth) {
		return -(getWidth() - handleWidth - getPaddingRight() - getPaddingLeft());
	}

	protected int computeScrollBoundsStartX(int handleWidth) {
		return 0;
	}

	@Override
	public void addView(View child, int index, LayoutParams params) {
		final int childCount = getChildCount();

		// Only allow handle in this view
		int childId = child.getId();
		if (childCount > 3
				|| (childId != R.id.toggleHandle && childId != R.id.toggleOff && childId != R.id.toggleOn)) {
			Log.w(LOG_TAG,
					"Only one child with id \"toggle_handle\" is allowed here");
			return;
		}

		if (childId == R.id.toggleHandle) {
			mHandle = child;
		}

		super.addView(child, index, params);
	}

	/**
	 * Set a handle for user to drag
	 * 
	 * @param handle
	 */
	public void setHandle(View handle) {
		setView(handle, R.id.toggleHandle);
	}

	/**
	 * Get the handle in this toggle
	 * 
	 * @return
	 */
	public View getHandle() {
		return findViewById(R.id.toggleHandle);
	}

	/**
	 * OFF View|Handle|ON View
	 * 
	 * @param onView
	 */
	public void setOnView(View onView) {
		setView(onView, R.id.toggleOn);
	}

	/**
	 * OFF View|Handle|ON View
	 * 
	 * @return
	 */
	public View getOnView() {
		return findViewById(R.id.toggleOn);
	}

	/**
	 * OFF View|Handle|ON View
	 * 
	 * @param offView
	 */
	public void setOffView(View offView) {
		setView(offView, R.id.toggleOff);
	}

	/**
	 * OFF View|Handle|ON View
	 * 
	 * @return
	 */
	public View getOffView() {
		return findViewById(R.id.toggleOff);
	}

	private void setView(View view, int id) {
		View existView = findViewById(R.id.toggleHandle);
		if (existView != null) {
			removeView(existView);
		}
		view.setId(R.id.toggleHandle);
		addView(view);
	}

	/**
	 * Set the radius to clip the this view
	 * 
	 * @param radius
	 *            value in scaled pixel
	 */
	public void setRadius(float radius) {
		setRadius(radius, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set the radius to clip the this view
	 * 
	 * @param radius
	 * @param unit
	 *            the unit of radius
	 */
	public void setRadius(float radius, int unit) {
		mClipRadius = Math.max(0, TypedValue.applyDimension(unit, radius,
				getContext().getResources().getDisplayMetrics()));
		invalidate();
	}

	/**
	 * Get the clip radius of this view
	 * 
	 * @return value in pixel
	 */
	public float getRadius() {
		return this.mClipRadius;
	}

	public void setToggle(boolean flag, boolean animate) {
		final int scrollX = getScrollX();
		final int handleWidth = mHandleWidth;

		mTouchState = TOUCH_STATE_FLING;
		mScroller.abortAnimation();
		mToggleState = flag;
		if (flag) {
			mScroller.startScroll(scrollX, 0,
					computeScrollBoundsStartX(handleWidth) - scrollX, 0,
					animate ? ANIMATION_DURATION : 0);
		} else {
			mScroller.startScroll(scrollX, 0,
					computeScrollBoundsEndX(handleWidth) - scrollX, 0,
					animate ? ANIMATION_DURATION : 0);
		}
		invalidate();
	}

	public void toggle() {
		setToggle(!mToggleState, true);
	}

	public boolean isToggle() {
		return this.mToggleState;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.toggleState = mToggleState;
		savedState.clipRadius = mClipRadius;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mToggleState = savedState.toggleState;
		mClipRadius = savedState.clipRadius;
	}

	static class SavedState extends BaseSavedState {
		public boolean toggleState;
		public float clipRadius;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			toggleState = in.readInt() == 0 ? false : true;
			clipRadius = in.readFloat();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(toggleState ? 1 : 0);
			out.writeFloat(clipRadius);
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
